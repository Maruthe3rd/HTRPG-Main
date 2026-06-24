package com.game.scenes;

import com.game.core.*;
import com.game.dialogue.DialogueNode;
import com.game.ui.DialogueView;
import java.util.ArrayList;
import java.util.List;

public class DialogueTemplate extends ModularScene {
    private DialogueView ui;

    public DialogueTemplate(SceneDirector director) { super(director); }

    @Override
    protected void buildUI() {
        this.ui = new DialogueView();
        root.getChildren().add(ui);
    }

    @Override
    public void onEnter(ScenePayload payload) {
        DialogueNode startNode = payload.get("START_NODE", DialogueNode.class);
        if (startNode != null) {
            ui.clearDialogue(); // Clear out historical trace logs
            renderNode(startNode);
        }
    }

    public void renderNode(DialogueNode node) {
        // 1. Update text content
        ui.setDialogueContent(node.getSpeaker(), node.getText());

        // 2. Transform the choices map into a List of UI ChoiceOptions
        List<DialogueView.ChoiceOption> uiChoices = new ArrayList<>();

        node.getChoices().forEach((fullChoiceText, action) -> {
            String title = "Option";
            String body = fullChoiceText;

            // Smart auto-parsing: turns "[Confident] Hello" into Title: Confident, Body: Hello
            if (fullChoiceText.startsWith("[") && fullChoiceText.contains("]")) {
                int closeBracket = fullChoiceText.indexOf("]");
                title = fullChoiceText.substring(1, closeBracket);
                body = fullChoiceText.substring(closeBracket + 1).trim();
            }

            uiChoices.add(new DialogueView.ChoiceOption(title, body, action));
        });

        // 3. Batch submit options to the interactive layout wheel
        ui.setChoices(uiChoices);
    }

    @Override public void onExit() {}
}