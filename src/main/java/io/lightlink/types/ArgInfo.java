package io.lightlink.types;

public class ArgInfo {


    private boolean in=true, out=false;
    private String name;
    private AbstractConverter converter;
    private Object value;

    public ArgInfo(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isIn() {
        return in;
    }

    public void setIn(boolean in) {
        this.in = in;
    }

    public boolean isOut() {
        return out;
    }

    public void setOut(boolean out) {
        this.out = out;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AbstractConverter getConverter() {
        return converter;
    }

    public void setConverter(AbstractConverter converter) {
        this.converter = converter;
    }

    public Integer findOutSqlType() {

        if (getConverter()!=null)
            return getConverter().getSQLType();
        else
            return null;

    }


    public String findOutSqlTypeName() {
        if (getConverter()!=null)
            return getConverter().getCustomSQLTypeName();
        else
            return null;


    }

    @Override
    public String toString() {
        return "ArgInfo{" +
                "in=" + in +
                ", out=" + out +
                ", name='" + name + '\'' +
                ", converter=" + converter +
                ", value=" + value +
                '}';
    }
}
