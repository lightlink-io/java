run({firstName: "Ste*"}, function (res) {


    assertRunSuccess(res);
    assertNotNull("resultSet", res.resultSet);
    assertEquals("resultSet length", res.resultSet.length, 3);
});

res = run({firstName: "Janette", lastName: "King"}, function (res) {


    assertEquals("resultSet length", res.resultSet.length, 1);
    assertEquals("FIRST_NAME", res.resultSet[0].FIRST_NAME, "Janette");
});



