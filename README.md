# Main concepts
LightLink is a server-side java framework to create data oriented REST/JSON services with a **MINIMUM** code.

It runs in the JVM (Java Virtual Machine) on the application server. Allows to program REST services in JavaScript (executed by Nashorn JavaScript engine of JVM). Of course Java classes can be called from JavaScript if necesary. It's also common to integrate with frameworks like Spring, etc..

### Streaming
LightLink **STREAMS** data from the Database to WEB2.0 client, allowing the Web client to show the first page of resultset immediately, no need to wait for the whole resultSet to be loaded.
See lightlink NPM package for API : https://github.com/lightlink-io/npm

## Installation

pom.xml:
```xml
        <dependency>
            <groupId>io.lightlink</groupId>
            <artifactId>lightlink-core</artifactId>
            <version>1.2</version>  <!-- at the moment of writing -->
        </dependency>
```

/src/main/webapp/WEB-INF/web.xml:
```xml
    <servlet>
        <servlet-name>JsProxyServlet</servlet-name>
        <servlet-class>io.lightlink.servlet.JsProxyServlet</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>JsProxyServlet</servlet-name>
        <url-pattern>/lightlink/*</url-pattern>
    </servlet-mapping>
```
/src/main/webapp/WEB-INF/lightlink/config.js:
```js
sql.setDataSourceJndi("java:comp/env/jdbc/MainDS"); // assuming that you have jdbc/MainDS configured in you app server
//or, only for DEV: sql.setConnection("my.jdbc.driver.class.name","my.jdbc.url","login","password"); 
```

## First step

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
--% if (p.minHireDate) {   // assuming that minHireDate and maxHireDate parameters dd/MM/yyyy strings
      AND HIRE_DATE >= :(date.dd/MM/yyyy)p.minHireDate 
--% }
--% if (p.maxHireDate) {
      AND HIRE_DATE <= :(date.dd/MM/yyyy)p.maxHireDate
--% }
ORDER BY EMPLOYEE_ID
```
Here it will parse `minHireDate` and `maxHireDate` input parameter strings using `dd/MM/yyyy` pattern, convert them into `java.sql.Timestamp` and pass it to the database as PreparedStatement's parameter


#### Dynamic SQL from data
This is quite a **questionable** example, but let's assume that columns list comes from the client. The first thing to do it to validate the parameter, white-listing allowed characters, then we can inline the value into our SQL expression. 
```sql
--% if (! p.columns.match(/^[a-z0-9_,\s]*$/i))
--%        throw "Illegal 'columns' value"+p.columns;
SELECT <%=p.columns%> FROM MY_TABLE
```
> **Inline values warning** : Without a proper check of allowed character set inlins scriptlets can leat to SQL Injection vulnerability. Use with caution. If values come from user input, always restrict allowed caracter.


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
Methods sql.addBatch(); and sql.query(); commands use JDBC Batch feature to send a number of statements as a single network call. Extremely usefull as a fast way to insert multiple (hundreds/thowsands of) lines.

### Bind casting

In order to explicitly indicate a different type of binding, use **bind casting**.

The default input binding type is `String`. 
This is useful when numeric or a date input parameter is passed as a string, which is a common 
situation as all HTML form elements values are always strings on the browser side.

Build-in casings are:

- (number) – to perform a BigDecimal parsing
- date.FORMAT) – String to java.sql.Timestamp conversion: See https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html for possible FORMAT letters
- (blob) – BLOB represented by a byte array. Rarely used because of its verbosity, blob.base64 is more efficient.
- (blob.base64) – BLOB represented as base64 encoded String
- (blob. UTF-8) – BLOB represented as UTF-8 encoded String
- (array) - Native DB Array type. Uses generic JDBC API to suport DB Array types. Example : PostgreSQL's array types
- (json) –  JSON data type. Mapped to a native JSON type if supported by RDBMS (PostgreSQL, Oracle,.. ) otherwise can be mapped to a BLOB, TEXT or VARCHAR. 
- (oArr)             – Oracle ARRAY of primitives, VARCHAR2 ou NUMBER
- (oStruct)         – Oracle STRUCT
- (oStructArr)   – Oracle ARRAY of STRUCT

#### Example:
```sql
INSERT INTO JOB_HISTORY(EMPLOYEE_ID, START_DATE, END_DATE, JOB_ID, DEPARTMENT_ID)
    VALUES(100, :(date.dd/MM/yyyy)p.from , :(date.dd/MM/yyyy)p.to , :(number)p.jobId, :(number)p.deptId)
