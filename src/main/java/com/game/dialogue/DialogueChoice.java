package com.game.dialogue;

public class DialogueChoice {
    private String text;
    private String targetNodeId;

    private String requiredFlag;

    private String triggerAction;
    private String actionParameter;

    private String minigameId; //launches names game instead of targetedNodeId. Once game is done it goes on

    // Optional: if the minigame reports a result tier (HIGH/MEDIUM/LOW), jump to one of these
    // instead of the plain targetNodeId. Any tier left null falls back to targetNodeId.
    private String targetNodeIdHigh;
    private String targetNodeIdMedium;
    private String targetNodeIdLow;

    // Optional: jump to a node in a different story file (a new "chapter"). When set, targetNodeId
    // (or one of the tiered variants above) is the START_NODE looked up inside that file.
    private String targetStoryFile;

    public String getMinigameId() { return minigameId; }
    public void setMinigameId(String minigameId) { this.minigameId = minigameId; }

    public String getTargetNodeIdHigh() { return targetNodeIdHigh; }
    public void setTargetNodeIdHigh(String targetNodeIdHigh) { this.targetNodeIdHigh = targetNodeIdHigh; }

    public String getTargetNodeIdMedium() { return targetNodeIdMedium; }
    public void setTargetNodeIdMedium(String targetNodeIdMedium) { this.targetNodeIdMedium = targetNodeIdMedium; }

    public String getTargetNodeIdLow() { return targetNodeIdLow; }
    public void setTargetNodeIdLow(String targetNodeIdLow) { this.targetNodeIdLow = targetNodeIdLow; }

    public String getTargetStoryFile() { return targetStoryFile; }
    public void setTargetStoryFile(String targetStoryFile) { this.targetStoryFile = targetStoryFile; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(String targetNodeId) { this.targetNodeId = targetNodeId; }

    public String getRequiredFlag() { return requiredFlag; }
    public void setRequiredFlag(String requiredFlag) { this.requiredFlag = requiredFlag; }

    public String getTriggerAction() { return triggerAction; }
    public void setTriggerAction(String triggerAction) { this.triggerAction = triggerAction; }

    public String getActionParameter() { return actionParameter; }
    public void setActionParameter(String actionParameter) { this.actionParameter = actionParameter; }
}