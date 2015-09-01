package io.lightlink.dao.mapping;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanMapper {

    public static final Logger LOG = LoggerFactory.getLogger(BeanMapper.class);

    Class paramClass;

    List<String> ownFields = new ArrayList<String>();

    Map<String, BeanMapper> childMappers = new CaseInsensitiveMap();
    Map<String, BeanMapper> childListMappers = new CaseInsensitiveMap();

    Map<MappingEntry, Object> pool = new HashMap<MappingEntry, Object>();

    private final Map<String, PropertyDescriptor> descriptorMap;


    public BeanMapper(Class paramClass, List<String> fieldsOfLine) {

        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(paramClass);
        descriptorMap = new CaseInsensitiveMap();
        for (PropertyDescriptor descriptor : descriptors) {
            descriptorMap.put(normalizePropertyName(descriptor.getName()), descriptor);
        }

        List<String> ownFields = new ArrayList<String>();
        Map<String, List<String>> fieldsByChild = new CaseInsensitiveMap();

        groupFields(fieldsOfLine, ownFields, fieldsByChild);

        this.ownFields = ownFields;
        for (Map.Entry<String, List<String>> entry : fieldsByChild.entrySet()) {
            String childProperty = normalizePropertyName(entry.getKey());
            PropertyDescriptor descriptor = descriptorMap.get(childProperty);
            if (descriptor != null) {
                List<String> properties = entry.getValue();
                if (descriptor.getPropertyType().isAssignableFrom(ArrayList.class)) {
                    Type[] typeArguments = ((ParameterizedType) descriptor.getReadMethod().getGenericReturnType()).getActualTypeArguments();
                    if (typeArguments.length == 0)
                        throw new RuntimeException("Cannot define Generic list type of " + entry.getKey() + " property of " + paramClass);
                    Type firstType = typeArguments[0];
                    if (firstType instanceof Class) {
                        childListMappers.put(childProperty, new BeanMapper((Class) firstType, properties));
                    }
                } else {
                    childMappers.put(childProperty, new BeanMapper(descriptor.getPropertyType(), properties));
                }
            } else {
                throw new RuntimeException("cannot define mapping for class:" + paramClass.getName() + " property:" + childProperty);
            }
        }

        this.paramClass = paramClass;
    }

    public static void groupFields(List<String> allFieldsOfLine, List<String> ownFields, Map<String, List<String>> fieldsByChild) {
        for (String f : allFieldsOfLine) {
            if (f.contains(".")) {
                int dotPos = f.indexOf('.');
                String childName = f.substring(0, dotPos);
                List<String> list = fieldsByChild.get(childName);
                if (list == null)
                    fieldsByChild.put(childName, list = new ArrayList<String>());
                list.add(f.substring(dotPos + 1));
            } else {
                ownFields.add(f);
            }
        }
    }

    public List convert(List<Map<String, Object>> maps) throws IllegalAccessException, InstantiationException,
            InvocationTargetException {
        if (maps == null) {
            return new ArrayList();
        }
        List beanList = new ArrayList(maps.size());
        for (Map<String, Object> map : maps) {
            Object object = convertObject(map, true);
            if (object != null) // object is already present il the result as a parent containing multiple child objects
                beanList.add(object);
        }
        return beanList;
    }

    public Object convertObject(Map<String, Object> map, boolean returnNullIfAlreadyPresent) throws IllegalAccessException, InstantiationException,
            InvocationTargetException {

        Object bean = paramClass.newInstance();

        populate(bean, map);

        List<String> fieldsToCompare = new ArrayList<String>(ownFields);
        fieldsToCompare.addAll(childMappers.keySet());

        MappingEntry mappingEntry = new MappingEntry(paramClass, fieldsToCompare, bean);

        Object pooledEntry = pool.get(mappingEntry);
        boolean fromPool;
        if (pooledEntry != null) {
            bean = pooledEntry;
            fromPool = true;
        } else {
            pool.put(mappingEntry, bean);
            fromPool = false;
        }

        boolean lineAdded = populateListObjects(bean, map);

        if (fromPool && lineAdded && returnNullIfAlreadyPresent)
            return null;
        else
            return bean;

    }

    private void populate(Object bean, Map<String, Object> map) throws InvocationTargetException, IllegalAccessException {

        Map<String, Object> data = new CaseInsensitiveMap(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            data.put(normalizePropertyName(entry.getKey()), entry.getValue());
        }

        populateOwnFields(bean, data);

        populateChildObjects(bean, map);

    }

    private boolean populateListObjects(Object bean, Map<String, Object> map) {
        boolean lineAdded = false;

        for (Map.Entry<String, BeanMapper> entry : childListMappers.entrySet()) {
            String originalKey = entry.getKey();
            String key = normalizePropertyName(entry.getKey());
            PropertyDescriptor propertyDescriptor = descriptorMap.get(key);
            if (propertyDescriptor == null) {

                LOG.info("Cannot find property for " + entry.getKey() + " in class " + paramClass.getCanonicalName());

            } else {

                Map<String, Object> childData = prepareChildData(originalKey, map);
                try {
                    Object childObject = entry.getValue().convertObject(childData, true);

                    List list = (List) propertyDescriptor.getReadMethod().invoke(bean);

                    if (list == null)
                        propertyDescriptor.getWriteMethod().invoke(bean, list = new ArrayList());

                    if (childObject != null)
                        list.add(childObject);

                    lineAdded = true;

                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }

        }
        return lineAdded;
    }

    private Map<String, Object> prepareChildData(String originalKey, Map<String, Object> map) {

        Map<String, Object> childData = new CaseInsensitiveMap(map.size());

        for (Map.Entry<String, Object> childEntry : map.entrySet()) {
            String childEntryKey = childEntry.getKey();
            if (childEntryKey.startsWith(originalKey + ".")) {
                childData.put(childEntryKey.substring(originalKey.length() + 1), childEntry.getValue());
            }
        }

        return childData;

    }

    private void populateChildObjects(Object bean, Map<String, Object> map) {
        for (Map.Entry<String, BeanMapper> entry : childMappers.entrySet()) {
            String originalKey = entry.getKey();
            String key = normalizePropertyName(originalKey);
            PropertyDescriptor propertyDescriptor = descriptorMap.get(key);
            if (propertyDescriptor == null) {
                LOG.info("Cannot find property for " + key + " in class " + paramClass.getCanonicalName());
            } else {
                Map<String, Object> childData = prepareChildData(originalKey, map);
                try {
                    Object childObject = entry.getValue().convertObject(childData, false);
                    propertyDescriptor.getWriteMethod().invoke(bean, childObject);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }

        }
    }

    private void populateOwnFields(Object bean, Map<String, Object> data) {
        for (String field : ownFields) {
            String key = normalizePropertyName(field);
            PropertyDescriptor propertyDescriptor = descriptorMap.get(key);
            if (propertyDescriptor == null) {
                LOG.info("Cannot find property for " + field + " in class " + paramClass.getCanonicalName());
            } else {
                try {
                    propertyDescriptor.getWriteMethod().invoke(bean,
                            MappingUtils.convert(propertyDescriptor.getPropertyType(), data.get(key), propertyDescriptor.getName()));
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    private String normalizePropertyName(String key) {
        return key.replaceAll("_", "");
    }

}
