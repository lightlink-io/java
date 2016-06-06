package io.lightlink.excel.reader;

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
