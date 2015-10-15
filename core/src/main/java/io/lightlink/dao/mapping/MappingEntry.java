package io.lightlink.dao.mapping;

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


import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

public class MappingEntry {

    public static final Logger LOG = LoggerFactory.getLogger(MappingEntry.class);

    private Class className;
    private List<String> usedProperties;

    private Object object;

    public MappingEntry(Class className, List<String> usedProperties, Object object) {
        this.className = className;
        this.usedProperties = usedProperties;
        this.object = object;
    }

    public Class getClassName() {
        return className;
    }

    public void setClassName(Class className) {
        this.className = className;
    }

    public List<String> getUsedProperties() {
        return usedProperties;
    }

    public void setUsedProperties(List<String> usedProperties) {
        this.usedProperties = usedProperties;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MappingEntry entry = (MappingEntry) o;

        if (className != null ? !className.equals(entry.className) : entry.className != null) return false;
        if (usedProperties != null ? !usedProperties.equals(entry.usedProperties) : entry.usedProperties != null)
            return false;

        try {
            if (usedProperties != null)
                for (String property : usedProperties) {
                    Object p1 = PropertyUtils.getProperty(object, property);
                    Object p2 = PropertyUtils.getProperty(entry.getObject(), property);

                    if (p1 != p2 && (p1 == null || p2 == null || !p1.equals(p2))) {
                        return false;
                    }
                }
        } catch (Exception e) {
            LOG.error(e.toString(), e);
            throw new RuntimeException(e);
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (usedProperties != null ? usedProperties.hashCode() : 0);

        try {
            if (usedProperties != null)
                for (String property : usedProperties) {
                    Object p = PropertyUtils.getProperty(object, property);
                    result = 31 * result + (p != null ? p.hashCode() : 0);
                }
        } catch (Exception e) {
            LOG.error(e.toString(), e);
            throw new RuntimeException(e);
        }
        return result;
    }
}
