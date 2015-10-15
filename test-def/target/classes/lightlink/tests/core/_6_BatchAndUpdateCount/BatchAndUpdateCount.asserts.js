run({}, function (res) {

    assertRunSuccess(res);

    assertEquals("deletedLines", res.deletedLines, 100);
});
