package io.lightlink.security;

import junit.framework.TestCase;

public class AntiXSSTest extends TestCase{

    public void test (){
        String[] strings = new String[]{"fqlskdfq&lkjmlkj","&amp;&lt;","dsd{][}\"&<script>'++"};
        for (String string : strings) {
            String escaped = AntiXSS.escape(string);
            assertEquals(string,AntiXSS.unescape(escaped));
        }
    }

}
