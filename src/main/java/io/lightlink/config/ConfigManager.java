package io.lightlink.config;

import io.lightlink.translator.ScriptTranslator;
import io.lightlink.utils.Utils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    public static final String DEFAULT_ROOT_PACKAGE = "lightlink";

    private ServletContext servletContext;
    private String rootPackage;
    private ScriptTranslator translator = new ScriptTranslator();

    public ConfigManager() {
        this(DEFAULT_ROOT_PACKAGE);
    }

    public ConfigManager(String rootPackage) {
        this.rootPackage = rootPackage.replace('.', '/');
    }

    public ConfigManager(String rootPackage, ServletContext servletContext) {
        this(rootPackage);
        this.servletContext = servletContext;
    }

    public ConfigManager(ServletContext servletContext) {
        this();
        this.servletContext = servletContext;
    }

    public String getRootPackage() {
        return rootPackage;
    }


    public static boolean isInDebugMode() {
        return "true".equalsIgnoreCase(System.getProperty("lightlink.debug"));
    }

    public List<Script> getConfigAndContent(String actionName, String method) {
        ArrayList<Script> res = new ArrayList<Script>();

        boolean methodPresent = StringUtils.isNotBlank(method);

        int pos = 0;
        do {
            String dir = actionName.substring(0, pos);
            Script script = getScript(dir + "/config.js", false);
            if (script != null)
                res.add(script);

            if (methodPresent) {
                script = getScript(dir + "/config." + method + ".js", false);
                if (script != null)
                    res.add(script);
            }

            Script scriptSql = getScript(dir + "/config.js.sql", true);
            if (scriptSql != null)
                res.add(scriptSql);

            if (methodPresent) {
                scriptSql = getScript(dir + "/config." + method + ".js.sql", true);
                if (scriptSql != null)
                    res.add(scriptSql);
            }
        } while (-1 != (pos = actionName.indexOf("/", pos + 1)));

        Script mainScript = methodPresent ? getScript(actionName + "." + method + ".js", false) : null;
        if (mainScript == null)
            mainScript = getScript(actionName + ".js", false);

        Script mainScriptSql = methodPresent ? getScript(actionName + "." + method + ".js.sql", true) : null;
        if (mainScriptSql == null)
            mainScriptSql = getScript(actionName + ".js.sql", true);

        Script mainScriptPostprocess = methodPresent
                ? getScript(actionName + "." + method + ".postprocess.js", false) : null;

        if (mainScriptPostprocess == null)
            mainScriptPostprocess = getScript(actionName + ".postprocess.js", false);

        if (mainScript == null && mainScriptSql == null)
            throw new io.lightlink.exception.ActionNotFoundException(actionName);

        if (mainScript != null)
            res.add(mainScript);
        if (mainScriptSql != null)
            res.add(mainScriptSql);
        if (mainScriptPostprocess != null)
            res.add(mainScriptPostprocess);

        return res;
    }

    private Script getScript(String scriptName, boolean translationNeeded) {

        scriptName = scriptName.replace('\\', '/');

        if (scriptName.startsWith("/"))
            scriptName = scriptName.substring(1);

        String name = getRootPackage() + "/" + scriptName;


        URL url = Utils.getUrl(name, servletContext);
        if (url == null) return null;

        String content;

        try {
            content = getScriptContent(url, scriptName, translationNeeded);
            return new Script(scriptName, url, content);
        } catch (IOException e) {
            return null;
        }
    }

    private String getScriptContent(URL url, String scriptName, boolean translationNeeded) throws IOException {
        String content;
        if (!isInDebugMode()) {

            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(getRootPackage() + "/" + scriptName); //cached by classloader
            content = Utils.streamToString(stream);
            stream.close();

            content = processInclude(scriptName, content, translationNeeded);

            if (translationNeeded)
                content = translator.translate(scriptName, url, content);
        } else {
            // debug mode

            InputStream stream = url.openStream();
            content = Utils.streamToString(stream);
            stream.close();

            content = processInclude(scriptName, content, translationNeeded);
            if (translationNeeded)
                content = translator.translate(scriptName, url, content);
        }
        return content;
    }

    private boolean prefixedByComment(int pos, StringBuilder sb) {
        if (pos < 2)
            return false;
        char a = sb.charAt(pos - 1);
        char b = sb.charAt(pos - 2);
        return (a == '/' && b == '/') || (a == '-' || b == '-');
    }

    private String processInclude(String scriptName, String content, boolean translationNeeded) {
        String key = "@include ";
        if (content.contains(key)) {
            StringBuilder sb = new StringBuilder(content);
            int pos = -1;
            while (-1 != (pos = sb.indexOf(key, pos + 1))) {
                if (prefixedByComment(pos, sb)) {
                    int keyLength = key.length() + 2;
                    pos = pos - 2;
                    boolean inclAsTemplate = sb.charAt(pos)=='-';
                    for (int pos2 = pos + keyLength; pos2 < sb.length(); pos2++) {
                        char c = sb.charAt(pos2);
                        if (Character.isWhitespace(c) || pos2 == sb.length() - 1) {
                            if (pos2 == sb.length() - 1)
                                pos2++;

                            String resource = sb.substring(pos + keyLength, pos2);
                            sb.delete(pos, pos2);
                            int lastSlash = scriptName.lastIndexOf("/");
                            if (lastSlash == -1)
                                lastSlash = 0;

                            String scriptDir = scriptName.substring(0, lastSlash);

                            String path = "/" + scriptDir + "/" + resource;


                            Script script = getScript(path, false);

                            if (script == null)
                                throw new IllegalArgumentException("Cannot include '" + resource + "' from '" + scriptName + "' File:'" + path + "' not found");


                            String prefix="", postfix="";

                            if (path.endsWith(".js.sql") && !inclAsTemplate){
                                prefix="\n%>";
                                postfix="\n<%";
                            } else if (!path.endsWith(".js.sql") && inclAsTemplate){
                                prefix="\n<%";
                                postfix="\n%>";
                            }

                            sb.insert(pos, prefix+script.getContent()+postfix);

                            break;

                        }
                    }
                }
            }
            return sb.toString();
        } else {
            return content;
        }
    }


}
