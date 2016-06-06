package io.lightlink.excel.reader;

public class MappingBean {

    private String coordinates, property;

    public MappingBean(String coordinates, String property) {
        this.coordinates = coordinates;
        this.property = property;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Object convertData(String value) {
        return value;
    }
}
