# LightLink.io

See: http://lightlink.io/

# Main concepts
LightLink is a server-side java framework to create data oriented REST/JSON services with a **MINIMUM** code.

It runs in the JVM (Java Virtual Machine) on the application server. Allows to be program REST services in JavaScript (executed by Nashorn JavaScript engine of JVM). Of course Java classes can be called from JavaScript if necesary. It's also common to integrate with frameworks like Spring, etc..

### Streaming
LightLink creates a **STREAM** data from the Database to WEB2.0 client, allowing the Web client to show the first page of resultset immediately, no need to wait for the whole resultSet to be loaded.

## First steps

What would be the absolute minimum code to create a REST service to query the database ? Well it's probably the SQL itself. Here it is: the code of your first service:

Create a file `/myPackage/getCountries.js.sql` in your classpath or in `/WEB-INF/lightlink/`
```SQL
SELECT ID,NAME,ISO_NAME FROM COUNTRIES
```
JSON Response will be 
```javascript
{
  "resultSet":[
     {"ID":1,"NAME":"JAPAN",ISO_NAME:"JP"},
     {"ID":2,"NAME":"ITALY",ISO_NAME:"IT"},
     ....
  ],
  "success":true
}
```

#### Well, you might need to pass parameters from the client. 

```SQL
SELECT * FROM COUNTRIES WHERE NAME=:p.name
```
here `p` is a JavaScript object containing the parsed JSON your client have sent in its request

**SQL Injection ?** Prevented by using JDBC Prepared Statement, that is translated to `SELECT * FROM COUNTRIES WHERE NAME=?` and a `preparedStatement.setString(1,value)`

#### Now you might need to make it a startsWith search using LIKE expression
```SQL
<%
    var expr = "%"+p.name;
%>
SELECT * FROM COUNTRIES WHERE LIKE :expr
```
Or one line expression syntax
```SQL
--% var expr = "%"+p.name;
SELECT * FROM COUNTRIES WHERE LIKE :expr
```

NOTE: --%  syntax is useful when you copy-paste the SQL to your SQL client for testing, it will consider --% as comments, allowing to concentrate on the SQL itself

#### Expressions
Let's move on. Now we would like our service to do the search only if the `name` parameter is present:

```SQL
--%  if (p.name){
--%     var expr = "%"+p.name;
SELECT * FROM COUNTRIES WHERE LIKE :expr
--%  }
```
Or like this
```SQL
--% if (!p.name) throw new Error("'name' parameter is missing");
--% var expr = "%"+p.name;
SELECT * FROM COUNTRIES WHERE LIKE :expr
```

The first code will return no data, the second will raise exception.

#### Dynamic SQL
But what if we want to have multiple input parameters and modify the search statement according to what parameters are present:
```SQL
SELECT * FROM employees WHERE 1=1
--% if (p.firstName) {
--%      var firstNameExpr = "%"+p.firstName;
         AND FIRST_NAME LIKE :firstNameExpr
--% }
--% if (p.lastName) {
--%      var lastNameExpr = "%"+p.lastName;
         AND LAST_NAME = :lastNameExpr
--% }
--% if (p.minHireDate) {
      AND HIRE_DATE >= :(date.dd/MM/yyyy)p.minHireDate
--% }
--% if (p.maxHireDate) {
      AND HIRE_DATE <= :(date.dd/MM/yyyy)p.maxHireDate
--% }
ORDER BY EMPLOYEE_ID
```
Here it will parse `minHireDate` and `maxHireDate` input parameter strings as using `dd/MM/yyyy` pattern, convert it into `java.sql.Timestamp` and pass it to the database as PreparedStatement's parameter


#### Dynamic SQL from data
This is quite a questionable example, but let's assume that columns list comes from the client. The first thing to do it to validate the parameter, white-listing allowed characters, then we can inline the value into our SQL expression. 
```sql
--% if (! p.columns.match(/^[a-z0-9_,\s]*$/i))
--%        throw "Illegal 'columns' value"+p.columns;
SELECT <%=p.columns%> FROM MY_TABLE
```

#### Ok, but how can I have multiple queries in a single service ? 
```SQL
Select * from countries
--% sql.query("countries");
Select * from client
--% sql.query("clients");
Select * from cities;
--% sql.query("cities");
```

#### Can I load the resultSet in memory for some additional processing ? 
```sql
SELECT * FROM EMPLOYEES
<% 
  var employees  = sql.queryToBuffer();
  var salarySum = 0;
  for (var i=0;i<employees.length;i++)
    salarySum+=employees[i].SALARY;
  var avgSalary = salarySum/employees.length;
  response.writeObject("employees",employees);
  response.writeObject("avgSalary", avgSalary);
%>
```
This will return the JSON like this
```javascript
{
   "employees":[{...},{...}],
   "avgSalary":...
   "success":true
}
```

#### Calling Java classes?
See: https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/nashorn/api.html for additional information

```javascript
response.writeObject("appServerIP", Java.type("java.net.Inet4Address").getLocalHost().getHostAddress());
``` 

#### Calling a bean from Spring context?
```javascript
var WebApplicationContextUtils = Java.type("org.springframework.web.context.support.WebApplicationContextUtils")
var context = WebApplicationContextUtils.getWebApplicationContext(env.getSession().getServletContext());
var myBean = spring.getBean("MyBean")
response.writeObject("someData", myBean.calculateSomething(p.param1,p.param2));
``` 

#### Updating 
```SQL
INSERT INTO JOB_HISTORY(EMPLOYEE_ID, START_DATE, END_DATE, JOB_ID, DEPARTMENT_ID)
    VALUES(100, :(date)p.from , :(date)p.to , :(number)p.jobId, :(number)p.deptId)
```
OR 
```SQL
INSERT INTO CITIES(name, country) values (:p.name, :p.country);
--% sql.query();

DELETE * FROM TEMP_RECORDS;
--% sql.query();
--% response.writeObject("deletedRecords",sql.getUpdateCount(1)); //optional
```
#### Batch update

Using JDBC batch API to group multiple queries into a single Java->DB call.
```SQL
--%    for (var i=0;i<p.dates.length;i++){
  INSERT INTO JOB_HISTORY(EMPLOYEE_ID, START_DATE, END_DATE, JOB_ID, DEPARTMENT_ID)
    VALUES(100, :(date)p.dates[i].from , :(date)p.dates[i].to , :(number)p.dates[i].jobId, :(number)p.dates[i].deptId)
--%        sql.addBatch();
--%    }
--%    sql.query();
```
