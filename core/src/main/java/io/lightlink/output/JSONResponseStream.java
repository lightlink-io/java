package io.lightlink.output;

/*
 * #%L
 * lightlink-core
 * %%
 * Copyright (C) 2015 Vitaliy Shevchuk
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


import io.lightlink.config.ConfigManager;
import io.lightlink.core.Hints;
import io.lightlink.core.RunnerContext;
import io.lightlink.facades.TypesFacade;
import io.lightlink.security.AntiXSS;
import io.lightlink.spring.LightLinkFilter;
import io.lightlink.types.DateConverter;
import jdk.nashorn.api.scripting.JSObject;
import org.apache.commons.beanutils.BeanMap;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JSONResponseStream implements ResponseStream {
    public static final String PROGRESSIVE_KEY_STR = "\n\t \t\n";
    public static final byte[] PROGRESSIVE_KEY = PROGRESSIVE_KEY_STR.getBytes();

    public static final Charset CHARSET = Charset.forName("UTF-8");
    public static final byte[] TOKEN_ARR_START = "\":[".getBytes();
    public static final byte[] TOKEN_OBJ_START = "\":{".getBytes();
    public static final String STR_QUOT = "\\\"";
    public static final String STR_BACKSLASH = "\\\\";
    public static final String STR_B = "\\b";
    public static final String STR_F = "\\f";
    public static final String STR_N = "\\n";
    public static final String STR_R = "\\r";
    public static final String STR_T = "\\t";
    public static final String STR_SLASH = "\\/";
    public static final String STR_SLASH_U = "\\u";

    boolean ended = false;
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
    private char rootTagOpen = '{';
    private char rootTagClose = '}';

    public JSONResponseStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        commaNeeded = false;
        ident = 0;
        ended = false;
        started = false;

        initHints();

    }

    protected void initHints() {
        if (LightLinkFilter.isThreadLocalStreamingDataSet()) {
            Hints hints = LightLinkFilter.getThreadLocalStreamingData().getHints();
            if (hints != null) {
                progressiveBlockSizes = hints.getProgressiveBlockSizes();
                antiXSS = hints.isAntiXSS();
            }
        }
    }

    protected OutputStream getOutputStream() {
        return outputStream;
    }

    public RunnerContext getRunnerContext() {
        return runnerContext;
    }

    public void setRunnerContext(RunnerContext runnerContext) {
        this.runnerContext = runnerContext;
    }


    private void identIn() {
        writeIdent();
        ident++;
    }

    private void identOut() {
        ident--;
        writeIdent();
    }

    private void writeIdent() {
        if (debug) {
            write('\n');
            for (int i = 0; i < ident; i++)
                write('\t');
        }
    }

    private void write(char c) {
        try {
            outputStream.write(c);
        } catch (IOException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    protected void write(byte[] byes, int size) {
        try {
            outputStream.write(byes, 0, size);
        } catch (IOException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    protected void write(byte[] byes) {
        try {
            outputStream.write(byes);
        } catch (IOException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }


    private void beginIfNeeded() {
        if (!started) {
            started = true;
            write(rootTagOpen);
            openBracesCount++;

            ident++;
        }
    }

    @Override
    public synchronized void end() {
        if (!ended) {
            beginIfNeeded();
            write(rootTagClose);
            openBracesCount--;
            ended = true;
        }
    }

    @Override
    public synchronized void writeProperty(String name, Object value) {
        beginIfNeeded();

        comma();

        identIn();
        write('"');
        writeEscapedString(name);
        write('"');
        write(':');

        commaNeeded = false;
        writeFullObjectToArray(value);
        ident--;
        commaNeeded = true;
    }

    @Override
    public synchronized void writeFullObjectToArray(Object value) {
        beginIfNeeded();

        if (value != null && value.getClass().getName().equals("jdk.nashorn.api.scripting.JSObject")) {
            JSObject jsObject = (JSObject) value;
            if (jsObject.isArray()) {
                Object[] array = new Object[((Number) jsObject.getMember("length")).intValue()];
                writeArrayStart();
                for (int i = 0; i < array.length; i++) {
                    writeFullObjectToArray(genericDateConvert(jsObject.getSlot(i)));
                }
                writeArrayEnd();
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
                Map<Object, Object> map = (Map<Object, Object>) value;
                if (value instanceof BeanMap) {
                    for (Map.Entry<Object, Object> entry : map.entrySet()) {
                        if (!"class".equals(entry.getKey())) {
                            writeProperty(entry.getKey() + "", entry.getValue());
                        }
                    }
                } else {
                    for (Map.Entry<Object, Object> entry : map.entrySet()) {
                        writeProperty(entry.getKey() + "", entry.getValue());
                    }
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
                writeArrayEnd();
            } else if (value instanceof Date) {
                String dateFormat;
                if (getRunnerContext() != null && getRunnerContext().getTypesFacade().getCustomDatePattern() != null) {
                    TypesFacade tf = getRunnerContext().getTypesFacade();
                    dateFormat = tf.getCustomDatePattern();
                } else
                    dateFormat = DateConverter.UNIVERSAL_DATE_FORMAT;

                writeString(new SimpleDateFormat(dateFormat).format(value));

            } else if (value == null) {

                writeUnquoted("null");

            } else if (value instanceof Number || value instanceof Boolean) {

                writeUnquoted(value);

            } else {

                writeString(value.toString());

            }

            commaNeeded = true;
        }


    }

    public void writeUnquoted(Object value) {
        ByteBuffer byteBuffer = CHARSET.encode(value.toString());
        write(byteBuffer.array(), byteBuffer.remaining());
    }


    public void writeFromReader(Reader reader) {
        write('"');
        char[] buffer = new char[16000];
        long count = 0;
        int n = 0;
        try {
            while (-1 != (n = reader.read(buffer))) {

                ByteBuffer bbuffer = CHARSET.encode(CharBuffer.wrap(buffer, 0, n));
                write(bbuffer.array(), bbuffer.remaining());

                count += n;
            }
            write('"');
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public void writeInputStream(InputStream inputStream) {
        write('"');

        byte[] buffer = new byte[3 * 1024];
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                String encodedBlock;
                if (length < buffer.length) {
                    byte part[] = new byte[length];
                    System.arraycopy(buffer, 0, part, 0, length);
                    encodedBlock = DatatypeConverter.printBase64Binary(part);
                } else {
                    encodedBlock = DatatypeConverter.printBase64Binary(buffer);
                }
                ByteBuffer bbuffer = CHARSET.encode(encodedBlock);
                write(bbuffer.array(), bbuffer.remaining());

            }
            write('"');

            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public void writeString(String valueStr) {
        if (antiXSS)
            valueStr = AntiXSS.escape(valueStr);

        write('\"');
        writeEscapedString(valueStr);
        write('\"');
    }

    private void writeEscapedString(String valueStr) {
        char[] chars = valueStr.toCharArray();
        StringBuffer sb = new StringBuffer(valueStr);
        for (int i = chars.length - 1; i >= 0; i--) {
            char ch = chars[i];
            switch (ch) {
                case '"':
                    sb.replace(i, i + 1, STR_QUOT);
                    break;
                case '\\':
                    sb.replace(i, i + 1, STR_BACKSLASH);
                    break;
                case '\b':
                    sb.replace(i, i + 1, STR_B);
                    break;
                case '\f':
                    sb.replace(i, i + 1, STR_F);
                    break;
                case '\n':
                    sb.replace(i, i + 1, STR_N);
                    break;
                case '\r':
                    sb.replace(i, i + 1, STR_R);
                    break;
                case '\t':
                    sb.replace(i, i + 1, STR_T);
                    break;
                case '/':
                    sb.replace(i, i + 1, STR_SLASH);
                    break;
                default:
                    //Reference: http://www.unicode.org/versions/Unicode5.1.0/
                    if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
                        StringBuilder encoded = new StringBuilder();
                        String ss = Integer.toHexString(ch).toUpperCase();
                        encoded.append(STR_SLASH_U);
                        for (int k = 0; k < 4 - ss.length(); k++) {
                            encoded.append('0');
                        }
                        encoded.append(ss.toUpperCase());
                        sb.replace(i, i + 1, encoded.toString());
                    }
            }
        }
        ByteBuffer buffer = CHARSET.encode(sb.toString());
        write(buffer.array(), buffer.remaining());

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
    public synchronized void writePropertyObjectStart(String name) {
        beginIfNeeded();
        comma();
        identIn();
        write('"');
        writeEscapedString(name);
        write(TOKEN_OBJ_START);
        openBracesCount++;
        commaNeeded = false;
    }


    @Override
    public synchronized void writePropertyObjectEnd() {
        beginIfNeeded();
        identOut();
        write('}');
        openBracesCount--;
        commaNeeded = true;
    }

    @Override
    public synchronized void writePropertyArrayStart(String name) {
        beginIfNeeded();
        comma();
        identIn();
        write('"');
        writeEscapedString(name);
        write(TOKEN_ARR_START);
        currentProgressiveBlock = 0;
        currentRowNum = 0;

        commaNeeded = false;

    }

    @Override
    public synchronized void writePropertyArrayEnd() {
        writeArrayEnd();
    }

    @Override
    public synchronized void writeObjectStart() {
        beginIfNeeded();
        comma();
        identIn();
        write('{');
        openBracesCount++;
        commaNeeded = false;
    }

    @Override
    public synchronized void writeObjectEnd() {
        beginIfNeeded();
        identOut();
        write('}');
        openBracesCount--;
        commaNeeded = true;

        progressiveLoadingSupport();


    }

    public void writeArrayStart() {
        if (!started) {
            rootTagOpen = '[';
            rootTagClose = ']';
            beginIfNeeded();
        } else {
            beginIfNeeded();
            comma();
            identIn();
            write('[');

            currentProgressiveBlock = 0;
            currentRowNum = 0;

            commaNeeded = false;
        }

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

    private void progressiveLoadingSupport() {
        if (progressiveBlockSizes != null && progressiveBlockSizes.length != 0) {
            currentRowNum++;


            if (progressiveBlockSizes.length <= currentProgressiveBlock) {
                return;
            }
            int currentBlockSize = progressiveBlockSizes[currentProgressiveBlock];

            if (currentRowNum >= currentBlockSize) {
                currentProgressiveBlock++;
                currentRowNum = 0;

                for (int i = 0; i < openBracesCount; i++) {
                    write('\t');//each <TAB> means one more brace to close to complete JSON request
                }
                write(PROGRESSIVE_KEY); // identify progressive loading block;

                try {
                    flushBuffer();
                } catch (IOException e) {
                    throw new RuntimeException(e.toString(), e);
                }
            }

        }
    }

    public void writeArrayEnd() {
        beginIfNeeded();
        identOut();
        write(']');
        commaNeeded = true;
    }

    private void comma() {
        if (commaNeeded) {
            write(',');
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
        outputStream.flush();
    }
}
