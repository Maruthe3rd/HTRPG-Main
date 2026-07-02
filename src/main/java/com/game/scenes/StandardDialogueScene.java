package com.game.scenes;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.core.ScenePayload;
import com.game.dialogue.DialogueChoice;
import com.game.dialogue.DialogueNode;
import com.game.dialogue.DialogueTemplate;
import com.game.ui.DialogueView;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.io.InputStream;

public class StandardDialogueScene extends ModularScene {

    private static final String DEFAULT_STORY_FILE = "/story/prologueDB.json";

    private DialogueTemplate template;
    private String currentNodeId;
    private DialogueView view;

    public StandardDialogueScene(StackPane masterViewport) {
        super(masterViewport);
    }

    @Override
    protected void onEnter(ScenePayload payload) {
        String storyFile = payload.metadata("STORY_FILE", String.class);
        if (storyFile == null) {
            storyFile = DEFAULT_STORY_FILE;
        }

        template = loadTemplate(storyFile);

        String requestedStartNode = payload.metadata("START_NODE", String.class);
        if (template != null && requestedStartNode != null && template.getNodes().containsKey(requestedStartNode)) {
            currentNodeId = requestedStartNode;
        } else if (template != null) {
            currentNodeId = template.getStartNodeId();
        }
    }

    @Override
    protected void onExit() {}

    @Override
    protected Parent initializeLayout() {
        view = new DialogueView();

        if (template == null) {
            view.setSpeakerName("System");
            view.setDialogueText("Could not load the story file. Check the console for details.");
            return view;
        }

        showNode(currentNodeId);
        return view;
    }

    private DialogueTemplate loadTemplate(String resourcePath) {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                System.err.println("Story file not found on classpath: " + resourcePath);
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(in, DialogueTemplate.class);
        } catch (IOException e) {
            System.err.println("Failed to parse story file '" + resourcePath + "': " + e.getMessage());
            return null;
        }
    }

    private void showNode(String nodeId) {
        DialogueNode node = (nodeId != null) ? template.getNodes().get(nodeId) : null;

        if (node == null) {
            view.setSpeakerName("");
            view.setDialogueText("(End of this branch — no further content written yet.)");
            view.clearChoices();
            return;
        }

        currentNodeId = nodeId;

        if (node.getTriggerAction() != null) {
            String param = node.getActionParameter();
            System.out.println("Trigger action: " + node.getTriggerAction() + (param != null ? " (" + param + ")" : ""));
        }

        if (node.getBackgroundPath() != null) {
            view.setBackgroundImage(node.getBackgroundPath());
        }

        view.setSpeakerName(node.getSpeaker());
        view.setDialogueText(node.getText());
        view.clearChoices();

        if (node.getChoices() == null || node.getChoices().isEmpty()) {
            view.addChoiceButton("(End of prologue)", () -> {}, false);
            return;
        }

        for (DialogueChoice choice : node.getChoices()) {
            view.addChoiceButton(choice.getText(), () -> showNode(choice.getTargetNodeId()), true);
        }
    }
}