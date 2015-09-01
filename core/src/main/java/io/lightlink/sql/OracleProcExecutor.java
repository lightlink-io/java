//package io.lightlink.sql;
//
//import io.lightlink.sql.dbridge.config.ArgDescr;
//import io.lightlink.sql.dbridge.config.ProcDesr;
//import oracle.jdbc.OracleCallableStatement;
//import oracle.jdbc.OracleConnection;
//import oracle.jdbc.OracleTypes;
//import oracle.sql.ARRAY;
//import oracle.sql.ArrayDescriptor;
//import oracle.sql.STRUCT;
//import oracle.sql.StructDescriptor;
//import org.apache.commons.beanutils.BeanMap;
//import org.apache.commons.collections.map.CaseInsensitiveMap;
//import org.springframework.jdbc.support.nativejdbc.OracleJdbc4NativeJdbcExtractor;
//import org.springframework.stereotype.Component;
//
//import java.sql.*;
//import java.sql.Date;
//import java.util.*;
//
//@Component
//public class OracleProcExecutor extends AbstractProcExecutor {
//
//
//    public static OracleJdbc4NativeJdbcExtractor oracleJdbc4NativeJdbcExtractor;
//
//    @Override
//    protected String prepareSQL(ProcDesr proc) {
//        StringBuilder sql = new StringBuilder();
//
//        sql.append("{call ").append(proc.getName()).append("(");
//
//        for (ArgDescr arg : proc.getArgs()) {
//            sql.append(arg.getName()).append(" => ?,");
//        }
//
//        sql.setLength(sql.length() - 1);
//        sql.append(")}");
//
//        return sql.toString();
//    }
//
//    @Override
//    protected boolean registerOutParams(CallableStatement cs, int pos, ArgDescr arg, Map<String, Object> params) throws SQLException {
//
//        boolean processes = super.registerOutParams(cs, pos, arg, params);
//        if (processes) {
//            return true;
//        }
//
//        OracleCallableStatement callStatement = getOracleCallableStatement(cs);
//
//        if (arg.isStringArray() || arg.isNumericArray()) {
//            callStatement.registerOutParameter(pos, OracleTypes.ARRAY, arg.getType());
//        } else if (arg.isStruct()) {
//            callStatement.registerOutParameter(pos, OracleTypes.STRUCT, arg.getType());
//        } else if (arg.isTimestampTZ()) {
//            callStatement.registerOutParameter(pos, OracleTypes.TIMESTAMPTZ);
//        } else if (arg.isCursor()) {
//            callStatement.registerOutParameter(pos, OracleTypes.CURSOR);
//        } else if (arg.isStructArray()) {
//            callStatement.registerOutParameter(pos, OracleTypes.ARRAY, arg.getType());
//        } else {
//            throw new IllegalArgumentException("Unrecognised out parameter " + arg);
//        }
//        return true;
//    }
//
//    @Override
//    protected void loadOutData(CallableStatement cs, ProcDesr proc, HashMap<String, Object> res)
//            throws SQLException {
//
//        super.loadOutData(cs, proc, res);
//
//        OracleCallableStatement callableStatement = getOracleCallableStatement(cs);
//
//        int pos = 0;
//        for (ArgDescr arg : proc.getArgs()) {
//            pos++;
//
//            if (arg.isOut()) {
//                Object value;
//
//                if (arg.isStringArray() || arg.isNumericArray()) {
//                    value = callableStatement.getArray(pos).getArray();
//                } else if (arg.isStructArray()) {
//                    List<Map> list = new ArrayList<Map>();
//                    Object[] structs = (Object[]) callableStatement.getArray(pos).getArray();
//
//                    for (Object struct : structs) {
//                        STRUCT structure = (STRUCT) struct;
//                        list.add(processMap(structure.getMap()));
//                    }
//                    value = list;
//                } else if (arg.isStruct()) {
//
//                    value = processMap(callableStatement.getSTRUCT(pos).getMap());
//
//                } else {
//                    continue;
//                }
//
//                res.put(arg.getName(), value);
//
//            }
//        }
//    }
//
//    @Override
//    protected boolean setInData(PreparedStatement ps, int pos, ArgDescr argDescr, Map<String, Object> params) throws SQLException {
//        if (ps instanceof CallableStatement)
//            return setInData((CallableStatement) ps, pos, argDescr, params);
//        else
//            return super.setInData(ps, pos, argDescr, params);
//    }
//
//
//    protected boolean setInData(CallableStatement cs, int pos, ArgDescr argDescr, Map<String, Object> params) throws SQLException {
//
//        OracleCallableStatement callStatement = getOracleCallableStatement(cs);
//
//        Connection connectionFromStatement = callStatement.getConnection();
//
//        Connection con = connectionFromStatement instanceof OracleConnection
//                ? connectionFromStatement
//                : getOracleJdbc4NativeJdbcExtractor().getNativeConnection(connectionFromStatement);
//
//        String name = argDescr.getName();
//
//        Object value = params.get(name);
//
//        boolean processed = super.setInData(callStatement, pos, argDescr, params);
//
//        if (!processed) {
//            if (argDescr.isTimestampTZ()) {
//
//
//                //            if (value == null) {
//                //                callStatement.setNull(pos, Types.TIMESTAMP);
//                //            } else if (value instanceof Number) {
//                //                callStatement.setTimestamp(pos, new Timestamp(((Number) value).longValue()));
//                //            } else if (value instanceof String) {
//                //                Long d = tryToConvertToDate(name, value);
//                //                callStatement.setTimestamp(pos, new Timestamp(d));
//                //            } else if (value instanceof Date) {
//                //                callStatement.setTimestamp(pos, new Timestamp(((Date) value).getTime()));
//                //            } else {
//                //                throw new IllegalArgumentException("DBRidge : Cannot convert to date the value:" + value + " field:" + name);
//                //            }
//
//            } else if (argDescr.isCustomType() && argDescr.isStruct()) {
//
//                if (value == null) {
//                    callStatement.setNull(pos, OracleTypes.STRUCT);
//                } else {
//
//                    if (!(value instanceof Map)) { // create a Map from bean
//                        Map map = new CaseInsensitiveMap(new BeanMap(value));
//                        map.remove("class");
//                        value = map;
//                    }
//
//                    STRUCT struct = createStructFromMap(con, (Map) value, argDescr.getType());
//                    callStatement.setSTRUCT(pos, struct);
//
//                }
//
//            } else if (argDescr.isCustomType() && argDescr.isStructArray()) {
//
//                ArrayDescriptor arrayStructDesc = safeCreateArrayDescriptor(argDescr.getType(), con);
//
//                if (value == null) {
//                    callStatement.setNull(pos, OracleTypes.NULL);
//                } else if (value instanceof List || value.getClass().isArray()) {
//                    if (value.getClass().isArray()) {
//                        value = Arrays.asList((Object[]) value);
//                    }
//
//                    List records = (List) value;
//
//                    STRUCT[] structArray = new STRUCT[records.size()];
//
//                    for (int i = 0; i < structArray.length; i++) {
//                        Object record = records.get(i);
//
//                        if (!(record instanceof Map)) { // create a Map from bean
//                            Map map = new CaseInsensitiveMap(new BeanMap(record));
//                            map.remove("class");
//                            record = map;
//                        }
//
//                        structArray[i] = createStructFromMap(con, (Map) record, arrayStructDesc.getBaseName());
//                    }
//
//                    ARRAY inArray = new ARRAY(arrayStructDesc, con, structArray);
//
//                    callStatement.setArray(pos, inArray);
//
//                } else {
//                    throw new IllegalArgumentException("Column " + argDescr.getName() + " of oracleKind=" + argDescr.getOracleKind() +
//                            " accepts array of array of values (in the order of fields declared in STRUCT definition)");
//                }
//
//
//            } else if (argDescr.isStringArray() || argDescr.isNumericArray()) {
//                ArrayDescriptor arrayStructDesc = safeCreateArrayDescriptor(argDescr.getType(), con);
//                if (value == null) {
//                    ARRAY inArray = new ARRAY(arrayStructDesc, con, new Object[0]);
//                    callStatement.setArray(pos, inArray);
//                } else if (value instanceof List) {
//                    ARRAY inArray = new ARRAY(arrayStructDesc, con, ((List) value).toArray());
//                    callStatement.setArray(pos, inArray);
//                } else {
//                    throw new IllegalArgumentException("Column " + argDescr.getName() + " of oracleKind=" + argDescr.getOracleKind() +
//                            " accepts array(list) of values.");
//                }
//
//
//            } else if (argDescr.isCustomType()) {
//                throw new IllegalArgumentException("Column " + argDescr.getName() + " of type=" + argDescr.getType() +
//                        " must have a oracleKind set to :" +
//                        "'" + ArgDescr.NUMERIC_ARRAY + "' or " +
//                        "'" + ArgDescr.STRING_ARRAY + "' or " +
//                        "'" + ArgDescr.STRUCT_ARRAY + "' or " +
//                        "'" + ArgDescr.STRUCT
//                );
//
//            } else {
//                throw new IllegalArgumentException("Cannot set in param " + argDescr.getName() + " of type=" + argDescr.getType());
//            }
//        }
//        return true;
//    }
//
//    private OracleJdbc4NativeJdbcExtractor getOracleJdbc4NativeJdbcExtractor() {
//        if (oracleJdbc4NativeJdbcExtractor ==null)
//            oracleJdbc4NativeJdbcExtractor = new OracleJdbc4NativeJdbcExtractor();
//        return oracleJdbc4NativeJdbcExtractor;
//    }
//
//    private OracleCallableStatement getOracleCallableStatement(CallableStatement cs) throws SQLException {
//        OracleCallableStatement callStatement;
//        if (cs instanceof OracleCallableStatement) {
//            callStatement = (OracleCallableStatement) cs;
//        } else {
//            callStatement = cs.unwrap(OracleCallableStatement.class);
//        }
//        return callStatement;
//    }
//
//    private STRUCT createStructFromMap(Connection con, Map value, String type) throws SQLException {
//        STRUCT struct;
//        StructDescriptor structType = safeCreateStructureDescriptor(type, con);
//        ResultSetMetaData stuctMeteData = structType.getMetaData();
//
//        value = new CaseInsensitiveMap(value);
//
//        List orderedValues = new ArrayList();
//
//        if (stuctMeteData.getColumnCount() == 1 && value.size() == 1) {
//            orderedValues.add(value.values().iterator().next());
//        } else {
//            for (int col = 1; col <= stuctMeteData.getColumnCount(); col++) {
//                Object v = value.get(stuctMeteData.getColumnName(col));
//                if (v == null) {
//                    v = value.get(stuctMeteData.getColumnName(col).replaceAll("_", ""));
//                }
//                orderedValues.add(v);
//            }
//        }
//
//        Object[] values = orderedValues.toArray();
//
//        for (int j = 0; j < values.length; j++) {
//
//            Object v = values[j];
//            if (v instanceof Long && stuctMeteData.getColumnTypeName(j + 1).equalsIgnoreCase("TIMESTAMP")) {
//                values[j] = new Timestamp((Long) v);
//            } else if (v instanceof Long && stuctMeteData.getColumnTypeName(j + 1).equalsIgnoreCase("DATE")) {
//                values[j] = new Date((Long) v);
//            }
//
//        }
//
//        struct = new STRUCT(structType, con, values);
//
//        return struct;
//    }
//
//    private StructDescriptor safeCreateStructureDescriptor(String baseName, Connection con) throws SQLException {
//        StructDescriptor res = StructDescriptor.createDescriptor(baseName, con);
//        if (res == null) {
//            throw new IllegalArgumentException("Cannot create STRUCT with name:" + baseName);
//        }
//        return res;
//    }
//
//    private ArrayDescriptor safeCreateArrayDescriptor(String arrayName, Connection con) throws SQLException {
//        ArrayDescriptor res = ArrayDescriptor.createDescriptor(arrayName, con);
//        if (res == null) {
//            throw new IllegalArgumentException("Cannot create ARRAY with name:" + arrayName);
//        }
//        return res;
//    }
//
//
//}