```
 
### Output casting
Output casing is needed to adapting a value from resultSet or a stored procedure's out parameters to a desired form.

Numbers and Dates types are handled automatically (unless you'd like to specify a particular date formatting pattern), no casting is needed. Most frequently you’ll need  to cast output Blobs:

```sql
SELECT blobField as "(blob.base64)blobField" FROM blobclob
```
or
```sql
SELECT blobField as "(blob.UTF-8)asString", clobField FROM blobclob
```
or 
```sql
SELECT SYSDATE as "(date.yyyy-MM-dd HH:mm:ss)currentDbTime" FROM DUAL
```

### Stored procedures casting
Lightlink allows to indicate the IN/OUT/INOUT parameters of stored procedure call queries:
```sql
CALL TEST_SIMPLE_SP(
    :(inout)(date.dd/MM/yyyy)p.date ,
    :(inout)(number)p.number ,
    :p.name , -- default IN parameter of String type
    :(out)p.helloString
)
```
Multiple casings can be applied as a series of independent casting :(cast1)(cast2)variable

Output parameters are returned by the names of binding variables. If the binding starts with “p.”, those 2 characters are removed. So if we consider the previous example, its resulting object will be {helloString:” Hello LightLink”,number:123}

### Binding consistency
Any binding’s value is caught at a moment of bind execution.  It’s safe to change the variable that was bind after the execution. 

Example  :
```sql
--%    function insertValue(no){
        INSERT INTO NUMBERS (VALUE) values( :no );
--%    sql.addBatch();
--%    }

--%    for (var i=0;i<10;i++) insertValue(i);

--%    sql.query();
```

## Querying data

#### Default query
After the .js.sql file is executed an implicit query is initiated, sending to the database everything that was present in memory buffer.

If the query returned a result set, its name in the final JSON response will be “resultSet”

If the query retuned multiple result sets, their default names will be “resultSet”,”resultSet2”,” resultSet3”,… etc

```sql
Select * from countries
```

#### Explicit query
Alternatively it’s possible to execute the buffer as SQL statement explicitly :

```sql
Select * from countries
--% sql.query();
```
Same as implicit query, the result sets go to “resultSet”, “resultSet2”, “resultSet3”, etc..

#### Explicit resut set names

In order to indicate the resultSet property name in the final JSON response, we can pass a parameter to sql.query()

```sql
Select * from countries
--% sql.query("countries");
```
Or, in case of multiple result sets returned, a comma separated list of resultset names.
```sql
Select * from clinets;
Select * from orders;
Select * from cities;
--% sql.query("clients,orders,cities");  
```
Please note the last syntax is not supported by ORACLE, where only one sql statement can be executed par jdbc call. Therefore we ORACLE way of the last service would be:
```sql
Select * from clinets;
--% sql.query("clients");  
Select * from orders;
--% sql.query("orders");  
Select * from cities;
--% sql.query("cities");  
```


#### Update count

If the query updated one or many records, a script can find out the number of lines updated (actually the value returned depends on the database and its jdbc driver) for each statement like this:

```sql
DELETE * FROM TEMP_RECORDS;
--% response.writeObject("records",sql.getUpdateCount ());
```

for the first/unique update or
```sql
INSERT INTO CITIES(name, country) values (:p.name, :p.country);
DELETE * FROM TEMP_RECORDS;
--% response.writeObject("records",sql.getUpdateCount (1));

if multiple update statement were executed (with a stored procedure or multiple statements within one query if the DB allows this) where N is the number of the statement (starting with 0)

### Custom row mapper
 
In case there is a need to transform the returned resultset on  the fly, it’s possible to use custom row mappers:
```sql
SELECT * FROM employees ORDER BY EMPLOYEE_ID
<%
 sql.query("employeesHiddenPersonalData",function(row, index, rsName){
      row.EMAIL = row.EMAIL.substring(0,2)+row.EMAIL.substring(2).replace(/./g,"*");  // HIDE private information
      return row;
    });
%>
```
or
```sql
SELECT FIRST_NAME, LAST_NAME FROM employees ORDER BY EMPLOYEE_ID
<%
 sql.query("employeesNames",function(row, index, rsName){
      return row.FIRST_NAME+" "+row.LAST_NAME // performs a server-side operation, return only one string column per row
    });
%>
```
In the last case, the response will contain employeesNames `array of strings`


### Return additional data.
To send some additional objects as a JSON reply to the client along with or instead of SQL streaming, use

```js
response.writeObject("name", value);
```
One mote example: calling a static method of a Java class 

```js
response.writeObject("appServerIP", Java.type("java.net.Inet4Address").getLocalHost().getHostAddress());
```

