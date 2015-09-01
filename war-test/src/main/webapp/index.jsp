<html>
<style>
    body, form{font-family: sans-serif}
</style>
<body>
<script src="<%=request.getContextPath()%>/lightlink-api/jsapi.js"></script>

<h1>LightLink demo</h1>
<script>
    document.addEventListener('DOMContentLoaded', function(){
        backoffice.TestSelect(
                {firstName:"P*"},
                function (res) {
                    console.log(res)
                });
    });
</script>

...

<h3>Excel test</h3>
<form action="rest/ExcelTest.xlsx" method="POST">
<table>

<tr>
    <td>First Name:</td>
    <td><input name="firstName" value="P*"></td>
    <td>Last Name::</td>
    <td><input name="lastName"></td>
</tr>
<tr>
    <td>Min Salary:</td>
    <td><input name="minSalary"></td>
    <td>Max Salary:</td>
    <td><input name="maxSalary"></td>
</tr>
<tr>
    <td>Max Hire Date:</td>
    <td><input name="minHireDate"></td>
    <td>Min Hire Date:</td>
    <td><input name="maxHireDate"></td>
</tr>


</table>
<br>
<input type="submit">
</form>

</body>
</html>