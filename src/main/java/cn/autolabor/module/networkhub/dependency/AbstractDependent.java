package cn.autolabor.module.networkhub.dependency;

import java.util.LinkedList;
import java.util.List;

/**
 * 抽象依赖
 * 封装了依赖项管理功能以及默认的哈希函数和判等条件
 * 须向抽象组件提供自身具体类型
 */
public abstract class AbstractDependent<T extends AbstractDependent<T>>
        extends AbstractComponent<T> implements Dependent {
    // 缓存仍不全的依赖项
    private final List<Hook<?>> dependencies = new LinkedList<>();

    protected AbstractDependent(Class<T> type) {
        super(type);
    }

    /**
     * 每一次扫描都清除成功装载的依赖项
     */
    @Override
    public boolean sync(Component dependency) {
        return dependencies.removeIf(it -> it.safeSet(dependency))
                && dependencies.isEmpty();
    }

    /**
     * 添加一个新的依赖项钩子
     */
    protected <U extends Component> Hook<U> buildHook(Class<U> type) {
        Hook<U> hook = new Hook<>(type);
        dependencies.add(hook);
        return hook;
    }
}
