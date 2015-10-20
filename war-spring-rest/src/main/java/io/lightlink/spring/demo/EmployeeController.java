package io.lightlink.spring.demo;


import io.lightlink.output.ResponseStream;
import io.lightlink.spring.LightLinkFilter;
import io.lightlink.spring.StreamingMapper;
import io.lightlink.sql.SQLHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/data")
public class EmployeeController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @RequestMapping("/loadNames")
    public List<Employee> getPersonDetailAsSpringREST() {

        String sql = getSQL();

        List<Employee> res = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Employee.class));
        return res;
    }


    @RequestMapping("/employeesStreamWithMapping")
    public void getPersonDetailAsStream() {

        ResponseStream responseStream = LightLinkFilter.getCurrentResponseStream();
        responseStream.writePropertyArrayStart("resultSet");  // write opening JSON array declaration : "resultSet":[

        jdbcTemplate.query(getSQL(), new StreamingMapper(responseStream, BeanPropertyRowMapper.newInstance(Employee.class)));

        responseStream.writePropertyArrayEnd();     // write closing array bracket : ]
                                                    // LightLinkFilter will complete the JSON response on method exit

    }

    @RequestMapping("/getPersonDetailAsStreamInPlainJDBC")
    public void getPersonDetailAsStreamInPlainJDBC() throws SQLException {

        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            ResultSet rs = connection.createStatement().executeQuery(getSQL());

            ResponseStream responseStream = LightLinkFilter.getCurrentResponseStream();
            responseStream.writePropertyArrayStart("resultSet");// write opening JSON array declaration : "resultSet":[

            while(rs.next()){
                responseStream.writeObjectStart();
                responseStream.writeProperty("id", rs.getString("id"));
                responseStream.writeProperty("gender",rs.getString("gender"));
                responseStream.writeProperty("name",rs.getString("name"));
                responseStream.writeProperty("age",rs.getInt("age"));
                // ....
                responseStream.writeObjectEnd();

            }

            responseStream.writePropertyArrayEnd();             // write closing array bracket : ]

        } finally {
            if (connection != null)
                connection.close();
        }

    }

    @RequestMapping("/employeesStreamWithoutMapping")
    public void getPersonDetailAsStreamWithoutMapping() {

        ResponseStream responseStream = LightLinkFilter.getCurrentResponseStream();
        responseStream.writePropertyArrayStart("resultSet");  // write opening JSON array declaration : "resultSet":[

        jdbcTemplate.query(getSQL(), new StreamingMapper(responseStream));

        responseStream.writePropertyArrayEnd();               // write closing array bracket : ]

    }

    private String getSQL() {
        boolean pgsql;

        try {
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            pgsql = "PostgreSQL".equals(connection.getMetaData().getDatabaseProductName());
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }


        String pgsqlColumns = "concat(e.FIRST_NAME ,' ',e2.LAST_NAME) \"name\", \n" +
                "CASE WHEN (random()>0.5) THEN 'male' ELSE 'female' END \"gender\",\n" +
                "ROUND(random()*45)+18 \"age\", e.* \n";

        String oracleColumn = "(e.FIRST_NAME || ' '|| e2.LAST_NAME) \"name\",\n" +
                "  (CASE WHEN (dbms_random.value()>0.5) THEN 'male' ELSE 'female' END) \"gender\",\n" +
                "  ROUND(dbms_random.value()*45)+18 \"age\", e.* \n";

        return "SELECT \n" +
                "e.EMPLOYEE_ID*1000+e2. EMPLOYEE_ID \"id\", \n" +
                (pgsql ? pgsqlColumns : oracleColumn) +
                "FROM employees e, employees e2\n" +
                "WHERE 1=1";
    }

}