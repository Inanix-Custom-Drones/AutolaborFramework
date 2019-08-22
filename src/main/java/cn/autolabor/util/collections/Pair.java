package cn.autolabor.util.collections;

import java.io.Serializable;
import java.util.Objects;

public class Pair<K, V> implements Serializable {

    private V value;
    private K key;


    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        return Objects.equals(value, pair.value) && Objects.equals(key, pair.key);
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }


}
