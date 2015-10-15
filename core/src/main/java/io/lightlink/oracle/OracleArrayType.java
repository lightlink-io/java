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

import java.io.IOException;
import java.sql.*;
import java.util.List;

public class OracleArrayType extends AbstractOracleType {

    @Override
    public Object convertToJdbc(Connection connection, RunnerContext runnerContext, String name, Object value) throws IOException, SQLException {
        OracleConnection con = unwrap(connection);
        ArrayDescriptor arrayStructDesc = safeCreateArrayDescriptor(getCustomSQLTypeName(), con);
        if (value == null) {
            return null;
        } else if (value instanceof List) {
            return new ARRAY(arrayStructDesc, con, ((List) value).toArray());
        } else {
            throw new IllegalArgumentException("Type " + getCustomSQLTypeName() + " of converter=" + this.getClass().getName() +
                    " accepts array/list of values.");
        }
    }

    @Override
    public Object readFromResultSet(ResultSet resultSet, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        Array array = resultSet.getArray(pos);
        return array!=null?array.getArray():null;
    }

    @Override
    public Object readFromCallableStatement(CallableStatement cs, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        Array array = cs.getArray(pos);
        return array!=null?array.getArray():null;
    }

    @Override
    public Integer getSQLType() {
        return OracleTypes.ARRAY;
    }


}
