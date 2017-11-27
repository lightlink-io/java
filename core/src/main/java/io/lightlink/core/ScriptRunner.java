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


import io.lightlink.config.ConfigManager;
import io.lightlink.config.Script;
import io.lightlink.facades.ResponseFacade;
import io.lightlink.facades.SQLFacade;
import io.lightlink.facades.ServletEnv;
import io.lightlink.facades.TxFacade;
import io.lightlink.output.ResponseStream;
import io.lightlink.translator.ScriptTranslator;
import io.lightlink.utils.LogUtils;
import io.lightlink.utils.Utils;
import jdk.nashorn.api.scripting.JSObject;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScriptRunner {

    HttpServletResponse response;
    ConfigManager cm;
    HttpServletRequest request;
    private ServletContext servletContext;

    public static final Object DEBUG_FILE_ACCESS_LOCK = new Object();

    public ScriptRunner(String rootPackage) {
        cm = new ConfigManager(rootPackage);
    }

    public ScriptRunner() {
        cm = new ConfigManager();
    }

    public ScriptRunner(HttpServletRequest request) {
        servletContext = request.getServletContext();
        cm = new ConfigManager(servletContext);
        this.request = request;
    }

    public ScriptRunner(String rootPackage, HttpServletRequest request, HttpServletResponse resp) {
        servletContext = request.getServletContext();
        cm = new ConfigManager(rootPackage, servletContext);
        this.request = request;
        this.response = resp;
    }

    public ScriptRunner(String rootPackage, ServletContext servletContext) {
        this.servletContext = servletContext;
        cm = new ConfigManager(rootPackage, servletContext);
    }


    public void execute(String action, String method, Map<String, Object> inputParams, ResponseStream responseStream) throws IOException {

        ScriptEngine engine = ConfigManager.getScriptEngine();

        ScriptContext context = engine.getContext();

        RunnerContext runnerContext = new RunnerContext(responseStream, engine);

        ResponseFacade responseFacade = runnerContext.getResponseFacade();

        SQLFacade sqlFacade = runnerContext.getRootSQLFacade();

        TxFacade txFacade = runnerContext.getTxFacade();
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("response", responseFacade);
        bindings.put("env", new ServletEnv(response, request));
        bindings.put("sql", sqlFacade);
        bindings.put("tx", runnerContext.getTxFacade());
        bindings.put("types", runnerContext.getTypesFacade());

        runnerContext.setParams(inputParams);

        PrintWriter printWriter = new PrintWriter(System.err);

        context.setWriter(printWriter);

        context.setAttribute(ScriptTranslator.SQL_CONTAINER_VARIABLE, "", ScriptContext.ENGINE_SCOPE);

        boolean success;

        List<Script> scripts = cm.getConfigAndContent(action, method);

        try {

            engine.eval(Utils.getResourceContent("io/lightlink/core/sqlFunctions.js"));

            for (Script script : scripts) {
                String content = script.getContent();
                String filePath = script.getUrl().getFile();
                bindings.put("_scriptName_", filePath);

                String debugOutput = null;

                if (ConfigManager.isInDebugMode()) {

                    if (script.getName().toLowerCase().endsWith(".js")) {
                        content = "load(\"" + filePath.replaceAll("\"", "\\\"") + "\");";
                    } else {

                        debugOutput = filePath.replaceAll(".js.sql$", ".debug.js");
                        if (debugOutput.matches("^/[A-Z]:.+"))
                            debugOutput = debugOutput.substring(1); // trim / from "/C:/..."

                        refreshTranslatedDebugFile(content, debugOutput, filePath);

                        content = "load(\"" + debugOutput.replaceAll("\"", "\\\"") + "\");";
                    }
                }
                try {
                    engine.eval(content);
                } catch (Exception e) {
                    if (debugOutput != null)
                        throw new RuntimeException(e.toString() + "\nSEE TRANSLATED JS: " + debugOutput, e);
                    else
                        throw new RuntimeException(e.toString(), e);
                } finally {
                    printWriter.flush();
                }

            }


            sqlFacade.query(); // implicit query if buffer not empty

            runnerContext.getResponseStream().writeProperty("success", true);

            txFacade.success();
        } catch (Throwable e) {
            runnerContext.getResponseStream().writeProperty("success", false);
            runnerContext.getResponseStream().writeProperty("error", e.toString());

            if (ConfigManager.isInDebugMode()) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                runnerContext.getResponseStream().writeProperty("stackTrace", sw.toString());
            }

            LogUtils.error(this.getClass(), e);

            txFacade.failure();

        } finally

        {
            sqlFacade.releaseConnection();
        }


        runnerContext.getResponseStream().end();

    }

    private void refreshTranslatedDebugFile(String content, String debugFilePath, String jsSqlFilePath) throws IOException {
        synchronized (DEBUG_FILE_ACCESS_LOCK) {
            File jsSql = new File(jsSqlFilePath);
            File debug = new File(debugFilePath);

            boolean needDebugFileUpdate = !debug.exists() || jsSql.lastModified() > debug.lastModified();
            if (needDebugFileUpdate){
                FileOutputStream fos = new FileOutputStream(debugFilePath);
                try {
                    fos.write(("//@ sourceURL=" + debugFilePath + "\n").getBytes("UTF-8"));
                    fos.write(content.getBytes("UTF-8"));
                } finally {
                    fos.close();
                }
            }
        }
    }

    private Map<String, Object> getHeadersMap() {

        Map<String, Object> map = new LinkedHashMap<String, Object>();

        if (request != null) {
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String key = (String) headerNames.nextElement();
                String value = request.getHeader(key);
                map.put(key, value);
            }
        }
        return map;
    }

    private JSObject newJSObject(ScriptEngine engine) {
        try {
            return (JSObject) engine.eval("({})");
        } catch (ScriptException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }
}
