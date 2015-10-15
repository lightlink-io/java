LL.DebugUI = {
    kBuffer: "",

    keyUp: function (evt) {
        if (LL.DebugUI.win && LL.DebugUI.win.style.display == "") {
            evt = evt || window.event;
            var charCode = evt.which || evt.keyCode;
            if (charCode == 27)
                LL.DebugUI.hideWindow();
            if (charCode == 13)
                LL.DebugUI.submit();
        }
    },
    keyPress: function (evt) {
        evt = evt || window.event;
        var charCode = evt.which || evt.keyCode;
        LL.DebugUI.kBuffer += String.fromCharCode(charCode);
        if (LL.DebugUI.kBuffer.length > 5)
            LL.DebugUI.kBuffer = LL.DebugUI.kBuffer.substring(LL.DebugUI.kBuffer.length - 5);
        var code = LL.DebugUI.kBuffer.toUpperCase();
        if (code == "IDDQD" || code == "IDDAD") {
            LL.DebugUI.displayWindow();
        }
    },


    hideWindow: function () {
        if (LL.DebugUI.win && LL.DebugUI.win.style.display == "")
            LL.DebugUI.win.style.display = "none";
    },

    submit: function () {
        var els = document.getElementsByName("LL.DebugUI.checkbox");
        var cookie = "";
        for (var i = 0; i < els.length; i++) {
            var input = els[i];
            if (input.checked)
                cookie += "/" + input.id;
        }
        if (!cookie)
            cookie = "-";
        document.cookie = "lightlink.debug=" + cookie + ";path=" + LL.JsApi.appContext + "/";
        location.reload(true);
    },

    displayWindow: function () {
        if (!LL.DebugUI.win) {
            var w = LL.DebugUI.win = document.createElement("div");
            w.className = "iddqdWin";
            var s = w.style;
            s.position = "absolute";
            s.border = "1px solid #888";
            s.top = s.left = s.bottom = s.right = "20px";
            s.maxWidth = "550px";
            s.maxHeight = "800px";
            s.boxShadow = "5px 5px 30px #AAA";
            s.backgroundColor = "white";
            s.padding = "15px";
            s.fontFamily = "sans-serif";
            s.fontSize = "small";
            s.color = "#3377DD";
            s.borderRadius = "5px";
            s.overflow = "auto";
            s.zIndex = "2147483647";

            var html = "<b style='font-size: 150%'>LighLink</b><div style=float:right>" +
                "<button onclick='LL.DebugUI.submit()'><b>Apply&Reload</b></button> " +
                "<button onclick='LL.DebugUI.hideWindow()'>Cancel</button></div><hr>" +
                "choose services for in-browser debugging:<br><br>";
            var services = LL.JsApi.services;
            var packages = {};
            services.sort();
            html += LL.DebugUI.lineHtml("*");

            for (var i = 0; i < services.length; i++) {
                var service = services[i];
                var package = service.replace(/\.?[a-zA-Z_0-9\$]+$/, "");
                if (package && !packages[package]) {
                    packages[package] = true;
                    html += LL.DebugUI.lineHtml(package + ".*");
                }
                html += LL.DebugUI.lineHtml(service);
            }


            w.innerHTML = html;
            document.body.appendChild(w);
            var style = document.createElement("style");
            style.innerHTML = ".debug{" +
                "background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA8AAAAPCAMAAAAMCGV4AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAK5QTFRFsa6xLG97MFVJHzgxN1VS2+vMME1JJ0Q4ME1Ey9+0LE08xtyuI2d5n8eBJ0c85/PbpsmHHGF4LE1EJmp5IGZ4HGJ4ttKZH2R4x9yv1ebCJ0A4Km17Ij0xK256N1VNUo07r86RtdKZx9uvlcJ4lsN4uNKc1efCOXsqHGJ3cqdXKWx7IkAxx9uuHmN3xtuu2+rMKWx6IWV5LWkhksF+rs6RHmR4LEc8Jml6uNKd////t35ewgAAADp0Uk5T////////////////////////////////////////////////////////////////////////////ADfA/woAAACQSURBVHjaVE8FDsMwEHPSpMxjZmbe/f9jS9so2yydT5Z9kg+kMCQDFGSrEV+Np2JXa27nMWNwYHywZMoYI6ur8732VXaCAeBzghCE8ymR3nuLyned11Lejs20pvP9MPD2m3EUWn6pHdQXk/tofgHZhc7xaKXRbHWo8lwtK1s3MtOXaAf89CeK//4pTyp8BBgA9t0jF0u9C04AAAAASUVORK5CYII=) 5px 6px no-repeat;" +
                "padding: 5px 5px 5px 25px;" +
                "white-space:nowrap;}" +
                ".run{" +
                "background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA8AAAAPCAMAAAAMCGV4AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAI1QTFRF////Za12rcut1eDVJYBAgMwlMohHVqRdbqRdMnY5LIAyXZpWiLduLIhAR4BHR5FOutK6TppWOYhOZa1uR5pWJYhAdq12R5FWkbd2H4BAOYhAR5pOOZFHQJFAkbduXZpuiLd2QJpHMog5OYg5wul3QIhHVqRWpMGIZaRliLeAQJFOmsGIQJFWgK1u////Tr2RTwAAAC90Uk5T/////////////////////////////////////////////////////////////wBapTj3AAAAqElEQVR42lTP1w7CMAwFUDuzSWhDN6tlb/D/fx5OkZC4Tz6JZF0DEUmslKpQ8khAhEbr5/V+q3Ay9jsNehwXi54fQJr3bA6gZxwjCbDMMjbojIMEJoSQDDqEzrCLohDJcCo6RbB0zk2eO7ddEijvfbLw/mz5Hx8xskWM6/LF+2ReDgLEajWsbS5Tn40V4nDcX+wGv33zxta1bfJv33RPq1T7u+cvHwEGADomESnH+7g5AAAAAElFTkSuQmCC) 5px 6px no-repeat;" +
                "padding: 5px 5px 5px 25px;" +
                "white-space:nowrap;}" +
                ".iddqdWin a{color:#3377DD}" +
                ".iddqdWin{}";
            document.body.appendChild(style);
        }
        LL.DebugUI.win.style.display = "";
    },

    lineHtml: function (service) {
        var cookie = document.cookie.match(new RegExp('lightlink.debug=([^;]+)'));
        var checked = cookie && ("/" + cookie[1]).indexOf("/" + service) != -1 ? " checked" : "";
        var testCode = service.match(/\*$/) ? "" : (" ( <a href=# onclick=\"LL.DebugUI.toggleDebug('" + service + "');\">test</a> )"
            + "<div id='" + service + "Test' style=text-align:right;display:none>"
            + "<textarea onkeyup='event.stopPropagation()' id='" + service + "Params' style=width:100%;height:100px>{\n// parameters \n}\n</textarea><br>"
            + "<button class=debug onclick='LL.DebugUI.run(\"" + service + "\",true)'>Debug</button>"
            + "<button class=run  onclick='LL.DebugUI.run(\"" + service + "\",false)'>Run</button>");

        var res = "<div><input type='checkbox' name=LL.DebugUI.checkbox id='" + service + "'" + checked + ">"
            + "<label for='" + service + "'>" + service + "</label>"
            + testCode + "</div>"
            + "</div>";
        return  res;
    },

    toggleDebug: function (service) {
        var s = document.getElementById(service + "Test").style;
        s.display = s.display == "none" ? "" : "none";
    },

    run: function (service, debug) {
        var p = document.getElementById(service + "Params").value;
        eval((debug ? "debugger;\n\n" : "") + service + "(" + p + ",function(res){alert('Done. The response is displayed in JavaScript console of your browser.');console.log(res)});");
    }

};


window.addEventListener("keypress", LL.DebugUI.keyPress, false);
window.addEventListener("keyup", LL.DebugUI.keyUp, false);
