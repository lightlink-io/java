var clobData = "Hello CLOB";
var blobStringData = "HelloBlobData";

run({clobData: clobData, blobStringData: blobStringData}, function (res) {

    assertRunSuccess(res);
    assertNotNull("rsDefault", res.rsDefault);
    assertTrue("rsDefault length", res.rsDefault.length == 1);

    var bytes = [];

    for (var i = 0; i < blobStringData.length; ++i)
        bytes.push(blobStringData.charCodeAt(i));

    var strBase64 = Java.type("java.util.Base64").getEncoder().encodeToString(bytes);

    assertEquals("blob", res.rsDefault[0].blobField.length, 13);

    assertEquals("blob", res.rsBase64[0].blobField, strBase64);

    assertEquals("clob", res.rsBase64[0].clobField, clobData);

    assertEquals("blob", res.rsAsString[0].asString, blobStringData);

    assertEquals("clob", res.rsAsString[0].clobField, clobData);

});

