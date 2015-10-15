run({ },
    function (res) {
        assertRunSuccess(res);
        assertNull("null out", res.outArr);
//        assertEquals("CLOB_DATA from forth sql", res.resultSet4[0].CLOB_DATA, "CLOB_DATA");
    });

run({ inArr:[1,2,3,4,5]},
    function (res) {
        assertRunSuccess(res);
        assertEquals("res.outArr must have 5 elements", res.outArr.length, 5);
        assertEquals("res.outArr must have 5 elements and the last = 5", res.outArr[4], 5);
    });

