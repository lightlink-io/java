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
