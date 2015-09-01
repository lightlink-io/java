package io.lightlink.servlet.debug;

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

        Object objectId = json.getData().get("objectId");

        params.put("action","invoke");
        params.put("objectId",objectId);
        params.put("methodName","getTestInstance");
        params.put("args", Collections.emptyList());

        json = new ObjectBufferResponseStream();
        proxyServlet.dispatch(request, params, json);
        objectId = json.getData().get("objectId");

        params.put("objectId",objectId);

        json = new ObjectBufferResponseStream();
        params.put("methodName","getList");
        proxyServlet.dispatch(request, params, json);
        Map<String,Object> data4List = json.getData();

        json = new ObjectBufferResponseStream();
        params.put("methodName","getMap");
        proxyServlet.dispatch(request, params, json);
        Map<String,Object> data4Map = json.getData();

        json = new ObjectBufferResponseStream();
        params.put("methodName","testInt");
        proxyServlet.dispatch(request, params, json);
        Map<String,Object> data4Int = json.getData();

        int x=0;

    }

}
