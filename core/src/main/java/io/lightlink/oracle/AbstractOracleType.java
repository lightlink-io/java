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
import oracle.sql.ArrayDescriptor;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;
import org.apache.commons.collections.map.CaseInsensitiveMap;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private StructDescriptor safeCreateStructureDescriptor(String baseName, OracleConnection con) throws SQLException {
        StructDescriptor res = StructDescriptor.createDescriptor(baseName, con);
        if (res == null) {
            throw new IllegalArgumentException("Cannot create STRUCT with name:" + baseName);
        }
        return res;
    }

    protected ArrayDescriptor safeCreateArrayDescriptor(String arrayName, OracleConnection con) throws SQLException {
        ArrayDescriptor res = ArrayDescriptor.createDescriptor(arrayName, con);
        if (res == null) {
            throw new IllegalArgumentException("Cannot create ARRAY with name:" + arrayName);
        }
        return res;
    }

    protected STRUCT createStructFromMap(OracleConnection con, Map value, String type) throws SQLException {
        STRUCT struct;
        StructDescriptor structType = safeCreateStructureDescriptor(type, con);
        ResultSetMetaData stuctMeteData = structType.getMetaData();

        value = new CaseInsensitiveMap(value);

        List orderedValues = new ArrayList();

        for (int col = 1; col <= stuctMeteData.getColumnCount(); col++) {
            String columnName = stuctMeteData.getColumnName(col);
            Object v = value.get(columnName);
            if (v == null) {
                v = value.get(columnName.replaceAll("_", ""));
            }
            orderedValues.add(v);
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



    protected Map<String,Object> getMapFromStruct(OracleConnection con, Struct struct) throws SQLException {

        Object[] attributes = struct.getAttributes();
        HashMap<String, Object> res = new HashMap<String, Object>();

        StructDescriptor structType = safeCreateStructureDescriptor(struct.getSQLTypeName(), con);
        ResultSetMetaData stuctMeteData = structType.getMetaData();

        List orderedValues = new ArrayList();

        for (int col = 1; col <= stuctMeteData.getColumnCount(); col++) {
            String columnName = stuctMeteData.getColumnName(col);
            res.put(columnName,attributes[col-1]);

        }
        return res;
    }


}
