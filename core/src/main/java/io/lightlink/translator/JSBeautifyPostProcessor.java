package io.lightlink.translator;

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


import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;

public class JSBeautifyPostProcessor implements IScriptPostProcessor {

    public static final Logger LOG = LoggerFactory.getLogger(JSBeautifyPostProcessor.class);

    @Override
    public String process(String script) throws IOException{
        String beautifier = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("io/lightlink/translator/beautify.js")
        );

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            engine.eval(beautifier);
            engine.getBindings(ScriptContext.ENGINE_SCOPE).put("$originalScript", script);
            return engine.eval("js_beautify($originalScript, {})")+"";
        } catch (ScriptException e) {
            LOG.error(e.toString(),e);
            return script;
        }

    }
}
