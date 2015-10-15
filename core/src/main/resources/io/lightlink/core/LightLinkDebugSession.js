LL.DebugSession = function () {

    this.request = new (Java.type("io.lightlink.servlet.debug.HttpRequestPlaceholder"))();
    this.session  =this.request.getSession();

    var context = new (Java.type("io.lightlink.core.debug.RemoteDebugRunnerContext"))();
    this.context = context;
    this.sql = new (Java.type("io.lightlink.facades.SQLFacade"))(context);
    this.tx = this.context.getTxFacade();
    this.types = this.context.getTypesFacade();
    this.resp = context.getResponseStream();
    var response = this.response = context.getResponseFacade();

    this.sql.query_orig = this.sql.query;
    this.sql.queryToBuffer_orig = this.sql.queryToBuffer;

    function prepareDebugContext() {
        context.setSqlForDebug($SQL);
        $SQL = "";

        var params = {};
        for (var p in $P) {
            if ($P.hasOwnProperty(p) && p.match(/^__.*/))
                params[p.substring(2)] = $P[p];
        }
        context.setParams(params);
    }

    this.sql.query = function () {
        prepareDebugContext();
        $SQL = "";

        var rowHandler = arguments[arguments.length - 1];
        if (typeof rowHandler == "function" && arguments.length>=2) {
            // row handler behaviour needs to be simulated
            var rsName = arguments[arguments.length-2];
            var data = this.queryToBuffer_orig();
            for (var i = 0; i < data.length; i++) {
                var line = data[i];
                data[i] = rowHandler(line,i,rsName);
            }
            response.writeObject(rsName,data);
        } else {
            this.query_orig.apply(this, arguments);
        }

    };


    this.sql.queryToBuffer = function () {
        prepareDebugContext();
        $SQL = "";
        return  this.queryToBuffer_orig();
    };

    this.sql.addBatch = function (resultSet, rowHandler) {
        this.query(true, resultSet, rowHandler);
    };

    window.sql = this.sql;
    window.types = this.types;
    window.tx = this.tx;
    window.response = this.response;
    window.request = this.request;
    window.session = this.session;


};