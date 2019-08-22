package cn.autolabor.module.networkhub.dependency;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态域
 * 组件被添加到动态域时，将执行一系列操作，以自动解算依赖关系和建立组件关联
 */
public final class DynamicScope {
    //组件集
    //  用于查找特定组件类型和判定类型冲突
    //  其中的组件只增加不减少
    private final ConcurrentHashSet<Component> _components = new ConcurrentHashSet<>();

    /**
     * 浏览所有组件
     */
    public final Collection<Component> components = _components.view;

    //依赖者列表
    //  用于在在新的依赖项到来时接收通知
    //  其中的组件一旦集齐依赖项就会离开列表，不再接收通知
    private final List<Dependent> dependents = new LinkedList<>();

    /**
     * 将一个新的组件加入到动态域，返回是否成功添加
     *
     * @return 若组件被添加到域，返回`true`
     * 与已有的组件发生冲突时返回`false`
     */
    public boolean setup(Component component) {
        if (_components.add(component)) {
            synchronized (dependents) {
                dependents.removeIf(it -> it.sync(component));

                if (component instanceof Dependent) {
                    Dependent it = (Dependent) component;
                    if (components.stream().noneMatch(it::sync))
                        dependents.add(it);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 线程安全的哈希集，仿照跳表集，基于映射构造
     */
    private final static class ConcurrentHashSet<T> {
        private final ConcurrentHashMap<T, Byte> core = new ConcurrentHashMap<>();
        public final Collection<T> view = core.keySet();

        public boolean add(T t) {
            return core.putIfAbsent(t, (byte) 0) == null;
        }
    }
}
