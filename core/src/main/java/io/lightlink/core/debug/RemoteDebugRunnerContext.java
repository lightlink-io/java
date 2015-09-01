package io.lightlink.core.debug;

import io.lightlink.core.RunnerContext;
import io.lightlink.output.JSONResponseStream;
import io.lightlink.output.JSONStringBufferResponseStream;
import io.lightlink.output.ResponseStream;
import jdk.nashorn.api.scripting.JSObject;

import javax.script.Compilable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RemoteDebugRunnerContext extends RunnerContext {

    private Map<String, Object> params = new HashMap<String, Object>();
    private String sqlForDebug;

    public RemoteDebugRunnerContext() {
        super(new JSONStringBufferResponseStream(), null);
    }

    @Override
    protected void initializeScriptingObjects(Compilable scriptEngine) {
        // do nothing because there is no scriptEngine
    }

    private ScriptEngine getScriptEngine() {
        throw new IllegalStateException("not available during remote debug");
    }

    public JSObject newJSObject() {
        throw new IllegalStateException("not available during remote debug");
    }




    public void clearParams() throws ScriptException {
        params.clear();
    }

    public Object getParam(String name) throws ScriptException {
        return params.get(name);
    }

    public void setParams(Map<String, Object> inputParams) {
        this.params = inputParams;
    }

    public Object getBufferAsJSObject(String buffer) throws ScriptException {
        return buffer;
    }

    public String getAndClearSQLBuffer() throws SQLException, IOException, ScriptException {
        String res = sqlForDebug;
        sqlForDebug = "";
        return res;
    }

    public void setSqlForDebug(String sqlForDebug) {
        this.sqlForDebug = sqlForDebug;
    }

    public String getBuffer() throws IOException {
        getResponseStream().end();
        return ((JSONStringBufferResponseStream)getResponseStream()).getBuffer();
    }

}
