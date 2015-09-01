run({},function(res){
    assertNotNull("resultSet in response",res.resultSet);

    assertEquals("resultSet records",res.resultSet.length,107);

    assertNotNull("FIRST_NAME column ",res.resultSet[0].FIRST_NAME);

    assertEquals("HIRE_DATE", res.resultSet[0].HIRE_DATE, "2003-06-17T00:00:00");

});




