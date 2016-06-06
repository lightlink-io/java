package io.lightlink.excel;

import org.xml.sax.Attributes;

import java.util.Map;

public class CellNode extends AttrNode{

    private String formula;

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public CellNode(Attributes attrs) {
        super(attrs);
    }

    public CellNode(String value, String decodedValue, Map<String, Object> attributes) {
        super(attributes);
        this.value = value;
        this.decodedValue = decodedValue;
    }

    String value, decodedValue;


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void changeValue(String value) {
        getAttributes().put("t","inlineStr");
        this.value = value;
    }
    public void changeValue(Number value) {
        getAttributes().put("t","n");
        this.value = ""+value;
    }

    public String getDecodedValue() {
        return decodedValue;
    }

    public void setDecodedValue(String decodedValue) {
        this.decodedValue = decodedValue;
    }

    public CellNode clone(){
        return new CellNode(value, decodedValue, attributes);
    }

}
