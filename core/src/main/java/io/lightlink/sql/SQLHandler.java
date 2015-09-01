package io.lightlink.sql;

import io.lightlink.autostop.AutoStopQuery;
import io.lightlink.translator.ScriptTranslator;
import io.lightlink.core.RunnerContext;
import io.lightlink.facades.SQLFacade;
import io.lightlink.output.ResponseStream;
import io.lightlink.types.*;
import io.lightlink.utils.Utils;
import jdk.nashorn.api.scripting.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class SQLHandler {

    public static final Logger LOG = LoggerFactory.getLogger(SQLHandler.class);

    PreparedStatement batchPS;
    CallableStatement batchCS;
    List<ArgInfo> batchArgInfos;
    private String batchSQL;

    private SQLFacade facade;

    public SQLHandler(SQLFacade facade) {
        this.facade = facade;
        this.facade = facade;
    }

    public void query(boolean addBatch, String resultSetName, RunnerContext runnerContext, String sql, JSObject rowHandler)
            throws SQLException, IOException, ScriptException {


        LOG.info("SQL:" + sql);

        if (sql == null || Utils.isBlank(sql)) {

            if (!addBatch && batchPS != null) {
                batchPS.addBatch();

                int[] updateCounts = batchPS.executeBatch();

                ArrayList<Integer> updateCountList = new ArrayList<Integer>(updateCounts.length);
                for (int updateCount : updateCounts) {
                    updateCountList.add(updateCount);
                }

                facade.setUpdateCount(updateCountList);
                batchPS = batchCS = null;
            }
            return;
        }


        Connection conn = facade.getConnection();

        // guess IN/OUT params

        List<ArgInfo> argInfos = new ArrayList<ArgInfo>();

        sql = guessArgs(runnerContext, sql, argInfos);
        try {
            prepareInArgsValues(conn, runnerContext, argInfos);


            // prepare/callable statement
            boolean outParam = false;
            for (ArgInfo argInfo : argInfos) {
                outParam = outParam || argInfo.isOut();
            }
            PreparedStatement ps;
            CallableStatement cs;

            if (outParam) {
                if (batchCS == null && batchPS != null) {
                    throw new RuntimeException("Cannot mix PreparedStatement and CallableStatement in  a batch");
                } else if (batchCS != null) {
                    ps = cs = batchCS;
                    checkSqlAndAddBatch(sql, ps);
                } else {
                    ps = cs = conn.prepareCall(sql);
                }
            } else {
                if (batchPS != null) {
                    ps = batchPS;
                    cs = batchCS;
                    checkSqlAndAddBatch(sql, ps);
                } else {
                    ps = conn.prepareStatement(sql);
                    cs = null;
                }
            }

            // inject in params & declare out params
             for (int i = 0; i < argInfos.size(); i++) {
                ArgInfo argInfo = argInfos.get(i);
                Integer sqlType = argInfo.findOutSqlType();
                if (argInfo.isIn()) {
                    Object value = argInfo.getValue();

                    AbstractConverter converter = argInfo.getConverter();
                    if (value == null) {
                        if (sqlType == null)
                            ps.setObject(i + 1, null);
                        else if (converter != null && converter.getCustomSQLTypeName() != null)
                            ps.setNull(i + 1, sqlType, converter.getCustomSQLTypeName());
                        else
                            ps.setNull(i + 1, sqlType);
                    } else if (value instanceof ByteArrayInputStream) {
                        int bytes = ((ByteArrayInputStream) value).available();
                        System.out.println("sql = " + sql);
                        System.out.println("bytes = " + bytes);
                        ps.setBinaryStream(i + 1, (ByteArrayInputStream) value, bytes);
                    } else if (value instanceof InputStream) {
                        System.out.println("IS sql = " + sql);
                        System.out.println("IS bytes = " + ((InputStream) value).available());
                        ps.setBinaryStream(i + 1, (InputStream) value);
                    } else if (sqlType != null) {
                        ps.setObject(i + 1, value, sqlType);
                    } else {
                        ps.setObject(i + 1, value);
                    }
                }
                if (argInfo.isOut() && cs != null) {
                    if (sqlType == null)
                        sqlType = Types.VARCHAR;

                    String typeName = argInfo.findOutSqlTypeName();
                    if (typeName != null)
                        cs.registerOutParameter(i + 1, sqlType, typeName);
                    else
                        cs.registerOutParameter(i + 1, sqlType);
                }

            }

            if (facade.getQueryTimeout() != null)
                ps.setQueryTimeout(facade.getQueryTimeout());
            ps.setFetchSize(facade.getFetchSize());
            if (facade.getMaxRows() != null)
                ps.setMaxRows(facade.getMaxRows());

            if (addBatch) {
                batchPS = ps;
                batchCS = cs;
                batchArgInfos = argInfos;
                batchSQL = sql;
                // save references so that they will be reused and executed for next queries
            } else {
                preparedStatementExecute(resultSetName, runnerContext, rowHandler, argInfos, ps, cs);
                batchPS = batchCS = null;
            }
        } catch (SQLException e) {
            throw new SQLException("SQL:" + sql + " \n" + e.toString(), e);
        }


    }

    private void checkSqlAndAddBatch(String sql, PreparedStatement ps) throws SQLException {
        // remove all whitespaces and compare SQLs
        if (!sql.replaceAll("\\s", "").equalsIgnoreCase(batchSQL.replaceAll("\\s", ""))) {
            // if different warn developer
            throw new RuntimeException("SQL statement: \n" + sql + "\n does not match to the initial one on a batch:\n" + batchSQL);
        }
        ps.addBatch();
    }

    private void preparedStatementExecute(String resultSetName, RunnerContext runnerContext, JSObject rowHandler, List<ArgInfo> argInfos, PreparedStatement ps, CallableStatement cs) throws SQLException, IOException {
        // execute
        boolean resultsAvailable;
        try {
            AutoStopQuery.getInstance().register(runnerContext, ps);
            resultsAvailable = ps.execute();
        } finally {
            AutoStopQuery.getInstance().unregister(runnerContext, ps);
        }

        loadResultSets(resultSetName, runnerContext, ps, resultsAvailable, rowHandler);

        if (cs != null) // if out params present
            loadOutData(argInfos, runnerContext, cs, rowHandler);
    }

    private void loadResultSets(String resultSetName, RunnerContext runnerContext, PreparedStatement ps, boolean resultsAvailable, JSObject rowHandler) throws SQLException, IOException {


        List<Integer> updateCount = new ArrayList<Integer>();


        while (true) {
            if (!resultsAvailable) {
                int uc = ps.getUpdateCount();
                if (uc == -1) {
                    break;
                } else {
                    updateCount.add(uc);
                }
            } else {
                ResultSet rs = ps.getResultSet();

                loadResultSet(runnerContext, makeUniqueResultSetName(runnerContext, resultSetName), rs, rowHandler);
            }

            resultsAvailable = ps.getMoreResults();
        }
        facade.setUpdateCount(updateCount);

    }

    private String makeUniqueResultSetName(RunnerContext runnerContext, String resultSetName) {

        if (resultSetName == null || Utils.isBlank(resultSetName))
            resultSetName = "resultSet";

        Set<String> usedNames = runnerContext.getUsedResultSetNames();

        String newResultSetName = resultSetName;
        int counter = 2;

        while (usedNames.contains(newResultSetName)) {
            newResultSetName = resultSetName + counter;
            counter++;
        }
        usedNames.add(newResultSetName);
        return newResultSetName;

    }

    private void loadResultSet(RunnerContext runnerContext, String rsName, ResultSet rs, JSObject rowHandler) throws SQLException, IOException {

        ResponseStream resp = runnerContext.getResponseStream();

        resp.writePropertyArrayStart(rsName);

        rs.setFetchSize(facade.getFetchSize());

        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int cnt = metaData.getColumnCount();
            String[] cols = new String[cnt];
            String[] outNames = new String[cnt];
            AbstractConverter[] convertors = new AbstractConverter[cnt];

            for (int i = 0; i < cols.length; i++) {
                String label = metaData.getColumnLabel(i + 1);
                cols[i] = label;
                if (label.startsWith("(")) {
                    ArgInfo argInfo = new ArgInfo(label);
                    applyDirectivesToArgInfo(runnerContext, argInfo);
                    convertors[i] = argInfo.getConverter();
                    outNames[i] = label.substring(label.lastIndexOf(")") + 1);
                } else {
                    outNames[i] = label;
                }
            }

            JSObject line = (rowHandler == null)
                    ? null // not needed without rowHandler
                    : runnerContext.newJSObject();

            int index = 0;
            while (rs.next()) {
                if (rowHandler != null) {

                    for (int i = 0; i < cols.length; i++) {

                        Object value = (convertors[i] != null)
                                ? convertors[i].readFromResultSet(rs, i + 1, runnerContext, cols[i])
                                : rs.getObject(i + 1);

                        line.setMember(outNames[i], value);
                    }
                    Object res = rowHandler.call(rowHandler, line, index, rsName);
                    if (res instanceof Map)
                        res = new LinkedHashMap((Map) res);
                    else if (res instanceof List)
                        res = new ArrayList((List) res);
                    // todo : real deep copy


                    resp.writeFullObjectToArray(genericConvertFromJdbc(runnerContext, res));

                } else {

                    resp.writeObjectStart();
                    for (int i = 0; i < cols.length; i++) {
                        Object value = (convertors[i] != null)
                                ? convertors[i].readFromResultSet(rs, i + 1, runnerContext, cols[i])
                                : rs.getObject(i + 1);

                        resp.writeProperty(outNames[i], genericConvertFromJdbc(runnerContext, value));
                    }
                    resp.writeObjectEnd();

                }
                index++;

            }



        } finally {
            resp.writePropertyArrayEnd();
            rs.close();
        }
    }


    protected void loadOutData(List<ArgInfo> args, RunnerContext runnerContext, CallableStatement cs, JSObject rowHandler)
            throws SQLException, IOException {


        int pos = 0;
        for (ArgInfo arg : args) {
            pos++;

            if (arg.isOut()) {
                Object value;


                value = cs.getObject(pos);

                String name = arg.getName();
                if (name.contains(")"))
                    name = name.substring(name.lastIndexOf(')') + 1);
                if (name.startsWith("p."))
                    name = name.substring(2);

                if (arg.getConverter() != null) {
                    value = arg.getConverter().readFromCallableStatement(cs, pos, runnerContext, name);
                } else {
                    value = genericConvertFromJdbc(runnerContext, value);
                }

                if (value instanceof ResultSet) {
                    ResultSet rs = ((ResultSet) value);
                    loadResultSet(runnerContext, name, rs, rowHandler);
                } else
                    runnerContext.getResponseStream().writeProperty(name, value);
            }
        }
    }


    protected Map<String, Object> processMap(RunnerContext runnerContext, Map<String, Object> map) throws SQLException {
// todo : why not called ?
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            entry.setValue(genericConvertFromJdbc(runnerContext, entry.getValue()));
        }
        return map;
    }

    protected Object genericConvertFromJdbc(RunnerContext runnerContext, Object value) throws SQLException {

        if (value instanceof Array) {
            Object[] array = (Object[]) ((Array) value).getArray();
            for (int i = 0; i < array.length; i++) {
                Object el = array[i];
                array[i] = genericConvertFromJdbc(runnerContext, el);
            }
            value = array;
        } else if (value instanceof Struct) {
            Struct struct = (Struct) value;
            Object[] attributes = struct.getAttributes();
            for (int i = 0; i < attributes.length; i++) {
                Object el = attributes[i];
                attributes[i] = genericConvertFromJdbc(runnerContext, el);
            }
            value = attributes;
        } else if (value instanceof Clob) {
            Clob clob = (Clob) value;
            value = clob.getCharacterStream();
        } else if (value instanceof java.util.Date) {
            value = runnerContext.getTypesFacade().dateToString((java.util.Date) value);
        }
        // any convertions will be done here
        return value;
    }


    private void prepareInArgsValues(Connection connection, RunnerContext runnerContext, List<ArgInfo> argInfos) throws IOException, SQLException {
        for (ArgInfo argInfo : argInfos) {
            if (argInfo.isIn()) {
                String name = argInfo.getName();
                Object value = null;
                try {
                    value = runnerContext.getParam(name);
                    if (LOG.isDebugEnabled())
                        LOG.debug("for " + name + " value:" + value);

                } catch (ScriptException e) {
                    throw new IllegalArgumentException("Cannot evaluate the value of parameter:");
                }

                AbstractConverter convertor = argInfo.getConverter();
                if (convertor != null) {
                    value = Utils.tryConvertToJavaCollections(value);
                    Object newValue = convertor.convertToJdbc(connection, runnerContext, name, value);
                    argInfo.setValue(newValue);
                } else {
                    argInfo.setValue(value);
                }
            }
        }
    }

    public static String guessArgs(RunnerContext runnerContext, String sql, List<ArgInfo> args) {
        List<String> usedArgs = new ArrayList<String>();
        StringBuilder sb = new StringBuilder(sql);
        int pos = sb.indexOf(":");
        while (pos != -1) {
            if (pos != 0 && sb.charAt(pos - 1) != ':' && pos < sb.length() - 1 && ScriptTranslator.isBindingExpressionChar(sb.charAt(pos), sb.charAt(pos + 1), "")) {
                int pos2 = pos + 1;
                while (pos2 < sb.length() && ScriptTranslator.isBindingExpressionChar(sb.charAt(pos2 - 1), sb.charAt(pos2)
                        , sb.substring(pos, pos2)))
                    pos2++;
                String arg = sb.substring(pos + 1, pos2).trim();
                sb.replace(pos, pos2, "?");
                pos -= arg.length();

                usedArgs.add(arg);
            }
            pos = sb.indexOf(":", pos + 1);
        }
        sql = sb.toString().replaceAll("::", ":");

        for (String fullName : usedArgs) {

            ArgInfo argInfo = new ArgInfo(fullName);
            args.add(argInfo);

            applyDirectivesToArgInfo(runnerContext, argInfo);
        }
        return sql;
    }

    public static void applyDirectivesToArgInfo(RunnerContext runnerContext, ArgInfo argInfo) {
        List<String> directives = findDirectives(argInfo.getName());
        for (String directive : directives) {
            String directiveLower = directive.toLowerCase();
            if (directive.equalsIgnoreCase("inout")) {
                argInfo.setIn(true);
                argInfo.setOut(true);
                continue;
            } else if (directive.equalsIgnoreCase("out")) {
                argInfo.setIn(false);
                argInfo.setOut(true);
                continue;
            }
            String parts[] = directive.split("\\.", 2);

            AbstractConverter converter;

            if (directiveLower.startsWith("blob")) {
                converter = BlobConverter.getInstance();
            } else if (directiveLower.startsWith("array"))
                converter = ArrayConverter.getInstance();
            else if (directiveLower.startsWith("json"))
                converter = JSONConverter.getInstance();
            else if (directive.equalsIgnoreCase("number") || directive.equalsIgnoreCase("numeric"))
                converter = NumberConverter.getInstance();
            else if (directive.equalsIgnoreCase("date"))
                converter = DateConverter.getInstance();
            else
                converter = runnerContext.getTypesFacade().getCustomConverter(parts[0]);

            if (converter != null && parts.length > 1) {
                converter.setConfig(parts[1]);
            }

            argInfo.setConverter(converter);

        }

        if (!argInfo.isOut())
            argInfo.setIn(true); // default direction even if (in) directive is missing
    }

    private static List<String> findDirectives(String fullName) {
        ArrayList<String> directives = new ArrayList<String>();
        while (fullName.length() > 1 && fullName.charAt(0) == '(' && fullName.contains(")")) {
            int pos2 = fullName.indexOf(')');
            directives.add(fullName.substring(1, pos2));
            fullName = fullName.substring(pos2 + 1);
        }
        return directives;
    }


}
