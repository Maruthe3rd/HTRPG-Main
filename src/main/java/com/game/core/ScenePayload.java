package com.game.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record ScenePayload(String currentSceneFile, String activeHeroId, Map<String, Object> contextualMetadata) {

    public ScenePayload {
        Objects.requireNonNull(currentSceneFile, "currentSceneFile");
        Objects.requireNonNull(activeHeroId, "activeHeroId");
        contextualMetadata = (contextualMetadata == null)
                ? Map.of()
                : Collections.unmodifiableMap(new HashMap<>(contextualMetadata));
    }

    public ScenePayload(String currentSceneFile, String activeHeroId) {
        this(currentSceneFile, activeHeroId, Map.of());
    }

    public Object metadata(String key) {
        return contextualMetadata.get(key);
    }

    public <T> T metadata(String key, Class<T> type) {
        Object value = contextualMetadata.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public ScenePayload withMetadata(String key, Object value) {
        Map<String, Object> merged = new HashMap<>(contextualMetadata);
        merged.put(key, value);
        return new ScenePayload(currentSceneFile, activeHeroId, merged);
    }
}