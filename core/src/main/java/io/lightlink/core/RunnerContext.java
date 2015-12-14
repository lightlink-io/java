package io.lightlink.core;

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


import io.lightlink.facades.ResponseFacade;
import io.lightlink.facades.SQLFacade;
import io.lightlink.facades.TxFacade;
import io.lightlink.facades.TypesFacade;
import io.lightlink.output.ResponseStream;
import io.lightlink.translator.ScriptTranslator;
import jdk.nashorn.api.scripting.JSObject;

import javax.script.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RunnerContext {

    private CompiledScript newObjectScript;

    private TxFacade txFacade;
    private TypesFacade typesFacade;
    private ResponseFacade responseFacade;
    private ScriptEngine scriptEngine;
    private Set<String> usedResultSetNames = new HashSet<String>();
    private ResponseStream responseStream;

    /**
     * Identifies the browser window/tab. In order to be able to cancel all running queries for a given window/tab
     */
    private String csrfToken;
    private io.lightlink.facades.SQLFacade SQLFacade;
    private SQLFacade rootSQLFacade;

    public RunnerContext(ResponseStream responseStream, ScriptEngine scriptEngine) {
        this.responseStream = responseStream;
        responseStream.setRunnerContext(this);
        this.scriptEngine = scriptEngine;
        txFacade = new TxFacade(this);
        typesFacade = new TypesFacade(this);
        responseFacade = new ResponseFacade(this);
        initializeScriptingObjects((Compilable) scriptEngine);
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    protected void initializeScriptingObjects(Compilable scriptEngine) {
        try {
            newObjectScript = scriptEngine.compile("({})");
        } catch (ScriptException e) {
            throw new RuntimeException(e.toString(), e); // should never happen
        }
    }

    public ResponseFacade getResponseFacade() {
        return responseFacade;
    }

    public TxFacade getTxFacade() {
        return txFacade;
    }

    public TypesFacade getTypesFacade() {
        return typesFacade;
    }

    public ScriptContext getScriptContext() {
        return scriptEngine.getContext();
    }

    public ResponseStream getResponseStream() {
        return responseStream;
    }


    public void setResponseStream(ResponseStream resp) {
        this.responseStream = resp;
        resp.setRunnerContext(this);
    }

    private ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    public JSObject newJSObject() {
        try {
            return (JSObject) newObjectScript.eval();
        } catch (ScriptException e) {
            throw new RuntimeException(e.toString(), e); // should never happen
        }
    }


    public Set<String> getUsedResultSetNames() {
        return usedResultSetNames;
    }

    public void clearParams() throws ScriptException {
        getScriptEngine().eval("$P = {}");
    }

    public Object getParam(String name) throws ScriptException {
        Object res = getScriptEngine().eval("$P['__" + name + "']");
        if (res instanceof CharSequence && !(res instanceof String)){
            res =res.toString();
        }
        return res;
    }

    public void setParams(Map<String, Object> inputParams) {
        try {
            JSObject params = mapToJSObject(inputParams);
            getScriptContext().setAttribute("p", params, ScriptContext.ENGINE_SCOPE);
        } catch (ScriptException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public JSObject mapToJSObject(Map<String, Object> inputParams) throws ScriptException {
        JSObject params = (JSObject) newObjectScript.eval();
        for (Map.Entry<String, Object> entry : inputParams.entrySet()) {
            params.setMember(entry.getKey(), entry.getValue());
        }
        return params;
    }

    public Object getBufferAsJSObject(String buffer) throws ScriptException {
        return getScriptEngine().eval("(" + buffer + ").buffer");
    }

    public String getAndClearSQLBuffer() throws SQLException, IOException, ScriptException {

        ScriptContext jsContext = getScriptContext();
        Object sqlVarValue = jsContext.getAttribute(ScriptTranslator.SQL_CONTAINER_VARIABLE);

        String sql = sqlVarValue == null ? null : sqlVarValue.toString();

        jsContext.setAttribute(ScriptTranslator.SQL_CONTAINER_VARIABLE, "", ScriptContext.ENGINE_SCOPE);
        return sql;
    }


    public SQLFacade getRootSQLFacade() {
        if (rootSQLFacade == null)
            rootSQLFacade = new SQLFacade(this);

        return rootSQLFacade;
    }
}