### Query to memory (buffering mode)
Instead of streaming the result set directly to the client, it’s possible to buffer it in memory for eventual additional handling. Example :

```js
SELECT *
FROM EMPLOYEES
ORDER BY EMPLOYEE_ID
<% 
  var employees  = sql.queryToBuffer();
  var salarySum = 0;
  for (var i=0;i<employees.length;i++)
    salarySum+=employees[i].SALARY;
  var avgSalary = salarySum/employees.length;
  response.writeObject("employees ",employees);
  response.writeObject("avgSalary", avgSalary);
%>
```

### Static include
 
To statically include another file (usually a library) use  `--@include` command:

Example:
```sql
--@include library.js.sql
--@include library.js
<%
   selectEmployees();
   response.writeObject("testFunction",testFunction());
%>
```
Where library.js.sql :
```sql
--% function selectEmployees(){
SELECT * FROM EMPLOYEES
--% }
```

And library.js:
```js
function testFunction(){
    return "testFunction";
}
```

### Stored Procedure Calls

MySQL Example : 
```sql
CALL TEST_SIMPLE_SP(
    :(inout)(date)p.date ,
    :(inout)(number)p.number ,
    :p.name ,
    :(out)p.helloString
)
```

Oracle Example with complex data types:
```sql
{
  call TEST_TYPES(
   :(oStructArr.PERSON_ARRAY)p.personsIn ,
   :(out)(oStructArr.PERSON_ARRAY)personsOut ,
   :(oArr.MY_INT_ARRAY)p.inArr ,
   :(out)(oArr.MY_INT_ARRAY)outArr
    )
}
```
Input parameters whould be:
```js
{
        personsIn:[
            { name: "John Smith", email: "john@smith.com" },
            { name: "James Bond", email: "james@bond.com" }
        ],
        inArr:[1,2,3,4,5]
}
```
Where PERSON_ARRAY, MY_INT_ARRAY and the procedure TEST_TYPES are defined as following:
```sql
CREATE TYPE MY_INT_ARRAY IS VARRAY(100) OF INTEGER;
CREATE TYPE PERSON AS OBJECT  (
    name    VARCHAR(30),
    email   VARCHAR(30)
); 
CREATE TYPE PERSON_ARRAY AS TABLE OF PERSON;
CREATE OR REPLACE PROCEDURE "TEST_TYPES" (
  personsIn IN PERSON_ARRAY,
  personsOut OUT PERSON_ARRAY,
  intArrayIn IN MY_INT_ARRAY,
  intArrayOut IN MY_INT_ARRAY
)
 IS
BEGIN
  personsOut := personsIn ;
  intArrayOut := intArrayIn ;
END; 
```


## Configuration

Default directory
- The defaut LightLink root location in Web context is `/WEB-INF/lightlink/`
- The defaut LightLink root package in classpath is `lightlink/`

> The choise to hide "lightlink" under WEB-INF/ is related to security concerns. In such way we remove the probability that incorrect configuration will lead to expostion of .js.sql files via HTTP

All files *.js  and *.js.sql located in lightlink subpackages become REST Services or JavaScript API

Exception is made for:
- config.js or config.js.sql files 
- files that starts with a number or any other non-alphabetical character: #@!-, etc

Therefore .js or .js.sql files that are included/called by other services internally and are not supposed to be callable directly must start with a non-alpha carecter, for example “!”. This also allows to quickly distinguish library files and ordinary service files.

### Files config.js and config.js.sql

Files config.js and config.js.sql are special. They are not exposed as services, and are executed (included) before all other .js or .js.sql in the package and subpackages.

Usually they define datasources and some common settings, but also can include other library files, define reusable functions, perform authentication checks, etc..

Examples :
```js
sql.setConnection("com.mysql.jdbc.Driver","jdbc:mysql://localhost/lightlink?","root","mysql");
/// don't do it this way, consider using JNDI DataSources
sql.getConnection().setAutoCommit(false);  
```
or
```js
sql.setDataSourceJndi("java:comp/env/jdbc/MainDB");
sql.getConnection().setAutoCommit(false);  
sql.setFetchSize(100);
```
or
```js
var user = session.getAttribute("user"); // quite simple but efficient way to require authenticated user
if (!user || !user.id) 
    throw new Error("Auth required");
```

### Packages Tree Structure
Configuration files can be applied on tree structure by adding some additional configutations or handling steps for subpackages

