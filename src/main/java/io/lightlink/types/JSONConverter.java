package io.lightlink.types;

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
