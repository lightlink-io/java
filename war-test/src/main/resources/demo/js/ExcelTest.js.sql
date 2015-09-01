<%

  var ExcelResponseStream = Java.type("io.lightlink.output.ExcelResponseStream")
  response.setFormat(new ExcelResponseStream("ExcelTest.xlsx","demo/js/ExcelTest.xlsx"));

  types.setCustomDatePattern("yyyy-MM-dd HH:mm:ss");
  
%>


SELECT EMPLOYEE_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, HIRE_DATE, SALARY, COMMISSION_PCT, JOB_ID, DEPARTMENT_ID
FROM employees
WHERE 1=1
--% if (p.firstName) {
--%    if (p.firstName.indexOf("*")!=-1) {
--%      p.firstName = p.firstName.replace(/\*/g,"%")  // replace '*' with '%' for like expression
         AND FIRST_NAME like :p.firstName
--%    } else {                             // if parameter without * the use strict equals for indexed search
         AND FIRST_NAME = :p.firstName
--%    }
--% }

--% if (p.lastName) {
--%    if (p.lastName.indexOf("*")!=-1) {
--%      p.lastName = p.lastName.replace(/\*/g,"%")  // replace '*' with '%' for like expression
         AND LAST_NAME like :p.lastName
--%    } else {                             // if parameter without * the use strict equals for indexed search
         AND LAST_NAME = :p.lastName
--%    }
--% }


--% if (p.minHireDate) {
      AND HIRE_DATE >= :(date)p.minHireDate
--% }
--% if (p.maxHireDate) {
      AND HIRE_DATE <= :(date)p.maxHireDate
--% }


<% if (p.minSalary) %>
  AND HIRE_DATE >= :(number)p.minSalary

--% if (p.maxSalary)
  AND HIRE_DATE <= :(number)p.maxSalary

ORDER BY EMPLOYEE_ID

<% 
    response.writeObject("headers",["ID","FIRST NAME","LAST NAME","EMAIL","PHONE NUMBER","HIRE DATE","SALARY","COMMISSION %","JOB ID","DEPARTMENT ID"]);
    response.writeObject("date",(new Date()).toString());
%>

