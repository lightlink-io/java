package io.lightlink.translator;

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
