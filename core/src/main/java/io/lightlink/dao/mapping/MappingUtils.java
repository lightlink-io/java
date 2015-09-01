package io.lightlink.dao.mapping;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

public class MappingUtils {
    public static Object convert(Class<?> propertyType, Object value, String propertyName) {
        if (value == null || propertyType.isAssignableFrom(value.getClass())) {
            return value;
        }

        if ((byte.class.equals(propertyType) || Byte.class.equals(propertyType)) && value.getClass().isArray()) {
            return value;
        } else if (value.getClass().isArray()) {
            Object[] valuesArray = (Object[]) value;
            if (propertyType.isArray()) {
                Object[] newArray = (Object[]) Array.newInstance(propertyType, valuesArray.length);
                for (int i = 0; i < valuesArray.length; i++) {
                    Object v = valuesArray[i];
                    newArray[i] = convert(propertyType.getComponentType(), v, propertyName);
                }
                return newArray;
            } else if (Collection.class.isAssignableFrom(propertyType)) {
                return Arrays.asList(valuesArray);
            }

        } else if (Boolean.class.equals(propertyType)
                || boolean.class.equals(propertyType)) {
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            } else {
                return !"0".equals(value.toString()) && !"".equals(value.toString());
            }

        } else if (value instanceof BigDecimal) {
            BigDecimal v = (BigDecimal) value;
            if (int.class.equals(propertyType) || Integer.class.equals(propertyType)) {
                return v.intValue();
            }
            if (long.class.equals(propertyType) || Long.class.equals(propertyType)) {
                return v.longValue();
            }
            if (float.class.equals(propertyType) || Float.class.equals(propertyType)) {
                return v.floatValue();
            }
            if (double.class.equals(propertyType) || Double.class.equals(propertyType)) {
                return v.doubleValue();
            }
            if (String.class.equals(propertyType)) {
                return v.toString();
            } else {
                return v.shortValue();
            }

        } else if (java.sql.Date.class.equals(propertyType)) {
            return new java.sql.Date(((Date) value).getTime());
        } else if (value instanceof Date) {
            return new Date(((Date) value).getTime());
        } else if (propertyType.isAssignableFrom(String.class)) {
            return value.toString();
        } else if ((Long.class.equals(propertyType) || long.class.equals(propertyType)) && value instanceof String) {
            return new Long((String) value);
        } else if ((Integer.class.equals(propertyType) || int.class.equals(propertyType)) && value instanceof String) {
            return new Integer((String) value);
        } else if ((Double.class.equals(propertyType) || double.class.equals(propertyType)) && value instanceof String) {
            return new Double((String) value);
        } else if ((Float.class.equals(propertyType) || float.class.equals(propertyType)) && value instanceof String) {
            return new Float((String) value);
        }
        throw new RuntimeException("Cannot convert for property:" + propertyName + " from:" + value.getClass().getCanonicalName()
                + " to " + propertyType.getCanonicalName() + " value:" + value);

    }
}
