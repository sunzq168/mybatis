package com.sun.typeResolver;

import java.util.Map;

public class A<K, V> {
    private Map<String, Object> map;

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}
