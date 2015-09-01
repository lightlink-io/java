<html>
<head>
    <script src="<%=request.getContextPath()%>/lightlink-api/jsapi.js"></script>
</head>
<script>

    function callDummy(){
        csrf.blank({},function(res){
            alert(res.hello);
        })
    }


</script>
<body>
<button  onclick="LL.JsApi.CSRF_Token='xfccxcvxcv'">Forget Token</button>
<button  onclick="callDummy()">Call Dummy</button>
</body>
</html>
