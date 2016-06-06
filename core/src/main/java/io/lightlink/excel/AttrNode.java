package io.lightlink.excel;

import org.xml.sax.Attributes;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: 303367
 * Date: 13/03/15
 * Time: 17:16
 */
public class AttrNode {

    Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    public AttrNode(Map<String, Object> attributes) {
        this.attributes = new LinkedHashMap<String, Object>(attributes);
    }

    public AttrNode(Attributes attrs) {
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name

                if ("".equals(aName)) {
                    aName = attrs.getQName(i);
                }

                attributes.put(aName, attrs.getValue(i));
            }
        }
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getCoordinates(){
        return (String) getAttributes().get("r");
    }

}
