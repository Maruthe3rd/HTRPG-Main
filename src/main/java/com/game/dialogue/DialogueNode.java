package com.game.dialogue;

import java.util.List;

public class DialogueNode {
    private String speaker;
    private String text;
    private String backgroundPath;
    private List<DialogueChoice> choices;

    public String getSpeaker() { return speaker; }
    public void setSpeaker(String speaker) { this.speaker = speaker; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getBackgroundPath() { return backgroundPath; }
    public void setBackgroundPath(String backgroundPath) { this.backgroundPath = backgroundPath; }

    public List<DialogueChoice> getChoices() { return choices; }
    public void setChoices(List<DialogueChoice> choices) { this.choices = choices; }
}
