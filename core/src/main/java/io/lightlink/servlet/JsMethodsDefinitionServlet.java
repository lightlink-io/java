package io.lightlink.servlet;

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
import io.lightlink.security.CSRFTokensContainer;
import io.lightlink.utils.ClasspathScanUtils;
import io.lightlink.utils.LogUtils;
import io.lightlink.utils.Utils;
import jdk.nashorn.api.scripting.JSObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

public class JsMethodsDefinitionServlet extends AbstractLightLinkServlet {

    private ConfigManager configManager;

    @Override
    public void init() throws ServletException {
        super.init();
        configManager = new ConfigManager(getRootPackage(), getServletContext());
    }


    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        resp.setContentType("application/javascript");
        resp.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        Cookie[] cookies = req.getCookies();
        String debugMethods = "";
        for (int i = 0; cookies != null && i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if ("lightlink.debug".equalsIgnoreCase(cookie.getName()))
                debugMethods = cookie.getValue();
        }

        PrintWriter writer = resp.getWriter();
        writer.print(getDeclarationScript(debugMethods, req));

        if (StringUtils.isNotEmpty(debugMethods) && ConfigManager.isInDebugMode()) {
            writer.print("\n// DEBUG PART \n");
            writer.print("\n\n    /***** io/lightlink/core/sqlFunctions.js   - for debugging *****/\n");
            writer.print(Utils.getResourceContent("io/lightlink/core/sqlFunctions.js"));
            writer.print("\n\n    /***** io/lightlink/core/debugProxy.js - for debugging *****/\n");
            writer.print(Utils.getResourceContent("io/lightlink/core/debugProxy.js"));
            writer.print("\n\n    /***** io/lightlink/core/LightLinkDebugSession.js - for debugging *****/\n");
            writer.print(Utils.getResourceContent("io/lightlink/core/LightLinkDebugSession.js"));
        }

        if (ConfigManager.isInDebugMode()) {
            writer.print("\n\n    /***** io/lightlink/core/IDDQD.js - for debugging *****/\n");
            writer.print(Utils.getResourceContent("io/lightlink/core/IDDQD.js"));
        }

        writer.print("\n" +
                "LL.JsApi.CSRF_Token = '" + CSRFTokensContainer.getInstance(req.getSession()).createNewToken() + "'\n");

        writer.close();
        resp.flushBuffer();
    }

    String getDeclarationScript(String debugMethods, HttpServletRequest req) throws IOException {

        Set<String> services = getServicesNames();

        URL definitionScript = Thread.currentThread().getContextClassLoader()
                .getResource("io/lightlink/core/jsApiDefinition.js");

        StringBuilder sb = new StringBuilder(IOUtils.toString(definitionScript));

        String lightLinkUrl = req.getServletPath().split("-api/")[0];
        sb.append("\n\nLL.JsApi.contextPath='").append(req.getContextPath()).append("';\n");
        sb.append("\n\nLL.JsApi.url='").append(req.getContextPath()).append(lightLinkUrl).append("';\n");
        sb.append("\n\nLL.JsApi.appContext='").append(req.getContextPath()).append("';\n\n");

        String[] debugExpressions = debugMethods.split("/");
        for (String service : services) {
            boolean debug = false;
            if (debugMethods != null) {
                for (String debugExpression : debugExpressions) {
                    if (service.matches(debugExpression.replaceAll("\\*", ".*"))) {
                        debug = true;
                        break;
                    }
                }
            }
            sb.append("LL.JsApi.").append(debug ? "debugDefine" : "define").append("('").append(service).append("');\n");
        }

        Map<String, String> customServices = getCustomServices();
        for (Map.Entry<String, String> entry : customServices.entrySet()) {
            sb.append("LL.JsApi.").append("defineCustomUrl")
                    .append("('").append(entry.getKey()).append("','").append(entry.getValue()).append("');\n");
        }
        return sb.toString();
    }


    private Set<String> getServicesNames() throws IOException {
        ArrayList<String> namesFromPackage = ClasspathScanUtils.getAllResources(getRootPackage(), getServletContext());
        Set<String> services = new TreeSet<String>();

        namesLoop:

        for (String name : namesFromPackage) {
            if (name.endsWith(".js.sql") || (name.endsWith(".js") && !name.endsWith(".debug.js"))) {
                name = name
                        .replaceAll("^[/\\\\]", "")
                        .replaceAll("[/\\\\]", ".")
                        .replaceAll(".js(.sql)?$", "");

                if (!name.endsWith("config")) {
                    //don't show services starting with non-alphabetic character.
                    if (Utils.isFirstNonAlphabetic(name))
                        continue namesLoop;

                    services.add(name);
                }
            }
        }
        return services;
    }

    private Map<String, String> getCustomServices() throws IOException {
        // list custom services
        Map<String, String> customServices = new LinkedHashMap<>();

        Script customServicesScript = configManager.getScript("#customServices.js", false);

        if (customServicesScript != null) {
            ScriptEngine engine = ConfigManager.getScriptEngine();

            try {
                Object result = engine.eval(customServicesScript.getContent());
                if (result instanceof JSObject) {
                    JSObject jsObject = (JSObject) result;
                    Set<String> keys = jsObject.keySet();
                    for (String key : keys) {
                        customServices.put(key, "" + jsObject.getMember(key));
                    }
                }
            } catch (ScriptException e) {
                LogUtils.error(getClass(), e);
            }

        }

        return customServices;

    }


}
