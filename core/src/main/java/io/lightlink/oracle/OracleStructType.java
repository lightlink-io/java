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
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.map.CaseInsensitiveMap;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class OracleStructType extends AbstractOracleType {

    @Override
    public Object convertToJdbc(Connection connection, RunnerContext runnerContext, String name, Object value) throws IOException, SQLException {
        OracleConnection con = unwrap(connection);


        if (!(value instanceof Map)) { // create a Map from bean
            Map map = new CaseInsensitiveMap(new BeanMap(value));
            map.remove("class");
            value = map;
        }

        return createStruct(con, (Map) value, getCustomSQLTypeName());

    }

    @Override
    public Object readFromResultSet(ResultSet resultSet, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        throw new IllegalArgumentException("Obtaining STRUCT type from SQL is not implemented.");
    }

    @Override
    public Object readFromCallableStatement(CallableStatement cs, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {

        OracleCallableStatement ocs = cs.unwrap(OracleCallableStatement.class);
        return getMapFromStruct(unwrap(ocs.getConnection()), ocs.getSTRUCT(pos));
    }

    @Override
    public Integer getSQLType() {
        return OracleTypes.STRUCT;
    }


}
