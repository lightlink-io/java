package io.lightlink.dao.mapping;

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
