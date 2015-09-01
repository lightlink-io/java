package io.lightlink.oracle;

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
