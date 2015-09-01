-- SELECT * FROM regions
-- % sql.query("regions");

-- select * from EMPLOYEES
-- % sql.query("employees");

select
e.EMPLOYEE_ID as employeeId,
e.FIRST_NAME as firstName,
e.LAST_NAME as lastName,
e.EMAIL as email,
e.PHONE_NUMBER as phoneDate,
e.HIRE_DATE as hireDate,
e.JOB_ID as jobId,
e.SALARY as salary,
e.COMMISSION_PCT as commissionPct,
e.MANAGER_ID as managerId,
e.DEPARTMENT_ID as departmentId

from EMPLOYEES e, EMPLOYEES e1 -- , regions r, regions r2

