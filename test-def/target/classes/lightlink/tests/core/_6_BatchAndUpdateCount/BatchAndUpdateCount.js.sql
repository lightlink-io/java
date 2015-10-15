<%
sql.getConnection().setAutoCommit(false)

var dates = [];  // prepare sample data
var now  = new Date().getTime();
for (var i=0;i<100;i++){
    dates.push({
        from:new Date(now+1000*60*60*24*365*(i+1)-1000),
        to:new Date(now+1000*60*60*24*365*(i+2))
    });
}

for (var i=0;i<dates.length;i++){
%>
  INSERT INTO JOB_HISTORY(EMPLOYEE_ID, START_DATE, END_DATE, JOB_ID, DEPARTMENT_ID)
    VALUES(100, :(date)dates[i].from , :(date)dates[i].to , 'SA_MAN', 10)
<%
  sql.addBatch();
}
sql.query();

%>

delete from JOB_HISTORY where START_DATE>= :(date)now

<%
 sql.query(); // explicit sql.query() is required before accessing to sql.getUpdateCount()

 response.writeObject("deletedLines",sql.getUpdateCount());

 sql.getConnection().rollback();
%>