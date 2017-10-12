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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractExcelStreamVisitor extends ExcelStreamVisitor {

    @Override
    public void visit(RowNode rowNode, RowPrintCallback rowPrintCallback) {



        int rowRepeat = getRowRepeatCount(rowNode);
        for (int r = 0; r < rowRepeat; r++) {
            List<RowNode> rowNodes = new ArrayList<RowNode>();
            rowNodes.addAll(nextRow());
            rowNodes.add(rowNode);
            rowNodes.addAll(remainder());

            for (RowNode row : rowNodes) {
                if (!row.isHidden()) {

                    RowNode cloneRowNode = row.clone();
                    List<CellNode> cells = cloneRowNode.getCells();
                    int size = cells.size();
                    for (int i = 0; i < size; i++) {
                        CellNode cell = cells.get(i);
                        handleCell(cloneRowNode, rowPrintCallback, i, cell);
                    }
                    rowPrintCallback.printRowNode(cloneRowNode);
                }
            }
        }


    }

    protected abstract Collection<RowNode> remainder();

    protected List<RowNode> nextRow() {
        // do nothing by default
        return Collections.EMPTY_LIST;
    }

    protected int getRowRepeatCount(RowNode rowNode) {
        return 1;
    }

    protected void handleCell(RowNode rowNode, RowPrintCallback rowPrintCallback, int i, CellNode cell) {
        String s = cell.getDecodedValue();
        if (s != null && s.startsWith(":")) {
            String trimmedValue = s.trim();

            String property = trimmedValue.substring(1); // cut leading :
            handleBinding(property, rowNode, i, rowPrintCallback);

        }
    }

    protected void handleBinding(String property, RowNode rowNode, int i, RowPrintCallback rowPrintCallback) {

    }

}
