package io.lightlink.excel.reader;

import java.util.ArrayList;
import java.util.List;

public class RegionMappingBean extends LineMappingBean {

    public RegionMappingBean(String coordinates, String property) {
        super(coordinates, property);
    }

    public Object convertData(String value) {
        List<List<String>> res = new ArrayList<List<String>>();
        List<String> line = new ArrayList<String>();
        res.add(line);
        line.add(value);
        return res;
    }

}
