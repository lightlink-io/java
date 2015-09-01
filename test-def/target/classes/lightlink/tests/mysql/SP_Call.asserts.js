run(
    {
        date: new Date(),
        number: 123,
        name: "LightLink"
    },

    function (res) {

        assertRunSuccess(res);

        assertEquals("helloString", res.helloString, "Hello LightLink");
        assertEquals("Number", res.number, 124);

    });

