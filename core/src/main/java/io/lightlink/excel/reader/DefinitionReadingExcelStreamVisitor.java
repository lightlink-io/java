package io.lightlink.excel.reader;

import io.lightlink.excel.AbstractExcelStreamVisitor;
import io.lightlink.excel.CellNode;
import io.lightlink.excel.RowNode;
import io.lightlink.excel.RowPrintCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefinitionReadingExcelStreamVisitor extends AbstractExcelStreamVisitor {

    public static final Logger LOG = LoggerFactory.getLogger(DefinitionReadingExcelStreamVisitor.class);

    private Map<String, MappingBean> targets = new HashMap<String, MappingBean>();

    protected void handleBinding(String property, RowNode rowNode, int i, RowPrintCallback rowPrintCallback) {
        List<CellNode> cells = rowNode.getCells();
        CellNode cell = cells.get(i);
        MappingBean mappingBean;

        if (property.endsWith("[][]"))
            mappingBean = new RegionMappingBean(cell.getCoordinates(), property.substring(0,property.length()-4));
        else if (property.endsWith("[]")) {
            mappingBean = new LineMappingBean(cell.getCoordinates(), property.substring(0,property.length()-2));
        } else {
            mappingBean = new MappingBean(cell.getCoordinates(), property);
        }

        targets.put(cell.getCoordinates(), mappingBean);
    }


    public Map<String, MappingBean> getTargets() {
        return targets;
    }
}
