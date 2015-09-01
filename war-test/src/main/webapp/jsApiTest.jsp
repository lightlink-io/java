<%@ page import="java.net.Inet4Address" %>
<html>

<script src="<%=request.getContextPath()%>/lightlink-api/jsapi.js"></script>
...

<script>
    document.addEventListener('DOMContentLoaded', function () {
        backoffice.TestSelect(
                {},
                function (res) {
                    console.log(res.resultSet)
                });
    });
</script>


<body>

...

</body>
</html>