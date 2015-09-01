package io.lightlink.translator;

import java.io.IOException;

public interface IScriptPostProcessor {

    public String process(String script) throws IOException;

}
