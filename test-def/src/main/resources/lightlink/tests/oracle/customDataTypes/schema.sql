
CREATE TYPE MY_INT_ARRAY IS VARRAY(100) OF INTEGER;


CREATE TYPE PERSON AS OBJECT  (
    name    VARCHAR(30),
    email   VARCHAR(30)
);


CREATE TYPE PERSON_ARRAY AS TABLE OF PERSON;

--------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE "DBRIDGE"."TEST_NUM_ARR"(
  intArrayIn IN MY_INT_ARRAY,
  intArrayOut OUT MY_INT_ARRAY
 ) IS
BEGIN
  intArrayOut := intArrayIn ;
END;

--------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE "TEST_STRUCT"
(
personIn IN PERSON,
personOut OUT PERSON
 )
  IS
BEGIN
  personOut := personIn ;
END;
--------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE "TEST_TYPES"
(
personsIn IN PERSON_ARRAY,
personsOut OUT PERSON_ARRAY,
intArrayIn IN MY_INT_ARRAY,
intArrayOut IN MY_INT_ARRAY
 )
  IS
BEGIN
  personsOut := personsIn ;
  intArrayOut := intArrayIn ;
END;



