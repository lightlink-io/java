package io.lightlink.types;

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
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.sql.*;
import java.util.List;

public class ArrayConverter extends AbstractConverter {


    public static AbstractConverter getInstance() {

        return new ArrayConverter();
    }

    private String type;

    @Override
    public void setConfig(String config) {
        type = config;
    }

    @Override
    public Object convertToJdbc(Connection connection, RunnerContext runnerContext, String name, Object value) throws IOException, SQLException {

        if (value == null)
            return null;

        if (value instanceof List)
            value = ((List) value).toArray();
        else if (value instanceof String) {
            value = StringUtils.isBlank((String) value) ? new Object[0] : new Object[]{value};
        } else if (value instanceof Number) {
            value = new Object[]{value};
        }
        return connection.createArrayOf(type, (Object[]) value);

    }

    @Override
    public Object readFromResultSet(ResultSet resultSet, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        Array array = resultSet.getArray(pos);
        return array == null ? null : array.getArray();
    }

    @Override
    public Object readFromCallableStatement(CallableStatement cs, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        Array array = cs.getArray(pos);
        return array == null ? null : array.getArray();
    }

    @Override
    public Integer getSQLType() {
        return Types.ARRAY;
    }

}
