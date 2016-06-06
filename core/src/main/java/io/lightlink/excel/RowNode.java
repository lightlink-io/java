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
