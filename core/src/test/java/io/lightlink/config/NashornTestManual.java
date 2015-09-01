package io.lightlink.config;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptUtils;
import junit.framework.TestCase;

import javax.script.*;

public class NashornTestManual extends TestCase {


    private JSObject function;

    public void test() throws ScriptException {

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        ScriptContext context = engine.getContext();
        Compilable compilable = (Compilable) engine;

        CompiledScript newObjectCreator = compilable.compile("({})");

        JSObject jsObject = (JSObject) newObjectCreator.eval();
        jsObject.setMember("a","a");
        context.setAttribute("myObject", jsObject, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("ctx",this,ScriptContext.ENGINE_SCOPE);

        Object aA = engine.eval("print(myObject.a)");

        Object obj = engine.eval("(function (a,b){print(a+' '+b)})");

        engine.eval("ctx.setJSFunction(function(a,b){return a+b})");

        int x=0;
    }

    public void setJSFunction(JSObject function){
        this.function = function;
    }


}
