package io.lightlink.excel.reader;

import io.lightlink.excel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataReadingExcelStreamVisitor extends AbstractExcelStreamVisitor {

    public static final Logger LOG = LoggerFactory.getLogger(DataReadingExcelStreamVisitor.class);

    private Map<String, MappingBean> targets;
    private Map<String, Object> data = new HashMap<String, Object>();

    public DataReadingExcelStreamVisitor(Map<String, MappingBean> targets) {
        this.targets = targets;
    }

    protected void handleCell(RowNode rowNode, RowPrintCallback rowPrintCallback, int i, CellNode cell) {
        List<CellNode> cells = rowNode.getCells();
        MappingBean mappingBean;
        String cellDecodedValue = cell.getDecodedValue();

        //is mapping here ?
        MappingBean mappingHere = targets.get(cell.getCoordinates());
        if (mappingHere != null) {
            putData(cellDecodedValue, mappingHere);
        } else {
            // is line mapping on the left ?
            MappingBean mappingLeft = targets.get(left(cell.getCoordinates()));

            if (mappingLeft != null && mappingLeft instanceof LineMappingBean) {
                List value = (List) data.get(mappingLeft.getProperty());
                Object lastEl = value.get(value.size() - 1);
                if (lastEl instanceof List) // is RegionMappingBean ?
                    value = (List) lastEl;
                value.add(cellDecodedValue);
                targets.put(cell.getCoordinates(), mappingLeft);
            } else {
                MappingBean mappingAbove = targets.get(above(cell.getCoordinates()));

                if (mappingAbove != null && mappingAbove instanceof RegionMappingBean) {
                    List value = (List) data.get(mappingAbove.getProperty());
                    List<String> line = new ArrayList<String>();
                    value.add(line);
                    line.add(cellDecodedValue);
                    targets.put(cell.getCoordinates(), mappingAbove);
                }
            }

        }

    }

    private Object putData(String cellDecodedValue, MappingBean mappingHere) {
        String property = mappingHere.getProperty();
        Map<String, Object> map = data;
        int dotPos;
        while ((dotPos = property.indexOf(".")) != -1) {
            Map<String, Object> newMap = new HashMap<String, Object>();
            String containerProp = property.substring(0, dotPos);
            if (map.containsKey(containerProp)) {
                Object container = map.get(containerProp);
                if (container instanceof Map){
                    map = (Map<String, Object>) container;
                } else {
                    throw new IllegalArgumentException("Map expected for '" + container + "' in '" + mappingHere.getProperty()
                            + "'. Found class:" + container.getClass() + " value:" + container);
                }
            } else {
                map.put(containerProp, newMap);
                map = newMap;
            }
            property = property.substring(dotPos + 1);
        }

        return map.put(property, mappingHere.convertData(cellDecodedValue));
    }

    public Map<String, Object> getData() {
        return data;
    }

    private String left(String coordinates) {
        int colNum = ExcelUtils.toExcelColumnNumber(coordinates.replaceAll("[0-9]", ""));
        if (colNum == 0)
            return "";

        return ExcelUtils.toExcelColumnName(colNum - 1) + coordinates.replaceAll("[A-Za-z]", "");
    }

    private String above(String coordinates) {
        int rowNum = Integer.parseInt(coordinates.replaceAll("[A-Za-z]", "")) - 1;
        if (rowNum == 0)
            return "";
        else
            return coordinates.replaceAll("[0-9]", "") + (rowNum);
    }


}
