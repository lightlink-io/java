run({}, function (res) {


        assertNotNull("employees resultSet", res.buffered);

        assertEquals("employee records", res.buffered.length, 107);

        assertNotNull("SALARY_PERCENT calculation ", res.buffered[0].SALARY_PERCENT);


        assertNotNull("moreThenAverageSalary resultSet", res.moreThenAverageSalary);
        assertTrue("moreThenAverageSalary records more then 10", res.moreThenAverageSalary.length > 10);
    }
)
;

