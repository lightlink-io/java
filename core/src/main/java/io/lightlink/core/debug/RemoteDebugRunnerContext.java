package io.lightlink.core.debug;

/*
 * #%L
 * lightlink-core
 * %%
 * Copyright (C) 2015 Vitaliy Shevchuk
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import io.lightlink.core.RunnerContext;
import io.lightlink.output.JSONStringBufferResponseStream;
import jdk.nashorn.api.scripting.JSObject;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
