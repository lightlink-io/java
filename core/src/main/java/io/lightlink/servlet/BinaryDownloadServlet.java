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


import io.lightlink.output.BinaryDownloadResponseStream;
import io.lightlink.output.ResponseStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class BinaryDownloadServlet extends RestServlet {
    public static final Logger LOG = LoggerFactory.getLogger(BinaryDownloadServlet.class);

    protected void doServide(String method, HttpServletRequest req, HttpServletResponse resp, Map<String, Object> inputParams) throws IOException {

        String actionName = getAction(req);

        ResponseStream responseStream = new BinaryDownloadResponseStream(resp);

        getScriptRunner(req, resp).execute(actionName, method, inputParams, responseStream);

    }


}
