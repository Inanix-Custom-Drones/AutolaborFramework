package cn.autolabor.module.networkhub.dependency;

/**
 * 组件不存在异常
 */
class ComponentNotExistException extends RuntimeException {
    ComponentNotExistException(Class type) {
        super("cannot find this dependency: " + type.getName());
    }
}
