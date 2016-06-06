package io.lightlink.excel;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RowNode extends AttrNode {

    List<CellNode> cells = new ArrayList<CellNode>();

    public RowNode(Map<String, Object> attributes) {
        super(attributes);
    }

    public RowNode(Attributes attrs) {
        super(attrs);
    }


    public List<CellNode> getCells() {
        return cells;
    }

    public void setCells(List<CellNode> cells) {
        this.cells = cells;
    }

    public RowNode clone(){
        RowNode rowNode = new RowNode(getAttributes());
        rowNode.cells = new ArrayList(cells);
        return rowNode;
    }
}
