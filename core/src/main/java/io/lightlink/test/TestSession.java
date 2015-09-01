package io.lightlink.test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class TestSession {

    Map<String, Object> attributes = new HashMap<String, Object>();

    public void setAttribute(String s, Object o) {
        attributes.put(s, o);
    }

    public Object getAttribute(String s) {
        return attributes.get(s);
    }

    public HttpSession getInstance() {
        return (HttpSession) Proxy.newProxyInstance(TestRequest.class.getClassLoader()
                , new Class[]{HttpSession.class}
                , new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getAttribute"))
                    return getAttribute((String) args[0]);
                else if (method.getName().equals("setAttribute"))
                    setAttribute((String) args[0], args[1]);

                return null;
            }
        });
    }

}
