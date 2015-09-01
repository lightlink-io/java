var person = {
    NAME: "John Smith", EMAIL: "john@smith.com"
};

run({ personIn: person},
    function (res) {
        assertRunSuccess(res);
        assertEquals("name must be equals", res.personOut.NAME,person.NAME);
        assertEquals("email must match", res.personOut.EMAIL,person.EMAIL);

    });

