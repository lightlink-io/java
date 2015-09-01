<html>
<body>
<script>
    var Test = {
        getXmlHttp: window.XMLHttpRequest
                ? function () {
            return new XMLHttpRequest()
        }
                : function () {
            return new ActiveXObject("Microsoft.XMLHTTP")
        },


        ajax: function (url, callback) {

            var xmlhttp = Test.getXmlHttp();
            xmlhttp.onreadystatechange = function () {
                var el = document.getElementById("results");
                if (el)
                    el.innerHTML +=
                            xmlhttp.readyState + "," + xmlhttp.status + "," + xmlhttp.responseText.length + "<br>";
//                if (xmlhttp.readyState === 4) {
//                    if (xmlhttp.status === 200) {
//                        callback(JSON.parse(xmlhttp.responseText));
//                    } else {
//                        // todo error handling
//                        // HTTP ERROR Code
//                        // HTTP Redirect
//                        // success : false
//                    }
//                }
            };
            xmlhttp.open("GET", url, true);
            xmlhttp.send();
        }
    };

    Test.ajax("data.jsp");

</script>

<div id="results"></div>

</body>
</html>