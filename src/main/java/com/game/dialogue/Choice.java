package com.game.dialogue;

public class Choice {
    private String buttonText;
    private String targetNodeId;
    private String triggerScene;
    private String requiredKnowledgeKey;

    public Choice(String buttonText, String targetNodeId, String requiredKnowledgeKey) {
        this.buttonText = buttonText;
        this.targetNodeId = targetNodeId;
        this.requiredKnowledgeKey = requiredKnowledgeKey;
    }

    public String getButtonText() { return buttonText; }
    public String getTargetNodeId() { return targetNodeId; }
    public String getTriggerScene() { return triggerScene; }
    public String getRequiredKnowledgeKey() { return requiredKnowledgeKey; }
}