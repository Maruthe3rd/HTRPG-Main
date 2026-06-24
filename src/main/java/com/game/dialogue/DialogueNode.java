package com.game.dialogue;

import java.util.LinkedHashMap;
import java.util.Map;

public class DialogueNode {
    private final String speaker;
    private final String text;
    private final Map<String, Runnable> choices = new LinkedHashMap<>();

    public DialogueNode(String speaker, String text) {
        this.speaker = speaker;
        this.text = text;
    }

    public DialogueNode addChoice(String choiceText, Runnable action) {
        choices.put(choiceText, action);
        return this; // Allows chaining
    }

    public String getSpeaker() { return speaker; }
    public String getText() { return text; }
    public Map<String, Runnable> getChoices() { return choices; }
}