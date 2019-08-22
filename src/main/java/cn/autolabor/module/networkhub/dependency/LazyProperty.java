package cn.autolabor.module.networkhub.dependency;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 线程安全的懒加载属性
 *
 * @param <T> 类型
 */
public final class LazyProperty<T> implements Supplier<T> {
    private final Supplier<T> func;
    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicReference<T> field = new AtomicReference<>();

    public LazyProperty(Supplier<T> func) {
        this.func = func;
    }

    @Override
    public T get() {
        return initialized.getAndSet(true)
                ? field.get()
                : field.updateAndGet((last) -> last == null ? func.get() : last);
    }
}
