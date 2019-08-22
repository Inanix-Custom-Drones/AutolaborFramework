package cn.autolabor.module.networkhub.dependency;

/**
 * 抽象组件
 * 封装了默认的哈希函数和判等条件
 * 需要实现类提供自己的具体类型 [type]
 * 泛型 [T] 可保证此类型来自这个实现类
 */
public abstract class AbstractComponent<T extends AbstractComponent<T>> implements Component {
    private final Class type;

    protected AbstractComponent(Class<T> type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || type.isInstance(obj);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
