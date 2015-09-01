var $Debug = {
    xmlhttp: window.XMLHttpRequest
        ? new XMLHttpRequest()
        : new ActiveXObject("Microsoft.XMLHTTP"),


    sjax: function (params, callback) {
        xmlhttp = $Debug.xmlhttp;
        xmlhttp.open("POST", LL.JsApi.url + "-debug-proxy", false);
        xmlhttp.send(JSON.stringify(params));
        if (xmlhttp.status != 200) {
            console.error(xmlhttp.responseText);
            throw (xmlhttp.status + " " + xmlhttp.statusText);
        }
        return JSON.parse(xmlhttp.responseText);
    }
};

var JSProxy = {
    generation: new Date().getTime(),
    createMethodProxy: function (method) {
        return function () {
            var args = [];
            for (var i = 0; i < arguments.length; i++) {
                args[i] = arguments[i];
            }

            return  JSProxy.descrToStub($Debug.sjax({
                action: "invoke",
                methodName: method,
                objectId: this.objectId,
                args: args
            }));
        }
    },
    readPooledObject: function (stubDesc, arguments) {
        var res;
        if (stubDesc.className) {  // class stub returned (need to support 'new' operation)
            res = function () {
                var instance = JSProxy.createInstance(stubDesc.className, arguments);
                for (var p in instance) {
                    if (instance.hasOwnProperty(p)) {
                        this[p] = instance[p];
                    }
                }
            }
        } else {
            res = {};
        }
        res.objectId = stubDesc.objectId;
        for (var i = 0; i < stubDesc.methods.length; i++) {
            var method = stubDesc.methods[i];
            res[method] = JSProxy.createMethodProxy(method);
        }
        for (var p in stubDesc.fields) {
            if (stubDesc.fields.hasOwnProperty(p))
                res[p] = stubDesc.fields[p];
        }

        return res;
    },

    descrToStub: function (stubDesc) {
        var res, p;
        if (stubDesc.exception) {  // server-side exception
            console.error(stubDesc.exception
                , stubDesc.message + " at:\n"
                    + stubDesc.stackTrace
            );
            var e = new Error(stubDesc.exception + stubDesc.message);
            e.serverStack = stubDesc.stackTrace;
            throw  e;
        } else if (stubDesc.type == "array") {
            res = [];
            for (var i = 0; i < stubDesc.values.length; i++) {
                var el = stubDesc.values[i];
                res.push(this.descrToStub(el));
            }

            return res;
        } else if (stubDesc.type == "map") {
            res = {};
            for (p in stubDesc) {
                if (stubDesc.hasOwnProperty(p) && p != "type") {
                    res[p] = this.descrToStub(stubDesc[p])
                }
            }

            return res;
        } else if (stubDesc.type == "simple") { // simple value returned
            return stubDesc.value;
        } else { // object stub returned
            return this.readPooledObject(stubDesc, arguments);
        }
    },
    createInstance: function (className, args) {
        var argsCopy = [];
        for (var i = 0; i < args.length; i++) {
            argsCopy[i] = args[i];
        }

        var stubDesc = $Debug.sjax({
            action: "create",
            generation: JSProxy.generation,
            className: className,
            args: argsCopy
        });

        return  JSProxy.descrToStub(stubDesc);
    },

    createType: function (className) {
        var stubDesc = $Debug.sjax({
            action: "createClass",
            generation: JSProxy.generation,
            className: className
        });

        return  JSProxy.descrToStub(stubDesc);
    }
};

Java = {
    type: JSProxy.createType
};
