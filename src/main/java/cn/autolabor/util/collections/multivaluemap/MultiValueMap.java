package cn.autolabor.util.collections.multivaluemap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface MultiValueMap<K, V> extends Map<K, V> {

    /**
     * 设置Key-List<Value>，如果这个Key存在就被替换，不存在则被添加。
     *
     * @param key    key.
     * @param values values.
     * @see #set(Object, Object)
     */
    void set(K key, Collection<V> values);

    /**
     * 替换所有的Key-List<Value>。
     *
     * @param values values.
     */
    void set(Map<K, Collection<V>> values);

    /**
     * 拿到某一个Key的所有值。
     *
     * @param key key.
     * @return values.
     */
    Set<V> getValues(K key);

    /**
     * 删除key对应的一个value
     *
     * @param key
     * @param value
     */
    void removeValue(K key, V value);

    /**
     * 删除所有key中的value
     *
     * @param value
     */
    void removeValue(V value);

    /**
     * 删除key下的所有value
     *
     * @param key
     */
    void removeValues(K key);

    /**
     * 获取所有的value
     *
     * @return
     */
    Collection<Set<V>> multiValues();

    /**
     * 获取所有的entrySet
     *
     * @return
     */
    Set<Entry<K, Set<V>>> multiEntrySet();

}
