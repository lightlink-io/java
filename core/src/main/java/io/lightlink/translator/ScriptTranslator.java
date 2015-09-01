package io.lightlink.translator;

import io.lightlink.utils.Utils;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.URL;

public class ScriptTranslator {

    public static final String SQL_CONTAINER_VARIABLE = "$SQL";
    public static final String APPEND_SQL_METHOD = "$appendSQL";

    public static boolean isBindingExpressionChar(char prev, char c, String s) {
        return (c >= '0' && c <= '9')
                || (c >= 'A' && c <= 'z')
                || c == '('
                || c == '.'
                || c == '_'
                || c == '-'  //tolerate "-" as binding part for UTF-8 blob encoding option
                || c == '['
                || (c == ']' && unclosed(s, '(', ')'))
                || (c == ')' && unclosed(s, '(', ')'));
    }

    private static boolean unclosed(String s, char openChar, char closeChar) {
        int score = 0;
        for (int i = 0; i < s.length() - 1; i++) {
            char c = s.charAt(i);
            score += c == openChar ? 1 : c == closeChar ? -1 : 0;
        }
        return score > 0;
    }

    public String translate(String scriptName, URL url, String content) throws IOException {
        String lines[] = content.split("\\r?\\n");
        StringBuilder res = new StringBuilder();

        boolean insideScript = false;

        for (String line : lines) {
            line = line.trim();
            if (line.length() == 0 || Utils.isBlank(line)) {
                res.append("\n");//don't transform empty lines but keep them for debugging and line counting

            } else if (line.startsWith("--%")) {
                res.append(line.substring("--%".length())).append("\n");

            } else {
                StringBuilder buffer = new StringBuilder();
                StringBuilder bufferOut = new StringBuilder();
                for (int j = 0; j < line.length(); j++) {
                    char c = line.charAt(j);
                    if (!insideScript && c == '<' && j < line.length() - 1 && line.charAt(j + 1) == '%') {
                        j++;
                        bufferOut.append(sqlBuffer(buffer.toString()));
                        buffer.setLength(0);
                        insideScript = true;
                    } else if (insideScript && c == '%' && j < line.length() - 1 && line.charAt(j + 1) == '>') {
                        j++;
                        appendScriptBuffer(buffer, bufferOut);

                        buffer.setLength(0);
                        insideScript = false;
                    } else {
                        buffer.append(c);
                    }
                }
                if (insideScript) {
                    appendScriptBuffer(buffer, bufferOut);
                } else {
                    bufferOut.append(sqlBuffer(buffer.toString() + "\n"));
                }

                res.append(bufferOut).append("\n");
            }

        }
        return res.toString();
    }

    private void appendScriptBuffer(StringBuilder buffer, StringBuilder bufferOut) {
        String script = buffer.toString();
        if (script.startsWith("=")) {
            script = APPEND_SQL_METHOD + "(" + script.substring(1) + ");";
        }
        bufferOut.append(script);
    }

    private String sqlBuffer(String buffer) {
        if (Utils.isBlank(buffer))
            return "";
        else {
            StringBuilder line = new StringBuilder(APPEND_SQL_METHOD).append("(").append(JSONValue.toJSONString(buffer));

            int pos = buffer.indexOf(":");
            while (pos != -1) {
                if (pos == 0 ||
                        (buffer.charAt(pos - 1) != ':'
                                && pos < buffer.length() - 1
                                && isBindingExpressionChar(buffer.charAt(pos), buffer.charAt(pos + 1), ""))
                        ) {
                    int pos2 = pos + 1;
                    while (pos2 < buffer.length()
                            && isBindingExpressionChar(buffer.charAt(pos2 - 1), buffer.charAt(pos2), buffer.substring(pos, pos2)))
                        pos2++;
                    String arg = buffer.substring(pos + 1, pos2).trim();
                    String argWithCasting = arg;

                    pos++;

                    int posBracket;
                    while (arg.charAt(0) == '(' && -1 != (posBracket = arg.indexOf(')'))) {
                        arg = arg.substring(posBracket + 1);
                    }

                    if (argWithCasting.contains("(out)")) {
                        // for (out) parameters in SP calls the argument name is not defines as JavaScript variable
                        // so putting null to avoid runtime error
                        line.append(",'").append(argWithCasting).append("',null");
                    } else {
                        line.append(",'").append(argWithCasting).append("',").append(arg);
                    }
                }
                pos = buffer.indexOf(":", pos + 1);
            }
            line.append(");");
            return line.toString();
        }
    }

}
