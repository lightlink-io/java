package io.lightlink.oracle;

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


import io.lightlink.types.AbstractConverter;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.map.CaseInsensitiveMap;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public abstract class AbstractOracleType extends AbstractConverter {
    private String typeName;

    private List structColumnList = new ArrayList();

    @Override
    public void setConfig(String config) {
        typeName = config;
    }

    protected OracleConnection unwrap(Connection con) throws SQLException {
        if (con instanceof OracleConnection)
            return (OracleConnection) con;
        else
            return con.unwrap(OracleConnection.class);
    }

    @Override
    public String getCustomSQLTypeName() {
        return typeName;
    }

    protected Map<String,Object> getMapFromStruct(OracleConnection con, Struct struct) throws SQLException {

        Object[] attributes = struct.getAttributes();
        HashMap<String, Object> res = new HashMap<String, Object>();

        StructDescriptor structType = safeCreateStructureDescriptor(struct.getSQLTypeName(), con);
        ResultSetMetaData stuctMeteData = structType.getMetaData();

        for (int col = 1; col <= stuctMeteData.getColumnCount(); col++) {
            String columnName = stuctMeteData.getColumnName(col);
            res.put(columnName,attributes[col-1]);

        }
        return res;
    }


    protected ARRAY createArray(Connection con, Object value, String typeName) throws SQLException {

        if (value==null)
            return null;

        ArrayDescriptor arrayStructDesc = safeCreateArrayDescriptor(typeName, con);

        if (value == null)
            return null;

        if (value.getClass().isArray()) {
            value = Arrays.asList((Object[]) value);
        }

        List records = (List) value;
        String baseName = arrayStructDesc.getBaseName();
        int baseType = arrayStructDesc.getBaseType();
        if (baseType == Types.VARCHAR
                || baseType == Types.CHAR
                || baseType == Types.CLOB
                || baseType == Types.NUMERIC
                || baseType == Types.INTEGER
                || baseType == Types.BIGINT
                || baseType == Types.FLOAT
                || baseType == Types.DOUBLE
                || baseType == Types.DECIMAL
                || baseType == Types.NCHAR
                || baseType == Types.NVARCHAR
                || baseType == Types.NCLOB
                ) {
            return new ARRAY(arrayStructDesc, con, records.toArray());    // primitive array
        } else {

            Object[] structArray = new Object[records.size()];

            for (int i = 0; i < structArray.length; i++) {
                Object record = records.get(i);

                if (baseType == OracleTypes.JAVA_STRUCT || baseType == OracleTypes.JAVA_OBJECT
                        || baseType == OracleTypes.STRUCT || baseType == OracleTypes.JAVA_STRUCT) {
                    record = createStruct(con, record, baseName);
                } else if (baseType == OracleTypes.ARRAY){
                    record = createArray(con, record, baseName);
                }
                structArray[i] = record;
            }

            return new ARRAY(arrayStructDesc, con, structArray);
        }
    }

    protected STRUCT createStruct(Connection con, Object value, String type) throws SQLException {

        if (value==null)
            return null;

        Map mapValue;
        if (value instanceof Map) {
            mapValue = (Map) value;
            mapValue = new CaseInsensitiveMap(mapValue);
        } else { // create a Map from bean
            Map map = new CaseInsensitiveMap(new BeanMap(value));
            map.remove("class");
            mapValue = map;
        }

        STRUCT struct;
        StructDescriptor structType = safeCreateStructureDescriptor(type, con);
        ResultSetMetaData stuctMeteData = structType.getMetaData();


        List<Object> orderedValues = new ArrayList<Object>();

        if (stuctMeteData.getColumnCount() == 1 && mapValue.size() == 1) {
            orderedValues.add(mapValue.values().iterator().next());
        } else {
            for (int col = 1; col <= stuctMeteData.getColumnCount(); col++) {
                Object v = mapValue.get(stuctMeteData.getColumnName(col));
                if (v == null) {
                    v = mapValue.get(stuctMeteData.getColumnName(col).replaceAll("_", ""));
                }

                String typeName = stuctMeteData.getColumnTypeName(col);
                int columnType = stuctMeteData.getColumnType(col);
                if (columnType == OracleTypes.ARRAY) {
                    v = createArray(con, v, typeName);
                } else if (columnType == OracleTypes.JAVA_STRUCT || columnType == OracleTypes.JAVA_OBJECT
                        || columnType == OracleTypes.STRUCT ) {
                    v = createStruct(con, v, typeName);
                }

                orderedValues.add(v);
            }
        }

        Object[] values = orderedValues.toArray();

        for (int j = 0; j < values.length; j++) {

            Object v = values[j];
            if (v instanceof Long && stuctMeteData.getColumnTypeName(j + 1).equalsIgnoreCase("TIMESTAMP")) {
                values[j] = new Timestamp((Long) v);
            } else if (v instanceof Long && stuctMeteData.getColumnTypeName(j + 1).equalsIgnoreCase("DATE")) {
                values[j] = new Date((Long) v);
            }

        }

        struct = new STRUCT(structType, con, values);

        return struct;
    }

    protected StructDescriptor safeCreateStructureDescriptor(String baseName, Connection con) throws SQLException {
        StructDescriptor res = StructDescriptor.createDescriptor(baseName, con);
        if (res == null) {
            throw new IllegalArgumentException("Cannot create STRUCT with name:" + baseName);
        }
        return res;
    }

    protected ArrayDescriptor safeCreateArrayDescriptor(String arrayName, Connection con) throws SQLException {
        ArrayDescriptor res = ArrayDescriptor.createDescriptor(arrayName, con);
        if (res == null) {
            throw new IllegalArgumentException("Cannot create ARRAY with name:" + arrayName);
        }
        return res;
    }


}
