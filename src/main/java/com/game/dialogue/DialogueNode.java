package com.game.dialogue;

import java.util.List;

public class DialogueNode {
    private String speaker;
    private String text;
    private String backgroundPath;
    private List<DialogueChoice> choices;

    private String leftPortrait;
    private String rightPortrait;

    private String activeSide;

    private String triggerAction;
    private String actionParameter;

    public String getSpeaker() { return speaker; }
    public void setSpeaker(String speaker) { this.speaker = speaker; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getBackgroundPath() { return backgroundPath; }
    public void setBackgroundPath(String backgroundPath) { this.backgroundPath = backgroundPath; }

    public List<DialogueChoice> getChoices() { return choices; }
    public void setChoices(List<DialogueChoice> choices) { this.choices = choices; }

    public String getTriggerAction() { return triggerAction; }
    public void setTriggerAction(String triggerAction) { this.triggerAction = triggerAction; }

    public String getActionParameter() { return actionParameter; }
    public void setActionParameter(String actionParameter) { this.actionParameter = actionParameter; }

    public String getLeftPortrait() { return leftPortrait; }
    public void setLeftPortrait(String leftPortrait) { this.leftPortrait = leftPortrait; }

    public String getRightPortrait() { return rightPortrait; }
    public void setRightPortrait(String rightPortrait) { this.rightPortrait = rightPortrait; }

    public String getActiveSide() { return activeSide; }
    public void setActiveSide(String activeSide) { this.activeSide = activeSide; }
}