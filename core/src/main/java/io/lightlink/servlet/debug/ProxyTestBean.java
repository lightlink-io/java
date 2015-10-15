package io.lightlink.servlet.debug;

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


import java.util.*;

public class ProxyTestBean {

    private List list;
    private Map map;



    public static ProxyTestBean getTestInstance() {
        HashMap hashMap = new HashMap();
        ArrayList arrayList = new ArrayList();

        arrayList.add("test");
        arrayList.add(123);
        arrayList.add(new Date());
        //arrayList.add(arrayList);

        hashMap.put("test","test");
        hashMap.put(123,123);
        hashMap.put(new Date(),new Date());
        // hashMap.put("self",hashMap);

        return new ProxyTestBean(arrayList, hashMap);
    }

    public List getList() {
        return list;
    }

    public Map getMap() {
        return map;
    }

    public ProxyTestBean(List<ProxyTestBean> list, Map<String, ProxyTestBean> map) {
        this.list = list;
        this.map = map;
    }

    public Date testDate(){
        return new Date();
    }

    public int testInt(){
        return 123;
    }

    public String testString(){
        return "testString";
    }

    public void testException(){
        throw new RuntimeException("TestException");
    }

}
