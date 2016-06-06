package io.lightlink.excel;

import java.util.List;

public abstract class AbstractExcelStreamVisitor extends ExcelStreamVisitor {

    @Override
    public void visit(RowNode rowNode, RowPrintCallback rowPrintCallback) {


        List<CellNode> cells = rowNode.getCells();
        int size = cells.size();

        int rowRepeat = getRowRepeatCount(rowNode);
        for (int r = 0; r < rowRepeat; r++) {
            nextRow();
            RowNode cloneRowNode = rowNode.clone();
            for (int i = 0; i < size; i++) {
                CellNode cell = cells.get(i);
                handleCell(cloneRowNode, rowPrintCallback, i, cell);
            }
            rowPrintCallback.printRowNode(cloneRowNode);
        }

    }

    protected void nextRow() {
        // do nothing by default
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
