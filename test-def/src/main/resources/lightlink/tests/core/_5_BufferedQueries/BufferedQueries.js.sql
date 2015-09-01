
SELECT *
FROM EMPLOYEES
ORDER BY EMPLOYEE_ID

<%

  var employees  = sql.queryToBuffer();


  var salarySum = 0;

  for (var i=0;i<employees.length;i++)
    salarySum+=employees[i].SALARY;

  var avgSalary = salarySum/employees.length;

  for (var i=0;i<employees.length;i++)
    employees[i].SALARY_PERCENT = employees[i].SALARY*100/avgSalary;

  response.writeObject("buffered",employees);

%>

-- Use buffered query data as parameter to next query

SELECT * FROM EMPLOYEES
WHERE SALARY > :(number)avgSalary
--% sql.query("moreThenAverageSalary");