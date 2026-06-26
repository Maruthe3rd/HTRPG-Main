package com.game.dialogue;

/**
 * Represents an actionable player decision option inside a dialogue sequence.
 * Handles convergence tracking, lock/unlock mechanics, and event flags.
 */
public class DialogueChoice {
    private String text;
    private String targetNodeId;

    private String requiredFlag;

    private String triggerAction;
    private String actionParameter;

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
