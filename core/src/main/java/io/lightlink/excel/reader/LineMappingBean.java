package io.lightlink.excel.reader;

import java.util.ArrayList;
import java.util.List;

public class LineMappingBean extends MappingBean {


    public LineMappingBean(String coordinates, String property) {
        super(coordinates, property);
    }

    public Object convertData(String value) {
        List<String> res = new ArrayList<String>();
        res.add(value);
        return res;
    }


}
