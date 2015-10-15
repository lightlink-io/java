SELECT
  e.EMPLOYEE_ID*1000+e2.EMPLOYEE_ID "id",

--% if ("PostgreSQL"==sql.getConnection().getMetaData().getDatabaseProductName()) {

  concat(e.FIRST_NAME ,' ',e2.LAST_NAME) "name",
  CASE WHEN (random()>0.5) THEN 'male' ELSE 'female' END "gender",
  ROUND(random()*45)+18 "age", e.*

--% } else {

  (e.FIRST_NAME || ' '|| e2.LAST_NAME) "name",
  (CASE WHEN (dbms_random.value()>0.5) THEN 'male' ELSE 'female' END) "gender",
  ROUND(dbms_random.value()*45)+18 "age", e.*

--% }

FROM employees e, employees e2
WHERE 1=1

--% if (p.firstName)
  AND e.FIRST_NAME = :p.firstName


--% if (p.lastName)
  AND e.LAST_NAME = :p.lastName


