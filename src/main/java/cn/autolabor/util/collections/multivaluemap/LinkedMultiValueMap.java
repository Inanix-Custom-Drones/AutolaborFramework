package cn.autolabor.util.collections.multivaluemap;

import cn.autolabor.util.Sugar;

import java.util.*;

public class LinkedMultiValueMap<K, V> implements MultiValueMap<K, V> {

    protected Map<K, Set<V>> mSource = new LinkedHashMap<>();

    @Override
    public void set(K key, Collection<V> values) {
        mSource.put(key, new HashSet<>(values));
    }

    @Override
    public void set(Map<K, Collection<V>> values) {
        for (Map.Entry<K, Collection<V>> entry : values.entrySet()) {
            mSource.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
    }

    @Override
    public Set<V> getValues(K key) {
        return mSource.getOrDefault(key, null);
    }

    @Override
    public int size() {
        return mSource.size();
    }

    @Override
    public boolean isEmpty() {
        return mSource.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return mSource.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (Entry<K, Set<V>> entry : mSource.entrySet()) {
            if (entry.getValue().contains(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        throw Sugar.makeThrow("Unable to use this method, try to use getValues()");
    }

    @Override
    public V put(K key, V value) {
        Set<V> values = mSource.get(key);
        if (values != null) {
            values.add(value);
        } else {
            mSource.put(key, new HashSet<V>() {{
                add(value);
            }});
        }
        return value;
    }

    @Override
    public V remove(Object key) {
        throw Sugar.makeThrow("Unable to use this method, try to use removeValues()");
    }

    @Override
    public void removeValue(K key, V value) {
        Set<V> values = mSource.get(key);
        if (values != null) {
            values.remove(value);
            if (values.size() == 0) {
                mSource.remove(key);
            }
        }
    }

    @Override
    public void removeValue(V value) {
        for (K key : mSource.keySet()) {
            removeValue(key, value);
        }
    }

    @Override
    public void removeValues(K key) {
        mSource.remove(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry mEntry : m.entrySet()) {
            put((K) mEntry.getKey(), (V) mEntry.getValue());
        }
    }

    @Override
    public void clear() {
        mSource.clear();
    }

    @Override
    public Set<K> keySet() {
        return mSource.keySet();
    }

    @Override
    public Collection<V> values() {
        throw Sugar.makeThrow("Unable to use this method, try to use multiValues()");
    }

    @Override
    public Collection<Set<V>> multiValues() {
        return mSource.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw Sugar.makeThrow("Unable to use this method, try to use multiEntrySet()");
    }

    @Override
    public Set<Entry<K, Set<V>>> multiEntrySet() {
        return mSource.entrySet();
    }

    @Override
    public String toString() {
        return "LinkedMultiValueMap{" + "mSource=" + mSource + '}';
    }
}
