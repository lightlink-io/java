package io.lightlink.oracle;

import io.lightlink.core.RunnerContext;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.OracleTypes;
import oracle.sql.STRUCT;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.map.CaseInsensitiveMap;

import java.io.IOException;
import java.sql.*;
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

        return createStructFromMap(con, (Map) value, getCustomSQLTypeName());

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
