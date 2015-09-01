package io.lightlink.types;

import io.lightlink.core.RunnerContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.*;

public class BlobConverter extends AbstractConverter {


    public static AbstractConverter getInstance() {
        return new BlobConverter();
    }

    private String encoding;

    @Override
    public void setConfig(String config) {
        encoding = config;
    }

    @Override
    public Object convertToJdbc(Connection connection, RunnerContext runnerContext, String name, Object value) throws IOException {
        if (value == null)
            return null;


        if (value instanceof InputStream && StringUtils.isBlank(encoding)) {
            return value;
        } else {

            byte[] array;
            if (value instanceof byte[]) {
                array = (byte[]) value;
            } else if (value instanceof InputStream) {
                array = IOUtils.toByteArray((InputStream) value);
            } else if (value instanceof String) {
                String str = (String) value;
                if (encoding.equalsIgnoreCase("base64"))
                    array = DatatypeConverter.parseBase64Binary(str);
                else
                    try {
                        array = str.getBytes(encoding);
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalArgumentException("Cannot convert value:" + value + " to Blob. Specified encoding:" + encoding + " not recognised. (note:default encoding for String is base64)");
                    }

            } else {
                throw new IllegalArgumentException("Cannot convert value:" + value + " of type:" + value.getClass().getName()
                        + " to Blob. int[] or String expected. (note:default encoding for String is base64)");
            }

            return new ByteArrayInputStream(array);
        }
    }

    @Override
    public Object readFromResultSet(ResultSet resultSet, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {

        InputStream stream = resultSet.getBinaryStream(pos);
        return inputStreamToReturnValue(stream);

    }

    @Override
    public Object readFromCallableStatement(CallableStatement cs, int pos, RunnerContext runnerContext, String colName) throws SQLException, IOException {

        InputStream stream = cs.getBlob(pos).getBinaryStream();
        return inputStreamToReturnValue(stream);

    }

    private Object inputStreamToReturnValue(InputStream stream) throws IOException {

        if (encoding.equalsIgnoreCase("base64")) {
            String res = DatatypeConverter.printBase64Binary(IOUtils.toByteArray(stream));
            stream.close();
            return res;
        } else if (StringUtils.isNotBlank(encoding)) {
            String res = new String(IOUtils.toByteArray(stream), encoding);
            stream.close();
            return res;
        } else
            return stream;
    }

    @Override
    public Integer getSQLType() {
        return Types.BLOB;
    }

}
