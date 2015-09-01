package io.lightlink.types;

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
        return resultSet.getArray(pos).getArray();
    }

    @Override
    public Object readFromCallableStatement(CallableStatement cs, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        return cs.getArray(pos).getArray();
    }

    @Override
    public Integer getSQLType() {
        return Types.ARRAY;
    }

}
