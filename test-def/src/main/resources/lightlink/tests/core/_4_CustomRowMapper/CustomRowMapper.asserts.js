run({}, function (res) {

    assertRunSuccess(res);

    assertNotNull("employeesHiddenPersonalData resultSet", res.employeesHiddenPersonalData);
    assertEquals("employeesHiddenPersonalData length", res.employeesHiddenPersonalData.length, 107);
    assertEquals("email of first line", res.employeesHiddenPersonalData[0].EMAIL, "SK***");
    assertNull("PHONE_NUMBER of first line", res.employeesHiddenPersonalData[0].PHONE_NUMBER);

    assertNotNull("employeesArray resultSet", res.employeesArray);
    assertEquals("employeesArray length", res.employeesArray.length, 107);
    assertEquals("firstName of first line", res.employeesArray[0][1], "Steven");

    assertNotNull("employeesArray resultSet", res.employeesIds);
    assertEquals("employeesArray length", res.employeesIds.length, 107);
    assertEquals("firstName of first line", res.employeesIds[0], 100);

    assertNotNull("employeesArray resultSet", res.employeesDates);
    assertEquals("employeesArray length", res.employeesDates.length, 107);
    assertEquals("firstName of first line", res.employeesDates[0], "2003-06-17T00:00:00");

    assertNotNull("employeesArray resultSet", res.employeesNames);
    assertEquals("employeesArray length", res.employeesNames.length, 107);
    assertEquals("firstName of first line", res.employeesNames[0], "Steven King");


});





