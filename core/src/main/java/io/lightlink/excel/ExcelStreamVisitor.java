package io.lightlink.excel;


public class ExcelStreamVisitor {

    public void visit(RowNode rowNode, RowPrintCallback rowPrintCallback) {
        rowPrintCallback.printRowNode(rowNode);
    }

}
