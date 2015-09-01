<%@ page import="io.lightlink.utils.ClasspathScanUtils" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.apache.commons.io.IOUtils" %>
<html>
<head>

</head>
<body>
<style>
    .ko {
        color: red;
    }

    .ok {
        color: green;
    }

    body, form {
        font-family: sans-serif;
    }

</style>


<script src="<%=request.getContextPath()%>/lightUnit-api/jsapi.js"></script>

<h1>LightLink Unit tests in client-side debug mode</h1>
<script>
    <%= IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("io/lightlink/test/assertFunctions.js")) %>
</script>

<%

    Cookie cookie = new Cookie("lightlink.debug", "/*");
    cookie.setPath("/");
    response.addCookie(cookie);

    String rootPackageName = "lightlink";
    ArrayList<String> asserts = ClasspathScanUtils
            .getResourcesFromPackage(rootPackageName, ".*\\.asserts\\.js$");


    for (String assertName : asserts) {
        String functionName = assertName.replaceAll(".asserts.js$", "");
        functionName = functionName.replaceAll("[\\\\/]", ".").replaceAll("^\\.", "");

        String only = request.getParameter("only");
        if (only!=null && !functionName.contains(only)){
            continue;
        }
%>
<script>

    function run(params, callback) {

        (<%=functionName%>)(params, function (res) {
            try {
                callback(res);

                var div = document.createElement("div");
                div.className="ok";
                div.innerHTML = "Test <%=functionName%> : Passed ";
                document.body.appendChild(div);
console.log("<%=functionName%>");
            } catch (e) {

                var div = document.createElement("div");
                console.error(e);
                div.className="ko";
                div.innerHTML = "Test <%=functionName%> : Failed ";
                document.body.appendChild(div);

            }
        });
    }


    <%=ClasspathScanUtils.getContentFromResource(rootPackageName,assertName)%>


</script>


<%
    }

%>


</body>
</html>