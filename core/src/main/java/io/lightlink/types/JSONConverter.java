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
import org.json.simple.JSONValue;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.sql.*;

public class JSONConverter extends AbstractConverter {

    static JSONConverter instance  = new JSONConverter();

    public static JSONConverter getInstance() {
        return instance;
    }


    private JSONConverter() {
    }

    @Override
    public Object convertToJdbc(Connection connection, RunnerContext runnerContext, String name, Object value) throws IOException, SQLException {

        if (value==null)
            return null;

        String dbProduct = connection.getMetaData().getDatabaseProductName();
        if ("PostgreSQL".equalsIgnoreCase(dbProduct)){
            PGobject jsonObject = new PGobject();
            jsonObject.setType("json");
            jsonObject.setValue(value.toString());
            return jsonObject;
        } else
            return JSONValue.toJSONString(value);

    }

    @Override
    public Object readFromResultSet(ResultSet resultSet, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        String string = resultSet.getString(pos);
        return string==null?null:JSONValue.parse(string);
    }

    @Override
    public Object readFromCallableStatement(CallableStatement cs, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        String string = cs.getString(pos);
        return string==null?null:JSONValue.parse(string);
    }

    @Override
    public Integer getSQLType() {
        return null;
    }

}
