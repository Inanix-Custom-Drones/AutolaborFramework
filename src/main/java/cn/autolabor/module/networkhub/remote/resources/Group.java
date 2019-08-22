package cn.autolabor.module.networkhub.remote.resources;

import cn.autolabor.module.networkhub.dependency.AbstractComponent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Group extends AbstractComponent<Group> {
    private final ConcurrentHashMap<String, Long> core = new ConcurrentHashMap<>();
    public final Map<String, Long> view = Collections.unmodifiableMap(core);

    public Group() {
        super(Group.class);
    }

    public Long update(String p, Long r) {
        return r != null ? core.put(p, r) : core.remove(p);
    }

    public Long get(String p) {
        return core.get(p);
    }

    public List<String> get(int timeout) {
        long now = System.currentTimeMillis();
        List<String> result = new LinkedList<>();
        core.forEachEntry(1, it -> {
            if (now - it.getValue() < timeout)
                result.add(it.getKey());
        });
        return result;
    }
}
