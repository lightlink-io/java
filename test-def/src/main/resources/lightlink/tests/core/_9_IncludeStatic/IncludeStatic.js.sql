--@include library.js.sql
--@include library.js

<%
selectEmployees();
response.writeObject("testFunction",testFunction());
%>