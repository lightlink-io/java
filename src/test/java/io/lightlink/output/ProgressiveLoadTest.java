package io.lightlink.output;

import io.lightlink.core.Hints;
import junit.framework.TestCase;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProgressiveLoadTest extends TestCase {

    public void test() throws IOException, ParseException {

        JSONStringBufferResponseStream bufferResponseStream = new JSONStringBufferResponseStream();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("!progressive", new Integer[]{100, 1000, 5000});
        bufferResponseStream.setHints(Hints.fromParams(params));

        bufferResponseStream.writePropertyArrayStart("resultSet");
        for (int i=0;i<11500;i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("value1",Math.random());
            map.put("value2",Math.random());
            map.put("value3",Math.random());

            bufferResponseStream.writeFullObjectToArray(map);
        }
        bufferResponseStream.writePropertyArrayEnd();

        String responseText = bufferResponseStream.getBuffer();
        int lastIndex = responseText.lastIndexOf(JSONResponseStream.PROGRESSIVE_KEY);

        responseText = responseText.substring(0, lastIndex);
        lastIndex--;

        responseText += "]";

        while (lastIndex > 0 && responseText.charAt(lastIndex) == '\t') {
            lastIndex--;
            responseText += "}";
        }

        Map res = (Map) new JSONParser().parse(responseText);
        Collection resultSet = (Collection) res.get("resultSet");
        assertEquals(resultSet.size(),11100);

    }
}
