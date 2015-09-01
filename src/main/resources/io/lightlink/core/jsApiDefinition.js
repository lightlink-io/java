var LL = {};

LL.HintManager = {

    PROGRESSIVE_KEY: "\n\t \t\n",

    callCallBack: function (callback, scope, res, isPartial) {
        if (typeof callback == "function")
            if (!scope) {
                callback(res, isPartial);
            } else {
                callback.apply(scope, [res, isPartial]);
            }

    },

    /**
     * Each partial section is delimited by "\n\t \t\n" token preceded by the number of \t equals to the
     * number of "}" after "]" needed to complete the JSON into correct form
     */
    completeJSON: function (responseText, hints) {
        LL.HintManager.PROGRESSIVE_KEY = "\n\t \t\n";
        var lastIndex = responseText.lastIndexOf(LL.HintManager.PROGRESSIVE_KEY);

        if (lastIndex != -1) {
            if (hints.lastIndex != lastIndex) {
                hints.lastIndex = lastIndex;
                responseText = responseText.substring(0, lastIndex);
                lastIndex--;
                responseText += "]";

                while (lastIndex > 0 && responseText[lastIndex] == "\t") {
                    lastIndex--;
                    responseText += "}";
                }
                return responseText;
            }
        }
        return null; // partial loading is impossible (received part is incomplete)
    },

    csrfTokenRenew: function (xmlhttp, fnName, params, callback, scope, hints) {
        LL.JsApi.ajax("csrfTokenRenew", {}, function (res) {
            LL.JsApi.CSRF_Token = res.newToken;
            LL.JsApi.ajax(fnName, params, callback, scope, hints)
        });
    },

    onReadyStateChange: function (xmlhttp, fnName, params, callback, scope, hints) {
        var res;

        if (xmlhttp.readyState === 3 && hints.progressive) {
            var responseText = xmlhttp.responseText;

            var completeJSON = this.completeJSON(responseText, hints);

            if (completeJSON == null)
                return;

            res = JSON.parse(completeJSON);

            if (res.success === false)
                return;

            LL.HintManager.callCallBack(callback, scope, res, true);

        } else if (xmlhttp.readyState === 4) {
            if (xmlhttp.status === 200) {
                try {
                    res = JSON.parse(xmlhttp.responseText);
                    if (res.success === false) {
                        if (res.csrf_error) {
                            LL.HintManager.csrfTokenRenew(xmlhttp, fnName, params, callback, scope, hints);
                        } else {
                            hints.onServerSideException(res, xmlhttp, fnName, params, callback, scope, hints);
                        }

                        return;
                    }
                } catch (e) {
                    hints.onJSONParsingError(e, xmlhttp, fnName, params, callback, scope, hints);
                    return;
                }
                LL.HintManager.callCallBack(callback, scope, res, false);
            } else {
                hints.onHTTPError(fnName, params, callback, scope, hints);
            }
        }
    },


    defaultOnJSONParsingError: function (e, xmlhttp, fnName, params, callback, scope, hints) {
        console.log("Error parsing JSON response for " + fnName + ": " + e, xmlhttp.responseText
            , "Redefine LL.HintManager.defaultHints.onJSONParsingError for custom handling");
        alert("Error parsing JSON response for " + fnName + ": " + e);
    },

    defaultOnServerSideException: function (res, xmlhttp, fnName, params, callback, scope, hints) {
        console.log("Server exception: " + res.error, xmlhttp.responseText
            , res.stackTrace);
        alert("Server exception: " + res.error);
        console.info("Redefine LL.HintManager.defaultHints.onServerSideException for custom messages");
    },

    defaultOnHTTPError: function (xmlhttp, fnName, params, callback, scope, hints) {
        console.log("HTTP error from server while calling:" + fnName + ": " + xmlhttp.status
            , "Redefine LL.HintManager.defaultHints.onJSONParsingError for custom handling");
        alert("HTTP error from server while calling:" + fnName + ": " + xmlhttp.status);
    },

    calcEffectiveHints: function (hints) {
        if (!hints)
            return LL.HintManager.defaultHints;
        var res = {};
        for (var hint in LL.HintManager.defaultHints) {
            if (LL.HintManager.defaultHints.hasOwnProperty(hint)) {
                res[hint] = hints.hasOwnProperty(hint) ? hints[hint] : LL.HintManager.defaultHints[hint];
            }
        }
        return res;
    }

};

LL.HintManager.defaultHints = {
    progressive: false, // [100,1000,5000]
    autoDetectDroppedClient: true,
    overloadProtection: true, //todo
    antiXss: true,// todo
    onJSONParsingError: LL.HintManager.defaultOnJSONParsingError,
    onServerSideException: LL.HintManager.defaultOnServerSideException,
    onHTTPError: LL.HintManager.defaultOnHTTPError
};


LL.JsApi = {

    services: [], // list of registered services;

    getXmlHttp: window.XMLHttpRequest
        ? function () {
        return new XMLHttpRequest()
    }
        : function () {
        return new ActiveXObject("Microsoft.XMLHTTP")
    },


    ajax: function (fnName, params, callback, scope, hints) {
        if (!params)
            params = {};

        hints = LL.HintManager.calcEffectiveHints(hints);

        var xmlhttp = LL.JsApi.getXmlHttp();
        xmlhttp.onreadystatechange = function () {
            LL.HintManager.onReadyStateChange(xmlhttp, fnName, params, callback, scope, hints);
        };

        xmlhttp.open("POST", LL.JsApi.url + "/" + fnName.replace(/\./g, "/"), true);

        if (hints) {
            params["!progressive"] = hints.progressive;
            params["!autoDetectDroppedClient"] = hints.autoDetectDroppedClient;
        }

        params.CSRF_Token = LL.JsApi.CSRF_Token;

        xmlhttp.send(JSON.stringify(params));
    },

    regService: function (fnName) {
        LL.JsApi.services.push(fnName);
    },

    define: function (fnName) {
        LL.JsApi.regService(fnName);

        var packages = fnName.split(/\./g);
        var ctx = window;
        for (var i = 0; i < packages.length - 1; i++) {
            var p = packages[i];
            if (!ctx[p])
                ctx[p] = {};
            ctx = ctx[p];
        }
        var name = packages[packages.length - 1];

        ctx[name] = function (param, callback, scope, hints) {
            LL.JsApi.ajax(fnName, param, callback, scope, hints);
        }
    },

    debugDefine: function (fnName) {
        document.write("<script type = text/javascript src='" + LL.JsApi.url + "-debug-src/" + fnName.replace(/\./g, "/") + "'></script>");
    }
};


LL.AntiXSS = {
    escape: function (data) {
        return data && ("" + data)
            .replace(/&/g, '&amp;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
    },
    unescape: function (data) {
        return data && ("" + data)
            .replace(/&quot;/g, '"')
            .replace(/&#39;/g, '\'')
            .replace(/&lt;/g, '<')
            .replace(/&gt;/g, '>')
            .replace(/&amp;/g, '&');
    }
};
