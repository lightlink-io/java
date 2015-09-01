package io.lightlink.servlet;

import io.lightlink.config.ConfigManager;
import io.lightlink.core.ScriptRunner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AbstractLightLinkServlet extends HttpServlet {

    private String rootPackage = ConfigManager.DEFAULT_ROOT_PACKAGE;

    @Override
    public void init(ServletConfig config) throws ServletException {

        String rootConfig = config.getInitParameter("lightlink.root");
        if (rootConfig != null)
            this.rootPackage = rootConfig;
        else {
            String root = config.getServletContext().getInitParameter("lightlink.root");
            if (root!=null)
                this.rootPackage = root;
        }
        super.init(config);
    }

    public ScriptRunner getScriptRunner(HttpServletRequest request, HttpServletResponse resp) {
        ScriptRunner scriptRunner = new ScriptRunner(this.rootPackage, request, resp);
        return scriptRunner;
    }

    public String getRootPackage() {
        return rootPackage;
    }


}
