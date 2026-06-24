package com.game.core;

import java.util.HashMap;
import java.util.Map;

public class ScenePayload {
    private final Map<String, Object> memory = new HashMap<>();

    public void put(String key, Object value) {
        memory.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        Object obj = memory.get(key);
        return obj != null ? type.cast(obj) : null;
    }
}