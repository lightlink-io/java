//package io.lightlink.sql;

/*
 * #%L
 * lightlink-core
 * %%
 * Copyright (C) 2015 Vitaliy Shevchuk
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

//
//import io.lightlink.sql.dbridge.config.ArgDescr;
//import io.lightlink.sql.dbridge.config.ProcDesr;
//import io.lightlink.sql.dbridge.config.ResultSetDescr;
//import io.lightlink.sql.dbridge.config.SqlDesr;
//import org.apache.commons.beanutils.PropertyUtils;
//import org.apache.commons.collections.map.CaseInsensitiveMap;
//import org.apache.commons.lang.StringUtils;
//import org.apache.log4j.Logger;
//import org.apache.velocity.VelocityContext;
//import org.apache.velocity.app.VelocityEngine;
//import org.springframework.jdbc.core.ColumnMapRowMapper;
//import org.springframework.util.LinkedCaseInsensitiveMap;
//
//import java.io.StringWriter;
//import java.sql.*;
//import java.sql.Date;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//import static io.lightlink.sql.dbridge.config.ConfigLoader.isBindingExpressionChar;
//
///**
// *
// */
//public abstract class AbstractProcExecutor {
//
//
//    public static final String[][] DATE_PATTERNS = new String[][]{
//            {"^\\d\\d/\\d\\d/\\d\\d\\d\\d$", "dd/MM/yyyy"},
//            {"^\\d\\d/\\d\\d/\\d\\d\\d\\d \\d\\d:\\d\\d$", "dd/MM/yyyy HH:mm"},
//            {"^\\d\\d/\\d\\d/\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d$", "dd/MM/yyyy HH:mm:ss"},
//
//            {"^\\d\\d/\\d\\d/\\d\\d$", "dd/MM/yy"},
//
//            {"^\\d\\d\\d\\d-\\d\\d-\\d\\d$", "yyyy-MM-dd"},
//            {"^\\d\\d\\d\\d-\\d\\d-\\d\\d\\ \\d\\d:\\d\\d$", "yyyy-MM-dd HH:mm"},
//            {"^\\d\\d\\d\\d-\\d\\d-\\d\\d\\ \\d\\d:\\d\\d:\\d\\d$", "yyyy-MM-dd HH:mm:ss"},
//
//            {"^\\d\\d\\d\\d\\d\\d\\d\\d$", "yyyyMMdd"},
//            {"^\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d$", "yyyyMMddHHmmss"},
//
//    };
//
//
//    protected abstract String prepareSQL(ProcDesr proc);
//
//    public static final ColumnMapRowMapper ROW_MAPPER = new ColumnMapRowMapper();
//
//    public Map<String, Object> doExecute(Connection c, ProcDesr proc, Map<String, Object> params) throws SQLException {
//
//        params = new CaseInsensitiveMap(params);
//
//        String sql = prepareSQL(proc);
//
//        CallableStatement cs = c.prepareCall(sql);
//
//        try {
//
//            HashMap<String, Object> res = new LinkedCaseInsensitiveMap<Object>();
//
//            int pos = 0;
//
//            for (ArgDescr argDescr : proc.getArgs()) {
//                pos++;
//
//                if (argDescr.isIn()) {
//                    setInData(cs, pos, argDescr, params);
//                }
//
//                if (argDescr.isOut()) {
//                    registerOutParams(cs, pos, argDescr, params);
//                }
//            }
//
//            boolean resultsAvailable = cs.execute();
//
//            loadResultSets(cs, proc, res, resultsAvailable);
//            loadOutData(cs, proc, res);
//
//
//            return res;
//
//        } finally {
//            cs.close();
//        }
//
//
//    }
//
//    protected void loadOutData(CallableStatement cs, ProcDesr proc, HashMap<String, Object> res)
//            throws SQLException {
//
//
//        int pos = 0;
//        for (ArgDescr arg : proc.getArgs()) {
//            pos++;
//
//            if (arg.isOut()) {
//                Object value;
//
//
//                if (arg.isStringArray() || arg.isNumericArray() || arg.isStructArray() || arg.isStruct()) {
//                    // ignore oracle specific field
//                } else {
//                    try {
//                        value = cs.getObject(pos);
//                    } catch (SQLException e) {
//                        value = null; // for example is the returned object is Oracle sursor that is closed
//                    }
//
//                    if (value instanceof ResultSet) {
//                        ResultSet rs = ((ResultSet) value);
//                        String[] columns = arg.getColumnsAsArray();
//                        value = loadResultSet(rs, columns);
//
//                    } else {
//                        value = processValue(value);
//                    }
//                    res.put(arg.getName(), value);
//                }
//            }
//        }
//    }
//
//    private List<Map<String, Object>> loadResultSet(ResultSet rs, String[] columns) throws SQLException {
//        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
//
//        rs.setFetchSize(100);
//        try {
//            if (columns != null || columns.length==0) {
//                while (rs.next()) {
//                    Map<String, Object> map = new LinkedHashMap<String, Object>(columns.length);
//                    for (int i = 0, columnsLength = columns.length; i < columnsLength; i++) {
//                        String column = columns[i];
//                        map.put(column, processValue(rs.getObject(column)));
//                    }
//
//                    list.add(map);
//                }
//            } else {
//                while (rs.next()) {
//                    Map<String, Object> map = ROW_MAPPER.mapRow(rs, 0);
//
//                    list.add(processMap(map));
//                }
//            }
//            return  list;
//        } finally {
//            rs.close();
//        }
//    }
//
//    protected Map<String, Object> processMap(Map<String, Object> map) throws SQLException {
//
//        for (Map.Entry<String, Object> entry : map.entrySet()) {
//            entry.setValue(processValue(entry.getValue()));
//        }
//        return map;
//    }
//
//    protected Object processValue(Object value) throws SQLException {
//
//        if (value instanceof Array) {
//            Object[] array = (Object[]) ((Array) value).getArray();
//            for (Object el : array) {
//                processValue(el);
//            }
//            value = array;
//        } else if (value instanceof Struct) {
//            Struct struct = (Struct) value;
//            Object[] attributes = struct.getAttributes();
//            for (Object el : attributes) {
//                processValue(el);
//            }
//            value = attributes;
//        }
//
//        // any convertions will be done here
//        return value;
//    }
//
//    private void loadResultSets(PreparedStatement ps, ProcDesr proc, HashMap<String, Object> res, boolean resultsAvailable) throws SQLException {
//
//        List<Integer> updateCount = (List<Integer>) res.get("updatesCount");
//        if (updateCount == null)
//            updateCount = new ArrayList<Integer>();
//
//        List<List<Map<String, Object>>> resultSets = (List<List<Map<String, Object>>>) res.get("resultSets");
//        if (resultSets == null)
//            resultSets = new ArrayList<List<Map<String, Object>>>();
//        int rsNumber = 0;
//        while (true) {
//            if (!resultsAvailable) {
//                int uc = ps.getUpdateCount();
//                if (uc == -1) {
//                    break;
//                } else {
//                    updateCount.add(uc);
//                }
//            } else {
//                ResultSet rs = ps.getResultSet();
//                String[] columns = null;
//
//                if (proc.getResultSets().size()>rsNumber){
//                    ResultSetDescr rsDescr = proc.getResultSets().get(rsNumber);
//                    columns = rsDescr.getColumnsAsArray();
//                }
//
//                resultSets.add(loadResultSet(rs, columns));
//                rsNumber++;
//            }
//
//            resultsAvailable = ps.getMoreResults();
//        }
//        if (resultSets.size() > 0)
//            res.put("resultSets", resultSets);
//        if (updateCount.size() > 0)
//            res.put("updatesCount", updateCount);
//
//    }
//
//    protected boolean registerOutParams(CallableStatement cs, int pos, ArgDescr arg, Map<String, Object> params) throws SQLException {
//        if (arg.isString()) {
//            cs.registerOutParameter(pos, Types.VARCHAR);
//        } else if (arg.isNumeric()) {
//            cs.registerOutParameter(pos, Types.NUMERIC);
//        } else if (arg.isDate()) {
//            cs.registerOutParameter(pos, Types.DATE);
//        } else if (arg.isTimestamp()) {
//            cs.registerOutParameter(pos, Types.TIMESTAMP);
//        } else
//            return false;
//
//        return true;
//    }
//
//
//    protected boolean setInData(PreparedStatement ps, int pos, ArgDescr argDescr, Map<String, Object> params) throws SQLException {
//
//        String name = argDescr.getName();
//        Object value = null;
//        try {
//            value = PropertyUtils.getProperty(params, name);
//        } catch (Exception e) {
//            throw new IllegalArgumentException(e.toString(),e);
//        }
//
//
//        if (argDescr.isString()) {
//            String strValue = (null == value) ? null : value.toString();
//            ps.setString(pos, strValue);
//        } else if (argDescr.isNumeric()) {
//            if (value == null || (value instanceof String && StringUtils.isBlank((String) value)))
//                ps.setNull(pos, Types.NUMERIC);
//            else if (value instanceof Number)
//                ps.setDouble(pos, ((Number) value).doubleValue());
//            else {
//                try {
//                    ps.setDouble(pos, Double.parseDouble("" + value));
//                } catch (NumberFormatException e) {
//                    throw new IllegalArgumentException("Cannot parse as numeric  value:" + value + " field:" + name);
//                }
//            }
//        } else if (argDescr.isDate()) {
//            if (value == null) {
//                ps.setNull(pos, Types.DATE);
//            } else if (value instanceof Number) {
//                ps.setDate(pos, new Date(((Number) value).longValue()));
//            } else if (value instanceof String) {
//                if (StringUtils.isBlank((String) value)) {
//                    ps.setNull(pos, Types.DATE);
//                } else {
//                    Long d = tryToConvertToDate(name, (String) value);
//                    ps.setDate(pos, new Date(d));
//                }
//            } else if (value instanceof java.util.Date) {
//                ps.setDate(pos, new Date(((java.util.Date) value).getTime()));
//            } else {
//                throw new IllegalArgumentException("Cannot convert to date the value:" + value + " field:" + name);
//            }
//
//        } else if (argDescr.isTimestamp()) {
//            if (value == null) {
//                ps.setNull(pos, Types.TIMESTAMP);
//            } else if (value instanceof Number) {
//                ps.setTimestamp(pos, new Timestamp(((Number) value).longValue()));
//            } else if (value instanceof String) {
//                Long d = tryToConvertToDate(name, (String) value);
//                ps.setTimestamp(pos, new Timestamp(d));
//            } else if (value instanceof Date) {
//                ps.setTimestamp(pos, new Timestamp(((Date) value).getTime()));
//            } else {
//                throw new IllegalArgumentException("Cannot convert to date the value:" + value + " field:" + name);
//            }
//        } else
//            return false;
//
//
//        return true;
//    }
//
//
//    protected Long tryToConvertToDate(String name, String v) {
//
//        Long d = null;
//        try {
//            for (String[] pattern : DATE_PATTERNS) {
//                if (v.matches(pattern[0])) {
//                    d = new SimpleDateFormat(pattern[1]).parse(v).getTime();
//                }
//            }
//        } catch (ParseException e) { /**/ }
//
//        if (d == null) {
//            throw new IllegalArgumentException("Cannot parse as date the value:" + v + " field:" + name);
//        }
//        return d;
//    }
//
//    public Map<String, Object> executeSQL(Connection c, SqlDesr sqlDesr, Map<String, Object> params) throws Exception {
//        HashMap<String, Object> res = new HashMap<String, Object>();
//        String sql = velocityProcess(sqlDesr.getSql(), params);
//
//        String[] statements = sql.split("(?i)--NEXT--SQL--");
//        for (String statement : statements) {
//
//            LOG.info(statement);
//            long l = System.currentTimeMillis();
//            executeStatement(c, sqlDesr, params, res, statement);
//            long l1 = System.currentTimeMillis() - l;
//            LOG.info("sql executed in :"+ l1 +" millis");
//            params.putAll(res);
//        }
//        return res;
//
//
//    }
//
//    private String velocityProcess(String sql, Map<String, Object> params) throws Exception {
//        VelocityContext context = new VelocityContext(params);
//
//        StringWriter writer = new StringWriter();
//        ve.evaluate(context,
//                writer,
//                "LOG",
//                sql);
//
//        return writer.toString();
//
//    }
//
//    private void executeStatement(Connection c, SqlDesr sqlDesr, Map<String, Object> params, HashMap<String, Object> res, String sql) throws SQLException {
//        StringBuffer sb = new StringBuffer(sql);
//
//        List<String> usedArgs = new ArrayList<String>();
//
//        int pos = sb.indexOf(":");
//        while (pos != -1) {
//            if (pos != 0 && sb.charAt(pos - 1) != ':' && pos < sb.length() - 1 && isBindingExpressionChar(sb.charAt(pos + 1))) {
//                int pos2 = pos+1;
//                while (pos2 < sb.length() && isBindingExpressionChar(sb.charAt(pos2)))
//                    pos2++;
//                String arg = sb.substring(pos + 1, pos2).trim();
//                sb.replace(pos, pos2, "?");
//                pos -= arg.length();
//
//                usedArgs.add(arg);
//            }
//            pos = sb.indexOf(":", pos + 1);
//        }
//        PreparedStatement ps = c.prepareStatement(sb.toString().replaceAll("::", ":"));
//
//
//        try {
//            for (int i = 0; i < usedArgs.size(); i++) {
//                String usedArg = usedArgs.get(i);
//                ArgDescr argDescr = findArg(sqlDesr.getArgs(), usedArg);
//                if (argDescr.isIn()) {
//                    setInData(ps, i+1, argDescr, params);
//                }
//            }
//
//            boolean resultsAvailable = ps.execute();
//
//            loadResultSets(ps, sqlDesr, res, resultsAvailable);
//
//
//        } finally {
//            ps.close();
//        }
//    }
//
//    private ArgDescr findArg(List<ArgDescr> args, String usedArg) {
//        for (ArgDescr arg : args) {
//            if (arg.getName().equalsIgnoreCase(usedArg))
//                return arg;
//        }
//        throw new IllegalArgumentException("Cannot find argument descriptor :" + usedArg);
//    }
//
//    public static boolean isBindingExpressionChar(char c) {
//
//        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'z');
//    }
//
//}
