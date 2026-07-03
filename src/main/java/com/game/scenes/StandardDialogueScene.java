package com.game.scenes;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import com.game.dialogue.DialogueChoice;
import com.game.dialogue.DialogueNode;
import com.game.dialogue.DialogueTemplate;
import com.game.ui.DialogueView;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StandardDialogueScene extends ModularScene {

    private static final String DEFAULT_STORY_FILE = "/story/prologueDB.json";

    private DialogueTemplate template;
    private String currentNodeId;
    private DialogueView view;
    private String storyFile; // kept so we can hand it back to ourselves after a minigame

    public StandardDialogueScene(StackPane masterViewport) {
        super(masterViewport);
    }

    @Override
    protected void onEnter(ScenePayload payload) {
        storyFile = payload.metadata("STORY_FILE", String.class);
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
            view.showLine("System", "Could not load the story file. Check the console for details.");
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
            view.showLine("", "(End of this branch — no further content written yet.)");
            view.setChoices(new ArrayList<>());
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

        view.setPortraits(node.getLeftPortrait(), node.getRightPortrait());
        view.setActiveSide(node.getActiveSide());
        view.showLine(node.getSpeaker(), node.getText());

        List<DialogueView.ChoiceOption> options = new ArrayList<>();
        if (node.getChoices() == null || node.getChoices().isEmpty()) {
            options.add(new DialogueView.ChoiceOption("(End of prologue)", () -> {}, false));
        } else {
            for (DialogueChoice choice : node.getChoices()) {
                options.add(new DialogueView.ChoiceOption(choice.getText(), () -> handleChoice(choice), true));
            }
        }
        view.setChoices(options);
    }

    private void handleChoice(DialogueChoice choice) {
        if (choice.getMinigameId() != null) {
            // This choice launches a minigame; the target node is only reached once it finishes.
            ScenePayload minigamePayload = new ScenePayload("MINIGAME", payload.activeHeroId())
                    .withMetadata("MINIGAME_ID", choice.getMinigameId())
                    .withMetadata("RETURN_STORY_FILE", storyFile)
                    .withMetadata("RETURN_NODE_ID", choice.getTargetNodeId());
            SceneDirector.switchScene(new MiniGameScene(masterViewport), minigamePayload);
        } else {
            // No minigame attached -> jump straight to the next node/scene as before.
            showNode(choice.getTargetNodeId());
        }
    }
}
