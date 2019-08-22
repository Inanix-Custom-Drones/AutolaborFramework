package cn.autolabor.module.networkhub.remote.resources;

import cn.autolabor.module.networkhub.dependency.AbstractComponent;

public final class Name extends AbstractComponent<Name> {
    public final String value;

    public Name(String value) {
        super(Name.class);
        this.value = value.trim();
    }
}
