var res = run({}, function (res) {


    assertNotNull("employees resultSet", res.employees);
    assertNotNull("jobs resultSet", res.jobs);

    assertEquals("employee records", res.employees.length, 107);
    assertEquals("job records", res.jobs.length, 19);

});
