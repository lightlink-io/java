package io.lightlink.servlet.debug;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class MethodOrConstructorWrapper {


    private Method method;
    private Constructor constructor;

    public static MethodOrConstructorWrapper[] getArray(Object[] methodsOrConstructors) {
        MethodOrConstructorWrapper[] wrappers = new MethodOrConstructorWrapper[methodsOrConstructors.length];
        for (int i = 0; i < methodsOrConstructors.length; i++) {
            Object o = methodsOrConstructors[i];
            if (o instanceof Method)
                wrappers[i] = new MethodOrConstructorWrapper((Method) o);
            else if (o instanceof Constructor)
                wrappers[i] = new MethodOrConstructorWrapper((Constructor) o);
        }
        return wrappers;
    }

    public MethodOrConstructorWrapper(Method method) {
        this.method = method;
    }

    public MethodOrConstructorWrapper(Constructor constructor) {
        this.constructor = constructor;
    }

    public Method getMethod() {
        return method;
    }

    public Constructor getConstructor() {
        return constructor;
    }

    public Class<?>[] getParameterTypes() {
        return method != null ? method.getParameterTypes() : constructor.getParameterTypes();
    }


}
