
SELECT * FROM employees ORDER BY EMPLOYEE_ID

<%
 sql.query("employeesHiddenPersonalData",function(row, index, rsName){
      // HIDE private information
      delete row.PHONE_NUMBER;
      row.EMAIL = row.EMAIL.substring(0,2)+row.EMAIL.substring(2).replace(/./g,"*");
      return row;
    });
%>

SELECT * FROM employees ORDER BY EMPLOYEE_ID

<%
 sql.query("employeesArray",function(row, index, rsName){
      return [ row.EMPLOYEE_ID, row.FIRST_NAME,
        row.LAST_NAME, row.EMAIL, row.PHONE_NUMBER,
        row.HIRE_DATE, row.JOB_ID, row.SALARY,
        row.COMMISSION_PCT ]; // return each row as an array instead of map
    });
%>


SELECT EMPLOYEE_ID FROM employees ORDER BY EMPLOYEE_ID

<%
 sql.query("employeesIds",function(row, index, rsName){
      return row.EMPLOYEE_ID // return only one numeric column per row
    });
%>

SELECT FIRST_NAME, LAST_NAME FROM employees ORDER BY EMPLOYEE_ID

<%
 sql.query("employeesNames",function(row, index, rsName){
      return row.FIRST_NAME+" "+row.LAST_NAME // return only one string column per row
    });
%>


SELECT HIRE_DATE FROM employees ORDER BY EMPLOYEE_ID

<%
 sql.query("employeesDates",function(row, index, rsName){
      return row.HIRE_DATE; // return only one date column per row
    });
%>



