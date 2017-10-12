package io.lightlink.excel;

/*
 * #%L
 * LightLink Core
 * %%
 * Copyright (C) 2015 - 2016 Vitaliy Shevchuk
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


import io.lightlink.utils.Utils;
import jdk.nashorn.internal.objects.NativeDate;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WritingExcelStreamVisitor extends AbstractExcelStreamVisitor {

    public static final Logger LOG = LoggerFactory.getLogger(WritingExcelStreamVisitor.class);

    private class AfterValueChangeListeners {
        boolean firstLine = true;
        private Object oldValue;
        private String propertyName;
        private RowNode rowNode;

        public AfterValueChangeListeners(String propertyName, RowNode rowNode) {
            this.propertyName = propertyName;
            this.rowNode = rowNode;
        }

        RowNode nextData(Object data) {
            boolean firstLine = this.firstLine;
            this.firstLine = false;

            Object newValue = getPropertyValue(data, propertyName);
            boolean equals = (oldValue == null && newValue == null || (oldValue != null && oldValue.equals(newValue)));
            boolean addSummary = !firstLine && !equals;
            oldValue = newValue;
            return addSummary ? rowNode : null;
        }
    }

    private Map<String, Object> data = new HashMap<String, Object>();
    private DateFormat dateFormat;

    private Iterator currentRowIterator;
    private String currentRowProperty;
    private List<AfterValueChangeListeners> afterValueChangeListeners = new ArrayList<AfterValueChangeListeners>();

    public WritingExcelStreamVisitor(Map<String, Object> data, DateFormat dateFormat) {
        this.data.putAll(data);
        this.dateFormat = dateFormat;
    }

    @Override
    protected List<RowNode> nextRow() {
        List<RowNode> res = new ArrayList<RowNode>();
        if (currentRowIterator != null && currentRowIterator.hasNext()) {
            Object data = currentRowIterator.next();
            this.data.put(currentRowProperty, data);
            for (AfterValueChangeListeners afterValueChangeListener : afterValueChangeListeners) {
                RowNode groupFooterRowNode = afterValueChangeListener.nextData(data);
                if (groupFooterRowNode != null)
                    res.add(groupFooterRowNode.clone());
            }
        }
        return res;
    }

    @Override
    protected List<RowNode> remainder() {
        List<RowNode> res = new ArrayList<RowNode>();
        if (currentRowIterator != null && !currentRowIterator.hasNext()) {
            for (AfterValueChangeListeners afterValueChangeListener : afterValueChangeListeners) {
                res.add(afterValueChangeListener.rowNode.clone());
            }

            afterValueChangeListeners.clear();
            currentRowIterator = null;

        }
        return res;
    }

    @Override
    protected int getRowRepeatCount(RowNode rowNode) {
        List<CellNode> cells = rowNode.getCells();

        if (cells.size() > 0 && cells.get(0).getDecodedValue() != null) {
            String decodedValue = cells.get(0).getDecodedValue();
            if (decodedValue.toUpperCase().startsWith(":ON AFTER CHANGE")) {
                String propertyName = decodedValue.substring(":ON AFTER CHANGE".length()).trim();
                afterValueChangeListeners.add(new AfterValueChangeListeners(propertyName, rowNode));
                rowNode.setHidden(true);
            } else {
                Matcher m = Pattern.compile("^:[a-zA-Z_0-9\\.]+\\[\\]\\.").matcher(decodedValue);
                if (m.find()) {
                    String s = m.group(0);
                    s = s.substring(1, s.length() - 1);

                    Object value = getPropertyValue(data, s.substring(0, s.length() - 2));
                    if (value != null) {
                        if (value instanceof Object[])
                            value = Arrays.asList((Object[]) value);
                        if (value instanceof Collection) {
                            Collection coll = (Collection) value;
                            currentRowIterator = coll.iterator();
                            currentRowProperty = s;
                            return coll.size();
                        }
                    }
                }
            }
        }

        return 1;
    }

    protected void handleBinding(String property, RowNode rowNode, int i, RowPrintCallback rowPrintCallback) {

        while (property.endsWith("[]"))               // cut ending [][]
            property = property.substring(0, property.length() - 2);

        List<CellNode> cells = rowNode.getCells();
        CellNode cell = cells.get(i);

        Object value = getPropertyValue(data, property);

        if (value == null) {
            cell.changeValue("");
            return;
        }

        if (value instanceof Number) {
            cell.changeValue((Number) value);
            return;
        }

        if (value instanceof NativeDate) {
            value = new Date((long) NativeDate.getTime(value));
        }
        if (value instanceof Date && dateFormat != null) {
            value = dateFormat.format((Date) value);
        }


        value = Utils.tryConvertToJavaCollections(value);

        if (value instanceof Map) {
            value = Collections.singletonList(value);
        }

        if (value instanceof List) {
            List list = (List) value;

            cells.remove(i);

            if (list.isEmpty())
                return;

            Object mayBeCollection = Utils.tryConvertToJavaCollections(list.get(0));
            if (!(mayBeCollection instanceof Map) && !(mayBeCollection instanceof List)) {
                list = Collections.singletonList(list); // one dimentional array as 2 dimention of one row
            }
            for (int l = 0; l < list.size(); l++) {
                Object line = list.get(l);
                if (line instanceof Map)
                    line = ((Map) line).values();

                line = Utils.tryConvertToJavaCollections(line);

                if (line instanceof Collection) {
                    Collection lineList = (Collection) line;
                    if (l == 0) { // first line
                        for (int j = 0; j < lineList.size(); j++) {
                            CellNode cc = cell.clone();
                            cells.add(j + i, cc);
                            cc.changeValue("");
                        }
                    }
                    int j = 0;
                    for (Iterator iterator = lineList.iterator(); iterator.hasNext(); j++) {
                        Object propertyValue = iterator.next();

                        CellNode cc = cells.get(i + j);

                        if (propertyValue == null) {
                            propertyValue = "";
                        }
                        if (propertyValue instanceof Date) {
                            propertyValue = dateFormat.format((Date) value);
                        }
                        if (propertyValue instanceof Number)
                            cc.changeValue((Number) propertyValue);
                        else
                            cc.changeValue(propertyValue.toString());
                    }

                    if (l < list.size() - 1)
                        rowPrintCallback.printRowNode(rowNode);

                }

            }


        } else {

            cell.changeValue(value.toString());
        }
    }

    private Object getPropertyValue(Object data, String property) {

        if (data instanceof Map && ((Map) data).containsKey(property))
            return ((Map) data).get(property);

        int pos = property.indexOf("[].");
        if (pos != -1) {
            String containerProperty = property.substring(0, pos + 2);
            String subProperty = property.substring(pos + 3);
            Object containerObj = getPropertyValue(data, containerProperty);
            return getPropertyValue(containerObj, subProperty);
        }

        try {
            return PropertyUtils.getProperty(data, property);
        } catch (Exception e) {
            LOG.error(e.toString(), e);
            return null;
        }

    }

}
