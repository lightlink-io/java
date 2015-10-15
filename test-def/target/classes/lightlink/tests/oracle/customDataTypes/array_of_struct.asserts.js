var persons = [
    { name: "John Smith", email: "john@smith.com" },
    { name: "James Bond", email: "james@bond.com" }
];

run({
        personsIn:persons,
        inArr:[1,2,3,4,5]},
    function (res) {
        assertRunSuccess(res);
        assertEquals("res.outArr must have 5 elements", res.outArr.length, 5);
        assertEquals("res.outArr must have 5 elements and the last = 5", res.outArr[4], 5);

        assertEquals("name must be equals", res.personsOut[0].NAME,persons[0].name);
        assertEquals("email must match", res.personsOut[0].EMAIL,persons[0].email);

        assertEquals("name must be equals", res.personsOut[1].NAME,persons[1].name);
        assertEquals("email must match", res.personsOut[1].EMAIL,persons[1].email);

    });



