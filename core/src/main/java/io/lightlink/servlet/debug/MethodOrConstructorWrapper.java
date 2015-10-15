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
