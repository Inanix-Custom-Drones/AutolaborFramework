package cn.autolabor.util.collections;

import java.util.concurrent.ConcurrentHashMap;

public class DoubleKeyMap<T, U, V> {


    private ConcurrentHashMap<T, ConcurrentHashMap<U, V>> map;

    public DoubleKeyMap() {
        this.map = new ConcurrentHashMap<>();
    }

    public void put(T key1, U key2, V value) {
        ConcurrentHashMap<U, V> subMap = map.get(key1);
        if (subMap == null) {
            subMap = new ConcurrentHashMap<>();
            map.put(key1, subMap);
        }
        subMap.put(key2, value);
    }

    public V get(T key1, U key2) {
        ConcurrentHashMap<U, V> subMap = map.get(key1);
        if (subMap == null) {
            return null;
        } else {
            return subMap.get(key2);
        }
    }

    public boolean containsKey(T key1, U key2) {
        return map.containsKey(key1) && map.get(key1).containsKey(key2);
    }

    public void remove(T key1, U key2) {
        ConcurrentHashMap<U, V> subMap = map.get(key1);
        if (subMap != null) {
            subMap.remove(key2);
        }
    }

    public ConcurrentHashMap<T, ConcurrentHashMap<U, V>> getMap() {
        return map;
    }
}

