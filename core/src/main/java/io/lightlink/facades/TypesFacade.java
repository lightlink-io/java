package io.lightlink.facades;

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


import io.lightlink.core.RunnerContext;
import io.lightlink.types.AbstractConverter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TypesFacade {

    private String customDatePattern;

    private Map<String, Class> converterClasses = new HashMap<String, Class>();

    private RunnerContext runnerContext;

    public TypesFacade(RunnerContext runnerContext) {
        this.runnerContext = runnerContext;
    }

    public void registerType(String name, String converterClassName) {
        try {
            Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(converterClassName);
            converterClasses.put(name, cls);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Type " + name + " registration failed. Class " + converterClassName + " cannot be found", e);
        }
    }

    public String getCustomDatePattern() {
        return customDatePattern;
    }

    public void setCustomDatePattern(String customDatePattern) {
        this.customDatePattern = customDatePattern;
    }


    public String dateToString(Date date) {
        return new SimpleDateFormat(customDatePattern != null ? customDatePattern : "yyyy-MM-dd'T'HH:mm:ss").format(date);
    }

    public AbstractConverter getCustomConverter(String directive) {

        Class cls = converterClasses.get(directive);
        if (cls != null) {
            try {

                Object o = cls.newInstance();
                if (!(o instanceof AbstractConverter))
                    throw new IllegalArgumentException("Type " + directive + " failed. Class " + cls.getName() + " does not implement io.lightlink.types.AbstractConverter");
                else
                    return (AbstractConverter) o;

            } catch (Exception e) {
                throw new IllegalArgumentException("Type " + directive + " failed. Class " + cls.getName() + " cannot be instantiated", e);
            }
        } else
            return null;

    }
}
