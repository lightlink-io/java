package io.lightlink.config;

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


import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class SpringTestManual extends TestCase {

    public static class Employee{
        String jobId,firstName,lastName,email;
        BigDecimal employeeId,managerId, departmentId;
        BigDecimal commissionPct, salary;
        Date hireDate;

        public String getJobId() { return jobId; }
        public void setJobId(String jobId) { this.jobId = jobId; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public BigDecimal getEmployeeId() { return employeeId; }
        public void setEmployeeId(BigDecimal employeeId) { this.employeeId = employeeId; }
        public BigDecimal getSalary() { return salary; }
        public void setSalary(BigDecimal salary) { this.salary = salary; }
        public BigDecimal getManagerId() { return managerId; }
        public void setManagerId(BigDecimal managerId) { this.managerId = managerId; }
        public BigDecimal getDepartmentId() { return departmentId; }
        public void setDepartmentId(BigDecimal departmentId) { this.departmentId = departmentId; }
        public BigDecimal getCommissionPct() { return commissionPct; }
        public void setCommissionPct(BigDecimal commissionPct) { this.commissionPct = commissionPct; }
        public Date getHireDate() { return hireDate; }
        public void setHireDate(Date hireDate) { this.hireDate = hireDate; }
    }


    public void test() throws Exception{
        ObjectMapper mapper = new ObjectMapper();

        new oracle.jdbc.OracleDriver();
        SingleConnectionDataSource dataSource =
                new SingleConnectionDataSource("jdbc:oracle:thin:@127.0.0.1:1521/XE", "system", "password", false);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

//        String sql = "select e.* from hr.EMPLOYEES e";
        String sql = "select e.* from hr.EMPLOYEES e, hr.EMPLOYEES e1--, hr.regions r, hr.regions r2";
        jdbcTemplate.setFetchSize(100);
        List<Employee> res = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Employee.class));

        long l = System.currentTimeMillis();
        dataSource =
                new SingleConnectionDataSource("jdbc:oracle:thin:@127.0.0.1:1521/XE", "system", "password", false);
        jdbcTemplate = new JdbcTemplate(dataSource);

        res = jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Employee.class));

        System.out.println(mapper.writeValueAsString(res).length()/1000000f+"Mb");
        System.out.println((System.currentTimeMillis()-l)/1000F);
//        System.out.println(mapper.writeValueAsString(res));

        int x=0;
    }


}
