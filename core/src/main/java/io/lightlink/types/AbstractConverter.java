package io.lightlink.types;

import io.lightlink.core.RunnerContext;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractConverter {

    public void setConfig(String config){
        // do nothing
    }

    public abstract Object convertToJdbc(Connection connection, RunnerContext runnerContext, String name, Object value) throws IOException, SQLException;

    public Object readFromResultSet(ResultSet resultSet, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        return resultSet.getObject(pos);
    }
    public Object readFromCallableStatement(CallableStatement cs, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {
        return cs.getObject(pos);
    }

    public abstract Integer getSQLType();

    public String getCustomSQLTypeName() {
        return null;
    }

}
