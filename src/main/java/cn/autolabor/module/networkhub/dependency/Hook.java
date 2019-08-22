package cn.autolabor.module.networkhub.dependency;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 组件钩子，用于保存组件的引用
 *
 * @param <T> 组件实际类型
 */
public class Hook<T extends Component> {
    private final Class<T> type;
    private T field;

    Hook(Class<T> type) {
        this.type = type;
    }

    /**
     * 转型并保存
     *
     * @param component 可能可以保存的组件
     * @return 是否保存成功
     */
    public boolean safeSet(Component component) {
        if (type.isInstance(component)) {
            field = (T) component;
            return true;
        }
        return false;
    }

    /**
     * 检查存在性
     */
    public T safeGet() {
        if (field == null)
            throw new ComponentNotExistException(type);
        return field;
    }

    /**
     * 安全执行
     */
    public void tryApply(Consumer<T> func) {
        if (field != null)
            func.accept(field);
    }

    /**
     * 安全转换或采用默认值
     */
    public <U> U tryLetOrDefault(U defaultValue, Function<T, U> func) {
        return field != null ? func.apply(field) : defaultValue;
    }
}
