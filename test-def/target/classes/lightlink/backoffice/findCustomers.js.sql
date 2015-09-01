/*  File : backend/insertCustomers.js.sql */
<%
for (var i=0;i<p.toCreate.length;i++) {
  var line = p.toInsert[i];
  %>
    INSERT INTO CUSTOMERS
      (ID, FIRST_NAME, LAST_NAME, CREATE_DATE)
    VALUES(SEQ_CUSTOMERS.nextval
      , :line.FIRST_NAME
      , :line.LAST_NAME
      , SYSDATE)
  <%
  sql.addBatch();
}
sql.query();
%>

var customers = [
  {FIRST_NAME:"Bill", LAST_NAME:"Smith"},
  {FIRST_NAME:"Alice", LAST_NAME:"Wonderland"},
  {FIRST_NAME:"John", LAST_NAME:"Malkovich"},
  /*....*/
];
backend.insertCustomers( {
      toCreate:customers
    }, function(){
      alert("Success");
    }
);

var minFrom = dates[dates.length-1].from;
%>

delete from JOB_HISTORY where START_DATE>=SYSDATE+ :(number)minFrom

<%
 sql.query(); // explicit sql.query() is required before accessing to sql.getUpdateCount()

 response.writeObject("deletedLines",sql.getUpdateCount());

 sql.getConnection().rollback();
%>