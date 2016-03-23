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
import io.lightlink.types.AbstractConverter;
import oracle.jdbc.OracleTypes;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OracleCursorType extends AbstractConverter {

    @Override
    public Object convertToJdbc(Connection connection, RunnerContext runnerContext, String name, Object value) throws IOException, SQLException {

        throw new IllegalArgumentException("Cursor supported as stored procedure output parameters only ");

    }

    @Override
    public Object readFromResultSet(ResultSet resultSet, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        throw new IllegalArgumentException("Cursor supported as stored procedure output parameters only ");
    }


    @Override
    public Integer getSQLType() {
        return OracleTypes.CURSOR;
    }


}
