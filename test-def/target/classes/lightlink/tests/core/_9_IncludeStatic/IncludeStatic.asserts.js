run({}, function (res) {

    assertRunSuccess(res);
    assertNotNull("resultSet", res.resultSet);
    assertTrue("resultSet length>10", res.resultSet.length > 10);

    assertEquals("testFunction", res.testFunction, "testFunction");
});

