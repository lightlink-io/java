run({firstName: "Ste*"}, function (res) {

    assertRunSuccess(res);
    assertNotNull("resultSet", res.resultSet);
    assertEquals("resultSet length", res.resultSet.length, 3);

});

run({firstName: "Janette", lastName: "King"}, function (res) {

    assertEquals("resultSet length", res.resultSet.length, 1);
    assertEquals("FIRST_NAME", res.resultSet[0].FIRST_NAME, "Janette");
});


run({minHireDate: "2007/01/01"}, function (res) {

    assertEquals("resultSet length", res.resultSet.length, 30);
    assertEquals("FIRST_NAME", res.resultSet[0].FIRST_NAME, "Bruce");

});

