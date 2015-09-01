package io.lightlink.test;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TestRequest {

    public static TestSession session = new TestSession();

    public TestSession getSession() {
        return session;
    }

    public static HttpServletRequest getInstance() {
        final TestRequest request = new TestRequest();
        return (HttpServletRequest) Proxy.newProxyInstance(TestRequest.class.getClassLoader()
                , new Class[]{HttpServletRequest.class}
                , new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getSession")) {
                    return request.getSession().getInstance();
                }
                else
                    return null;
            }
        });
    }
}
