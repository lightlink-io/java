package io.lightlink.test;

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


import io.lightlink.core.ScriptRunner;
import io.lightlink.output.JSONStringBufferResponseStream;
import io.lightlink.utils.Utils;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LightLinkFileTester {

    public static final Logger LOG = LoggerFactory.getLogger(LightLinkFileTester.class);

    String rootPackage;
    File file;

    int executionNumber;

    public LightLinkFileTester(String rootPackage, File file) {
        this.rootPackage = rootPackage;
        this.file = file;
    }

    public void doTestFile() throws IOException, ScriptException {

        String assertJs = null;
        String assertFileName = null;
        try {
            assertFileName = file.getAbsolutePath().replaceAll(".js.sql", ".asserts.js");
            if (assertFileName.contains("!")) {
                String cp = assertFileName.substring(assertFileName.indexOf('!') + 2).replace('\\', '/');
                assertJs = Utils.getResourceContent(cp);
            } else {
                assertJs = Utils.streamToString(new FileInputStream(assertFileName));
            }
        } catch (Exception e) {
            assertFileName = "default assert script: empty run'";
            assertJs = "run( {},function(res){})"; // if assert file absent, use default assert
        }
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("tester", this);
        bindings.put("_scriptName_", assertFileName);

        eval(engine, Utils.getResourceContent("io/lightlink/test/assertFunctions.js"));
        eval(engine, assertJs);


    }

    public String run(Object params) throws IOException, ScriptException {


        String[] parts = file.getAbsolutePath().replaceAll("\\\\", "/").split(
                rootPackage.replaceAll("\\.", "[\\\\/]") + "/"
        );
        String actionName = parts[parts.length - 1].replaceAll(".js.sql", "").replaceAll(".asserts.js", "");

        return runAction(actionName, params);
    }

    public String runAction(String actionName, Object params) throws IOException {
        executionNumber++;
        Map<String, Object> inputParams = new HashMap<String, Object>();


        if (params instanceof Map) {
            inputParams.putAll((Map<String, Object>) params);
        } else if (params instanceof ScriptObject) {
            ScriptObject so = (ScriptObject) params;
            Iterator<String> iterator = so.propertyIterator();
            while (iterator.hasNext()) {
                String property = iterator.next();
                inputParams.put(property, so.get(property));
            }
        }


        JSONStringBufferResponseStream bufferResponseStream = new JSONStringBufferResponseStream();
        ScriptRunner scriptRunner = new ScriptRunner(rootPackage);

        scriptRunner.setRequest(TestRequest.getInstance());

        scriptRunner.execute(
                actionName
                , "TEST"
                , inputParams, bufferResponseStream);

        String resJson = bufferResponseStream.getBuffer();

        if (file.exists()) {
            String fName = file.getAbsolutePath() + ".result" + executionNumber + ".json";
            FileOutputStream fos = new FileOutputStream(fName);
            fos.write(resJson.getBytes("UTF-8"));
            fos.close();
            LOG.info("Resulting JSON is saved for debugging purpose in " + fName);
        }
        return resJson;
    }

    private Object eval(ScriptEngine engine, String script) throws ScriptException {
        return engine.eval(script);

    }
}
