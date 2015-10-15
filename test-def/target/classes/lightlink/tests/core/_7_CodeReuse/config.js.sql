

--% function batchInsertJobHistory(employeeId, from,to, jobCode){

  INSERT INTO JOB_HISTORY(EMPLOYEE_ID, START_DATE, END_DATE, JOB_ID, DEPARTMENT_ID)
    VALUES(:employeeId, :(date)from , :(date)to , :jobCode, 10)

--%   sql.addBatch();
--% }
