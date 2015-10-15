package io.lightlink.config;

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
