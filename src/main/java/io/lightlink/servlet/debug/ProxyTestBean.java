package io.lightlink.servlet.debug;

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
