package com.game.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared container used by the SceneDirector to pass contextual parameters
 * across loosely-coupled states (e.g., parameters like START_NODE, ENEMY_INSTANCE).
 */
public class ScenePayload {
    // The internal map doing the actual heavy lifting
    private final Map<String, Object> parameters = new HashMap<>();

    /**
     * Stores a configuration parameter.
     */
    public void put(String key, Object value) {
        parameters.put(key, value);
    }

    /**
     * Retrieves a parameter. If the key is missing or explicitly mapped to null,
     * it automatically falls back to your specified defaultValue.
     * * @param <T>          The expected type inferred by where you assign this value.
     * @param key          The lookup key string.
     * @param defaultValue The fallback option if missing.
     * @return The parameter value cast to type T, or the defaultValue.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        Object value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            System.err.println("[ScenePayload Warning] Type mismatch for key '" + key
                    + "'. Falling back to default.");
            return defaultValue;
        }
    }

    /**
     * Standard retrieval method. Returns null if missing.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) parameters.get(key);
    }

    /**
     * Checks whether a parameter key is present.
     */
    public boolean containsKey(String key) {
        return parameters.containsKey(key);
    }

    /**
     * Clears all data. Highly useful when executed inside a scene's onExit() hook
     * to ensure absolute memory isolation between game chapters.
     */
    public void clear() {
        parameters.clear();
    }
}