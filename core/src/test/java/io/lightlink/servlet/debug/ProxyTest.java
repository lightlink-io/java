package io.lightlink.servlet.debug;

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


import io.lightlink.output.ObjectBufferResponseStream;
import io.lightlink.test.TestRequest;
import junit.framework.TestCase;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProxyTest extends TestCase {

    private DebugFacadesProxyServlet proxyServlet;

    @Override
    protected void setUp() throws Exception {

        proxyServlet = new DebugFacadesProxyServlet();

    }

    public void test() throws Exception{


        HashMap<String,Object> params = new HashMap<String,Object>();

        params.put("action","createClass");
        params.put("className","io.lightlink.servlet.debug.ProxyTestBean");
        params.put("generation",0);

        ObjectBufferResponseStream json = new ObjectBufferResponseStream();

        HttpServletRequest request = TestRequest.getInstance();

        proxyServlet.dispatch(request, params, json);

        Object objectId = json.getDataMap().get("objectId");

        params.put("action","invoke");
        params.put("objectId",objectId);
        params.put("methodName","getTestInstance");
        params.put("args", Collections.emptyList());

        json = new ObjectBufferResponseStream();
        proxyServlet.dispatch(request, params, json);
        objectId = json.getDataMap().get("objectId");

        params.put("objectId",objectId);

        json = new ObjectBufferResponseStream();
        params.put("methodName","getList");
        proxyServlet.dispatch(request, params, json);
        Map<String,Object> data4List = json.getDataMap();

        json = new ObjectBufferResponseStream();
        params.put("methodName","getMap");
        proxyServlet.dispatch(request, params, json);
        Map<String,Object> data4Map = json.getDataMap();

        json = new ObjectBufferResponseStream();
        params.put("methodName","testInt");
        proxyServlet.dispatch(request, params, json);
        Map<String,Object> data4Int = json.getDataMap();

        int x=0;

    }

}
