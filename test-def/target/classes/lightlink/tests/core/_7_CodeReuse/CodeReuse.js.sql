

delete from JOB_HISTORY
<%  sql.query();


  batchInsertJobHistory(100, new Date(0),new Date(), 'SA_MAN');
  batchInsertJobHistory(101, new Date(0),new Date(), 'SA_MAN');
  batchInsertJobHistory(102, new Date(0),new Date(), 'SA_MAN');
  batchInsertJobHistory(103, new Date(0),new Date(), 'SA_MAN');
  sql.query();

%>
select count(*) as CNT from JOB_HISTORY

