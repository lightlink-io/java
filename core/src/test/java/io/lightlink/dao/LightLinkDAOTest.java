package io.lightlink.dao;

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


import junit.framework.TestCase;

import java.io.IOException;
import java.util.*;

public class LightLinkDAOTest extends TestCase {

    public void test() {
        LightLinkDAO dao = new LightLinkDAO("", "") {

            private Map<String, Object> employeeLine(String firstName, String lastName, String email, Date hireDate, String jobTitle,
                                                     String manFirstName, String manLastName, String manEmail, Date manHireDate, String manJobTitle) {

                HashMap<String, Object> res = new HashMap<String, Object>();

                res.put("firstName", firstName);
                res.put("lastName", lastName);
                res.put("email", email);
                res.put("hireDate", hireDate);
                res.put("job.jobTitle", jobTitle);

                res.put("manager.firstName", manFirstName);
                res.put("manager.lastName", manLastName);
                res.put("manager.email", manEmail);
                res.put("manager.hireDate", manHireDate);
                res.put("manager.job.jobTitle", manJobTitle);

                return res;
            }

            @SuppressWarnings("deprecation")
            @Override
            protected Map<String, Object> doExecute(Object params) throws IOException {
                HashMap<String, Object> res = new HashMap<String, Object>();

                ArrayList<Map<String, Object>> rs = new ArrayList<Map<String, Object>>();
                res.put("resultSet", rs);

                rs.add(employeeLine("Bill", "Gates", "billgates@gmail.com", new Date(1980, 01, 01),
                        "big boss", null, null, null, null, null));

                rs.add(employeeLine("John", "Smith", "johnsmith@gmail.com", new Date(1985, 01, 01),
                        "small boss", "Bill", "Gates", "billgates@gmail.com", new Date(1980, 01, 01), "big boss"));

                rs.add(employeeLine("Jimmy", "Jones", "jimmyjones@gmail.com", new Date(1990, 01, 01),
                        "small boss", "Bill", "Gates", "billgates@gmail.com", new Date(1980, 01, 01), "big boss"));

                rs.add(employeeLine("Peter", "Norton", "peternorton@gmail.com", new Date(1995, 01, 01),
                        "programmer", "Jimmy", "Jones", "jimmyjones@gmail.com", new Date(1990, 01, 01), "small boss"));

                rs.add(employeeLine("Strange", "Entry", "strange.entry@gmail.com", new Date(1995, 01, 01),
                        "programmer", "Jimmy", "Jones", "jimmyjones@gmail.com", new Date(1990, 01, 01), "wrong job name"));

                return res;
            }
        };

        List<Employee> employees = dao.queryForList(new Object(), Employee.class);

        assertTrue("Reused Manager object between John and Jimmy"
                , employees.get(1).getManager() == employees.get(2).getManager());

        assertTrue("Reused Job object between John and Jimmy"
                , employees.get(1).getJob() == employees.get(2).getJob());

        assertFalse("Not reused Manager object between Peter and Strange because manager's job name is different"
                , employees.get(3).getManager() == employees.get(4).getManager());

        assertFalse("Not reused Bill record and John's Manager object because levels are different"
                , employees.get(0) == employees.get(1).getManager());

    }


    public void test1() {
        LightLinkDAO dao = new LightLinkDAO("", "") {

            private Map<String, Object> employeeLine(String departmentName, String firstName, String lastName, String email, Date hireDate, String jobTitle,
                                                     String manFirstName, String manLastName, String manEmail, Date manHireDate, String manJobTitle) {

                HashMap<String, Object> res = new HashMap<String, Object>();

                res.put("departmentName", departmentName);

                res.put("employees.firstName", firstName);
                res.put("employees.lastName", lastName);
                res.put("employees.email", email);
                res.put("employees.hireDate", hireDate);
                res.put("employees.job.jobTitle", jobTitle);

                res.put("manager.firstName", manFirstName);
                res.put("manager.lastName", manLastName);
                res.put("manager.email", manEmail);
                res.put("manager.hireDate", manHireDate);
                res.put("manager.job.jobTitle", manJobTitle);

                return res;
            }

            @SuppressWarnings("deprecation")
            @Override
            protected Map<String, Object> doExecute(Object params) throws IOException {
                HashMap<String, Object> res = new HashMap<String, Object>();

                ArrayList<Map<String, Object>> rs = new ArrayList<Map<String, Object>>();
                res.put("resultSet", rs);


                rs.add(employeeLine("direction", "John", "Smith", "johnsmith@gmail.com", new Date(1985, 01, 01),
                        "small boss", "Bill", "Gates", "billgates@gmail.com", new Date(1980, 01, 01), "big boss"));

                rs.add(employeeLine("direction", "Jimmy", "Jones", "jimmyjones@gmail.com", new Date(1990, 01, 01),
                        "small boss", "Bill", "Gates", "billgates@gmail.com", new Date(1980, 01, 01), "big boss"));

                rs.add(employeeLine("department1", "Peter", "Norton", "peternorton@gmail.com", new Date(1995, 01, 01),
                        "programmer", "Jimmy", "Jones", "jimmyjones@gmail.com", new Date(1990, 01, 01), "small boss"));

                rs.add(employeeLine("department2", "First", "Entry", "strange.entry@gmail.com", new Date(1995, 01, 01),
                        "programmer", "Jimmy", "Jones", "jimmyjones@gmail.com", new Date(1990, 01, 01), "programmer"));

                rs.add(employeeLine("department2", "Second", "Entry", "strange.entry@gmail.com", new Date(1995, 01, 01),
                        "programmer", "Jimmy", "Jones", "jimmyjones@gmail.com", new Date(1990, 01, 01), "programmer"));

                return res;
            }
        };

        List<Department> departments = dao.queryForList(new Object(), Department.class);

        assertEquals("Distinct departments", 3, departments.size());
        assertEquals("direction departments employees", 2, departments.get(0).getEmployees().size());
        assertEquals("department1 departments employees", 1, departments.get(1).getEmployees().size());
        assertEquals("department2 departments employees", 2, departments.get(2).getEmployees().size());

        assertTrue("Job object shared between department2 employees"
                , departments.get(2).getEmployees().get(1).getJob() == departments.get(2).getEmployees().get(0).getJob());

        int x = 0;

    }


}
