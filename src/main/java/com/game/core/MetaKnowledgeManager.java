package com.game.core;

import java.util.HashSet;
import java.util.Set;

public class MetaKnowledgeManager {
    // This set persists *across lifetimes* via SQLite database loads
    private final Set<String> unlockedKnowledge = new HashSet<>();

    public void unlockInsight(String knowledgeKey) {
        unlockedKnowledge.add(knowledgeKey);
        // Async update to SQLite database profile goes here...
    }

    public boolean playerKnows(String knowledgeKey) {
        if (knowledgeKey == null || knowledgeKey.isEmpty()) return true;
        return unlockedKnowledge.contains(knowledgeKey);
    }
}