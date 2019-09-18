package cn.autolabor.module.networkhub;

import cn.autolabor.module.networkhub.dependency.Component;
import cn.autolabor.module.networkhub.dependency.DynamicScope;

import java.io.Closeable;
import java.io.IOException;

/**
 * 单例的远程终端内核
 */
public enum RemoteHub implements Closeable {
    ME;

    private final DynamicScope scope = new DynamicScope();

    /**
     * 装载一个组件
     *
     * @param component 新的组件
     * @param <T>       组件类型
     * @return 若同类组件存在，返回那个组件，否则装载新的组件并返回它
     */
    public <T extends Component> T setAndGet(T component) {
        if (scope.setup(component))
            return component;

        synchronized (scope) {
            for (Component d : scope.components)
                if (d.equals(component)) {
                    //noinspection unchecked
                    return (T) d;
                }
        }
        throw new RuntimeException("type system run into a exception");
    }

    @Override
    public void close() throws IOException {
        scope.components.stream()
            .filter(it -> it instanceof Closeable)
            .forEach(it -> {
                try {
                    ((Closeable) it).close();
                } catch (IOException e) {
                    // ignore
                }
            });
    }
}
