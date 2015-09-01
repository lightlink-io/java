package io.lightlink.output;

import io.lightlink.config.ConfigManager;
import io.lightlink.core.Hints;
import io.lightlink.core.RunnerContext;
import io.lightlink.facades.TypesFacade;
import io.lightlink.security.AntiXSS;
import io.lightlink.types.DateConverter;
import jdk.nashorn.api.scripting.JSObject;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONValue;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class JSONResponseStream implements ResponseStream {
    public static final String PROGRESSIVE_KEY = "\n\t \t\n";

    boolean ended = false;
    Writer out;
    int ident = 0;
    boolean debug = ConfigManager.isInDebugMode();
    RunnerContext runnerContext;
    private boolean commaNeeded;
    private boolean started;

    private int[] progressiveBlockSizes;
    private int currentProgressiveBlock;
    private int currentRowNum;
    private int openBracesCount;

    private boolean antiXSS;
    private OutputStream outputStream;

    public JSONResponseStream(OutputStream outputStream) {
        try {
            this.outputStream = outputStream;
            this.out = new OutputStreamWriter(outputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString(), e); // should never happen
        }
        commaNeeded = false;
        ident = 0;
        ended = false;
        started = false;
    }

    protected OutputStream getOutputStream() {
        return outputStream;
    }

    public Writer getOut() throws IOException {
        return out;
    }

    public RunnerContext getRunnerContext() {
        return runnerContext;
    }

    public void setRunnerContext(RunnerContext runnerContext) {
        this.runnerContext = runnerContext;
    }

    @Override
    public void setHints(Hints hints) {
        if (hints != null) {
            progressiveBlockSizes = hints.getProgressiveBlockSizes();
            antiXSS = hints.isAntiXSS();
        }
    }

    private void identIn() throws IOException {
        writeIdent();
        ident++;
    }

    private void identOut() throws IOException {
        ident--;
        writeIdent();
    }

    private void writeIdent() throws IOException {
        if (debug) {
            getOut().write("\n");
            for (int i = 0; i < ident; i++)
                getOut().write("\t");
        }
    }


    private void beginIfNeeded() throws IOException {
        if (!started) {
            started = true;
            getOut().write("{");
            openBracesCount++;

            ident++;
        }
    }

    @Override
    public synchronized void end() throws IOException {
        if (!ended) {
            beginIfNeeded();
            getOut().write("\n}");
            openBracesCount--;

            flushBuffer();
            ended = true;
        }
    }

    @Override
    public synchronized void writeProperty(String name, Object value) throws IOException {
        beginIfNeeded();
        comma();

        identIn();
        getOut().write("\"");
        getOut().write(encodeQuotes(name));
        getOut().write("\":");
        commaNeeded = false;
        writeFullObjectToArray(value);
        ident--;
        commaNeeded = true;
    }

    @Override
    public synchronized void writeFullObjectToArray(Object value) throws IOException {
        beginIfNeeded();

        if (value instanceof JSObject) {
            JSObject jsObject = (JSObject) value;
            if (jsObject.isArray()) {
                Object[] array = new Object[((Number) jsObject.getMember("length")).intValue()];
                writeArrayStart();
                for (int i = 0; i < array.length; i++) {
                    writeFullObjectToArray(genericDateConvert(jsObject.getSlot(i)));
                }
                writeArryaEnd();
            } else {
                writeObjectStart();
                for (String key : jsObject.keySet()) {
                    writeProperty(key, genericDateConvert(jsObject.getMember(key)));
                }
                writeObjectEnd();
            }
        } else {

            comma();

            List list = new ArrayList();
            value = handlePrimitiveArrays(value, list);
            if (value instanceof Map) {
                writeObjectStart();
                for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                    writeProperty(entry.getKey() + "", entry.getValue());
                }
                writeObjectEnd();
            } else if (value instanceof List) {
                writeArrayStart();
                for (Object o : (List) value) {
                    writeFullObjectToArray(o);
                }
                writePropertyArrayEnd();
            } else if (value instanceof InputStream) {
                writeInputStream((InputStream) value);
            } else if (value instanceof Reader) {
                writeFromReader((Reader) value);
            } else if (value instanceof Object[]) {
                writeArrayStart();
                for (Object o : (Object[]) value) {
                    writeFullObjectToArray(o);
                }
                writeArryaEnd();
            } else if (value instanceof Date) {

                TypesFacade tf = getRunnerContext().getTypesFacade();
                String dateFormat = tf.getCustomDatePattern();
                if (dateFormat == null)
                    dateFormat = DateConverter.UNIVERSAL_DATE_FORMAT;

                writeString(new SimpleDateFormat(dateFormat).format(value));

            } else if (value == null || value instanceof Number || value instanceof Boolean) {

                writeUnquoted(value);

            } else {

                writeString("" + value);

            }

            commaNeeded = true;
        }


    }

    public void writeUnquoted(Object value) throws IOException {
        getOut().write(value + "");
    }

    public void writeFromReader(Reader reader) throws IOException {
        Writer out = getOut();
        out.write('"');
        IOUtils.copyLarge(reader, out);
        out.write('"');

    }
    public void writeInputStream(InputStream inputStream) throws IOException {
        Writer out = getOut();
        out.write('"');

        byte[] buffer = new byte[3 * 1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            String encodedBlock;
            if (length < buffer.length) {
                byte part[] = new byte[length];
                System.arraycopy(buffer, 0, part, 0, length);
                encodedBlock = DatatypeConverter.printBase64Binary(part);
            } else {
                encodedBlock = DatatypeConverter.printBase64Binary(buffer);
            }
            out.write(encodedBlock);
        }
        out.write('"');

        inputStream.close();
    }

    public void writeString(String valueStr) throws IOException {
        if (antiXSS)
            valueStr = AntiXSS.escape(valueStr);

        JSONValue.writeJSONString(valueStr, out);
    }

    private Object handlePrimitiveArrays(Object value, List list) {
        if (value instanceof byte[]) {
            for (byte b : (byte[]) value) list.add(b);
            value = list;
        } else if (value instanceof short[]) {
            for (short b : (short[]) value) list.add(b);
            value = list;
        } else if (value instanceof int[]) {
            for (int b : (int[]) value) list.add(b);
            value = list;
        } else if (value instanceof long[]) {
            for (long b : (long[]) value) list.add(b);
            value = list;
        } else if (value instanceof float[]) {
            for (float b : (float[]) value) list.add(b);
        } else if (value instanceof double[]) {
            for (double b : (double[]) value) list.add(b);
            value = list;
        }
        return value;
    }

    private Object genericDateConvert(Object value) {
        if (value instanceof java.util.Date) {
            value = runnerContext.getTypesFacade().dateToString((java.util.Date) value);
        }
        return value;
    }

    @Override
    public synchronized void writePropertyObjectStart(String name) throws IOException {
        beginIfNeeded();
        comma();
        identIn();
        getOut().write("\"");
        getOut().write(encodeQuotes(name));
        getOut().write("\":{");
        openBracesCount++;
        commaNeeded = false;
    }

    @Override
    public synchronized void writePropertyObjectEnt() throws IOException {
        beginIfNeeded();
        identOut();
        getOut().write("}");
        openBracesCount--;
        commaNeeded = true;
    }

    @Override
    public synchronized void writePropertyArrayStart(String name) throws IOException {
        beginIfNeeded();
        comma();
        identIn();
        getOut().write("\"");
        getOut().write(encodeQuotes(name));
        getOut().write("\":[");

        currentProgressiveBlock = 0;
        currentRowNum = 0;

        commaNeeded = false;
    }

    @Override
    public synchronized void writePropertyArrayEnd() throws IOException {
        beginIfNeeded();
        identOut();
        getOut().write("]");
        commaNeeded = true;
    }

    @Override
    public synchronized void writeObjectStart() throws IOException {
        beginIfNeeded();
        comma();
        identIn();
        getOut().write("{");
        openBracesCount++;
        commaNeeded = false;
    }

    @Override
    public synchronized void writeObjectEnd() throws IOException {
        beginIfNeeded();
        identOut();
        getOut().write("}");
        openBracesCount--;
        commaNeeded = true;

        progressiveLoadingSupport();


    }

    private void writeArrayStart() throws IOException {
        beginIfNeeded();
        comma();
        identIn();
        getOut().write("[");

        currentProgressiveBlock = 0;
        currentRowNum = 0;

        commaNeeded = false;
    }

    @Override
    public boolean checkConnectionAlive() {
        try {
            flushBuffer();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private void progressiveLoadingSupport() throws IOException {
        if (progressiveBlockSizes != null && progressiveBlockSizes.length != 0) {
            currentRowNum++;

            int currentBlockSize = progressiveBlockSizes.length <= currentProgressiveBlock
                    ? progressiveBlockSizes[progressiveBlockSizes.length - 1]
                    : progressiveBlockSizes[currentProgressiveBlock];

            if (currentRowNum >= currentBlockSize) {
                currentProgressiveBlock++;
                currentRowNum = 0;

                for (int i = 0; i < openBracesCount; i++) {
                    getOut().write("\t");//each <TAB> means one more brace to close to complete JSON request
                }
                getOut().write(PROGRESSIVE_KEY); // identify progressive loading block;

                flushBuffer();

            }

        }
    }

    private void writeArryaEnd() throws IOException {
        beginIfNeeded();
        identOut();
        getOut().write("]");
        commaNeeded = true;
    }

    private void comma() throws IOException {
        if (commaNeeded) {
            getOut().write(',');
            commaNeeded = false;
        }
    }

    private String encodeQuotes(String name) {
        if (name.indexOf('"') == -1)
            return name;
        return name.replaceAll("\"", "\\\"");
    }

    @Override
    public void setContentType(String value) {
        // do nothing
    }

    @Override
    public void setHeader(String header, String value) {
        // do nothing

    }

    @Override
    public void flushBuffer() throws IOException {
        getOut().flush();
    }
}
