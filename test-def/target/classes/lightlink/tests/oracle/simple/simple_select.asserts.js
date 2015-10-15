run(
    {
        jsonData: {
            a: 1,
            b: 2,
            c: {
                ca: "ca",
                cb: "cb"
            }
        }
    },

    function (res) {

        assertRunSuccess(res);

        assertEquals("1 from first sql", res.resultSet[0].ONE, 1);

        assertEquals("2 from second sql", res.resultSet2[0].TWO, 2);

        assertEquals("json a=1 from third sql", res.resultSet3[0].jsonOut.a, 1);
        assertEquals("json c.ca = 'ca' from third sql", res.resultSet3[0].jsonOut.c.ca, "ca");

        assertEquals("CLOB_DATA from forth sql", res.resultSet4[0].CLOB_DATA, "CLOB_DATA");

    });

