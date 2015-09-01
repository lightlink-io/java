<html>
<style>
    body, form{font-family: sans-serif}
</style>
<body>
<script src="<%=request.getContextPath()%>/lightlink-api/jsapi.js"></script>


<script>
    function loadData(){
        l = new Date().getTime();
        progressive.LongQuery({}, callback, {progressive:[50,500,5000]});

    }

    function callback(res, isPartial) {
        document.getElementById("log").innerHTML +=
                "<br>" + res.resultSet.length + " rows received. Partial:" + isPartial
                +" Response time: "+(new Date().getTime()-l)+" millis";
    }
</script>
<button onclick="loadData()">Test Progressive Loading</button>
<code id="log"></code>

</body>
</html>