Example:
```
rootPackage
   config.js     <--- datasource definition
   unprotected
       login.js.sql  <--- login/password check and creation of user object in HTTPSession
   secure
       config.js   <--- user object presence check in HTTPSession
       user
           userOperation1.js.sql
           userOperation2.js.sql
       admin
           adminOperation1.js.sql
           adminOperation2.js.sql
           config.js <--- checks that user object has admin rights
```

### Database connection settings
You can configure LightLink to use you app server connection pool  or direct connection (not recommended for production)

```
sql.setConnection("com.mysql.jdbc.Driver","jdbc:mysql://localhost/lightlink?","root","mysql");
```
or
```
sql.setDataSourceJndi("java:comp/env/jdbc/MainDB");
```
Additional parameters :
```
sql.setFetchSize(N); 
```
(See:  https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html#setFetchSize-int- )

```
sql.setMaxRows(N);  //allowing to limit the maximum number of rows
sql. setQueryTimeout (N); //allowing to limit the maximum query execution time
```

It’s also possible to change connection setting depending on parameter data
```
if (p.historicalDataRequested)
    sql.setDataSourceJndi("java:comp/env/jdbc/HistoDB");
else 
    sql.setDataSourceJndi("java:comp/env/jdbc/MainDB");
```

### Transaction settings
Transactions are disabled by default (autocommit=true)

#### Unmanaged transactions:
config.js:
```js
sql.setDataSourceJndi("java:comp/env/jdbc/MainDB");
sql.setFetchSize(100);
sql.setAutoCommit(false); // use transactions
```
### AppServer managed JEE transactions:
config.js:
```js
sql.setDataSourceJndi("java:comp/env/jdbc/MainDB");
tx.useTxJee(); // use JEE transactions
tx.setTxTimeout(30) // timeout in minutes for TransactionManager.setTransactionTimeout(..)
```

### Transaction auto commit

Both Managed and unmanaged transactions respect the following rules:
- Upon a successful execution the transaction is automatically comitted.
- In case of error or applicative exception raised by JavaScript or Java code, current transaction is rolled back.

### Manual transaction operations:
Unmanaged transactions:
```js 
sql.getConnection().rollback();
// or 
sql.getConnection().commit(); // .... see java.sql.Connection API
```

### AppServer managed JEE transactions:
```js
	tx.setTxRollbackOnly()
```
### Date format configuration
Allowing to define date format for JSON, both for input and output data
```js
types.setCustomDatePattern("yyyy-MM-dd HH:mm:ss");
```
> Note that (date) casting will first try to format incomming String as Date using the provided custom date pattern, if failed, it will try out a universal JSON date format : yyyy-MM-dd'T'HH:mm:ss.SSSXXX


## NoSQL and SQL

The debate SQL vs NoSQL is very interesting and controversial now days. There are many exciting new findings and each of the both camps has its advantages.

_We believe that the biggest advantage is be able to combine SQL and NoSQL approaches and use the best the two worlds._

**LightLink offers a seamless support for JSON data types from in-browser JavaScript to the server-side.**

Developers simply create their JSON object and use it as a parameter. On the server side this objects can be and/or stored to (retrieved from) the database as native data types.

```sql
SELECT
  ID, NAME, PRICE, DATE,
  UNSTRUCTURED_DATA AS "(json)UNSTRUCTURED_DATA"
FROM TEST_LIST
```

Note `UNSTRUCTURED_DATA AS "(json)UNSTRUCTURED_DATA"` indicating to handle UNSTRUCTURED_DATA column as JSON type. If the database version does not supports native JSON columns, a greaceful fallback to BOLB/TEXT is used.

Update example :

```sql
UPDATE
  TEST_LIST
SET
  UNSTRUCTURED_DATA =:(json)p.UNSTRUCTURED_DATA 
WHERE 
  ID=:(number)p.ID
```

**NoSQL  =  Not Only SQL**

So, let’s explain NoSQL as Not Only SQL and see what NoSQL-related features most popular databases offer today:
 
PostgreSQL :

http://www.linuxjournal.com/content/postgresql-nosql-database

http://info.enterprisedb.com/rs/enterprisedb/images/EDB_White_Paper_Using_the_NoSQL_Features_in_Postgres.pdf

Oracle:

https://docs.oracle.com/database/121/ADXDB/json.htm#ADXDB6246

MySQL :

https://dev.mysql.com/doc/refman/5.7/en/json.html

MS SQL:

https://msdn.microsoft.com/en-us/library/dn921882.aspx

In contrast to exclusive NoSQL databases like MongoDB you still use SQL as a query format, but take advantage of SQL environment combined with main NoSQL features :

- semi-structured and unstructured data,

- filtering and indexes by data in JSON objects,

- key-value, document store, etc..