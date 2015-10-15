
DELETE FROM blobclob
--% sql.query();

INSERT INTO blobclob(name, blobField, clobField)
    VALUES('test', :(blob.UTF-8)p.blobStringData , :p.clobData )

--% sql.query();


SELECT blobField,clobField FROM blobclob

--% sql.query("rsDefault");

SELECT blobField as '(blob.base64)blobField', clobField FROM blobclob

--% sql.query("rsBase64");


SELECT blobField as '(blob.UTF-8)asString', clobField FROM blobclob

--% sql.query("rsAsString");
