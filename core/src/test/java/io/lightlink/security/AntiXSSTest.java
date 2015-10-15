package io.lightlink.security;

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
