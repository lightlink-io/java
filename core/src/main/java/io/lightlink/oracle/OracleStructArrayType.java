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


import io.lightlink.core.RunnerContext;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.STRUCT;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.map.CaseInsensitiveMap;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OracleStructArrayType extends AbstractOracleType {

    @Override
    public Object convertToJdbc(Connection connection, RunnerContext runnerContext, String name, Object value) throws IOException, SQLException {
        OracleConnection con = unwrap(connection);

        ArrayDescriptor arrayStructDesc = safeCreateArrayDescriptor(getCustomSQLTypeName(), con);

        if (value == null) {
            return null;
        } else if (value instanceof List || value.getClass().isArray()) {
            if (value.getClass().isArray()) {
                value = Arrays.asList((Object[]) value);
            }

            List records = (List) value;

            STRUCT[] structArray = new STRUCT[records.size()];

            for (int i = 0; i < structArray.length; i++) {
                Object record = records.get(i);

                if (!(record instanceof Map)) { // create a Map from bean
                    Map map = new CaseInsensitiveMap(new BeanMap(record));
                    map.remove("class");
                    record = map;
                }

                structArray[i] = createStructFromMap(con, (Map) record, arrayStructDesc.getBaseName());
            }

            return new ARRAY(arrayStructDesc, con, structArray);

        }  else {

            throw new IllegalArgumentException("Type " + getCustomSQLTypeName() + " of converter=" + this.getClass().getName() +
                    " accepts array/list of objects.");
        }
    }

    @Override
    public Object readFromResultSet(ResultSet resultSet, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        throw new IllegalArgumentException("Obtaining ARRAY of STRUCT type from SQL is not implemented.");
    }

    @Override
    public Object readFromCallableStatement(CallableStatement cs, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {

        Array array = cs.getArray(pos);
        return getDataFromArray(unwrap(cs.getConnection()),array);
    }

    private Object getDataFromArray(OracleConnection con, Array array) throws SQLException {
        if (array == null) {
            return null;
        } else {
            List<Map<String, Object>> list = new ArrayList<>();

            Object[] structs = (Object[]) array.getArray();

            for (Object struct : structs) {
                Struct structure = (Struct) struct;
                list.add(getMapFromStruct(con, structure));
            }
            return list;
        }
    }

    @Override
    public Integer getSQLType() {
        return OracleTypes.ARRAY;
    }


}
