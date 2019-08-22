package cn.autolabor.util.collections;

import cn.autolabor.util.Sugar;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DuplexMap<K, V> implements Map<K, V> {

    private HashMap<K, Entry> kEntryMap = new HashMap<>();
    private HashMap<V, Entry> vEntryMap = new HashMap<>();
    private ReadWriteLock rwlock = new ReentrantReadWriteLock();

    public DuplexMap() {
    }

    @Override
    public int size() {
        rwlock.readLock().lock();
        try {
            return kEntryMap.size();
        } finally {
            rwlock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        rwlock.readLock().lock();
        try {
            return kEntryMap.isEmpty();
        } finally {
            rwlock.readLock().unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        rwlock.readLock().lock();
        try {
            return kEntryMap.containsKey(key);
        } finally {
            rwlock.readLock().unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        rwlock.readLock().lock();
        try {
            return vEntryMap.containsKey(value);
        } finally {
            rwlock.readLock().unlock();
        }
    }

    @Override
    public V get(Object key) {
        rwlock.readLock().lock();
        try {
            Entry e = kEntryMap.get(key);
            if (e == null) {
                return null;
            }
            return (V) e.getValue();
        } finally {
            rwlock.readLock().unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        rwlock.writeLock().lock();
        try {
            if (key == null || value == null) {
                throw Sugar.makeThrow("key or value cannot be empty");
            }
            Entry e = new Entry(key, value);
            if (containsKey(key)) {
                removeByKey(key);
            }
            if (containsValue(value)) {
                removeByValue(value);
            }
            kEntryMap.put(key, e);
            vEntryMap.put(value, e);
            return value;
        } finally {
            rwlock.writeLock().unlock();
        }
    }

    public K removeByValue(V value) {
        rwlock.writeLock().lock();
        try {
            Entry e = vEntryMap.remove(value);
            if (e == null) {
                return null;
            }
            kEntryMap.remove(e.getKey());
            return (K) e.getKey();
        } finally {
            rwlock.writeLock().unlock();
        }
    }

    public V removeByKey(K key) {
        rwlock.writeLock().lock();
        try {
            Entry e = kEntryMap.remove(key);
            if (e == null) {
                return null;
            }
            vEntryMap.remove(e.getValue());
            return (V) e.getValue();
        } finally {
            rwlock.writeLock().unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        rwlock.writeLock().lock();
        try {
            for (Map.Entry mEntry : m.entrySet()) {
                put((K) mEntry.getKey(), (V) mEntry.getValue());
            }
        } finally {
            rwlock.writeLock().unlock();
        }

    }

    @Override
    public void clear() {
        rwlock.writeLock().lock();
        try {
            kEntryMap.clear();
            vEntryMap.clear();
        } finally {
            rwlock.writeLock().unlock();
        }
    }

    @Override
    public Set<K> keySet() {
        rwlock.readLock().lock();
        try {
            return new HashSet<>(kEntryMap.keySet());
        } finally {
            rwlock.readLock().unlock();
        }
    }

    @Override
    public Collection<V> values() {
        rwlock.readLock().lock();
        try {
            return new HashSet<>(vEntryMap.keySet());
        } finally {
            rwlock.readLock().unlock();
        }
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        rwlock.readLock().lock();
        try {
            HashSet<Map.Entry<K, V>> entries = new HashSet<>();
            for (Entry entry : kEntryMap.values()) {
                entries.add(entry);
            }
            return entries;
        } finally {
            rwlock.readLock().unlock();
        }
    }

    public K getbyValue(V v) {
        rwlock.readLock().lock();
        try {
            Entry e = vEntryMap.get(v);
            if (e == null) {
                return null;
            }
            return (K) e.getKey();
        } finally {
            rwlock.readLock().unlock();
        }
    }

    public Entry<K, V> getEntryByKey(K key) {
        rwlock.readLock().lock();
        try {
            return kEntryMap.get(key);
        } finally {
            rwlock.readLock().unlock();
        }
    }

    public Entry<K, V> getEntryByValue(V value) {
        rwlock.readLock().lock();
        try {
            return vEntryMap.get(value);
        } finally {
            rwlock.readLock().unlock();
        }
    }

    public V getValueByValue(V value) {
        rwlock.readLock().lock();
        try {
            Entry entry = vEntryMap.get(value);
            if (entry == null) {
                return null;
            }
            return (V) entry.getValue();
        } finally {
            rwlock.readLock().unlock();
        }
    }

    public K getKeyByKey(K key) {
        rwlock.readLock().lock();
        try {
            Entry entry = kEntryMap.get(key);
            if (entry == null) {
                return null;
            }
            return (K) entry.getKey();
        } finally {
            rwlock.readLock().unlock();
        }
    }

    public boolean contains(K key) {
        return containsKey(key);
    }

    public V getByKey(K key) {
        return get(key);
    }

    @Override
    public V remove(Object key) {
        return removeByKey((K) key);
    }

    @Override
    public String toString() {
        return kEntryMap.values().toString();
    }


    public class Entry<K, V> implements Map.Entry {
        K key;
        V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            this.value = (V) value;
            return value;
        }

        @Override
        public String toString() {
            return "Entry{" + "k=" + key + ", v=" + value + '}';
        }
    }
}
