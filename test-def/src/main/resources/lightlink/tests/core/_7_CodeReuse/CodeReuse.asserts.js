run({}, function (res) {

    assertRunSuccess(res);

    assertEquals("lines count in resultSet ", res.resultSet[0].CNT, 4);
});
