package com.game.scenes;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import com.game.dialogue.DialogueChoice;
import com.game.dialogue.DialogueNode;
import com.game.dialogue.DialogueTemplate;
import com.game.state.DatabaseManager;
import com.game.ui.DialogueView;
import javafx.scene.Parent;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StandardDialogueScene extends ModularScene {

    private static final String DEFAULT_STORY_FILE = "/story/Dragonborn/prologueDB.json";

    private DialogueTemplate template;
    private String currentNodeId;
    private DialogueView view;
    private String storyFile; // kept so we can hand it back to ourselves after a minigame

    public StandardDialogueScene() {
        super();
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

        if (node.getChoices() == null || node.getChoices().isEmpty()) {
            if (node.getTriggerAction() != null) {
                handleTrigger(node.getTriggerAction(), node.getActionParameter());
            }
            return;
        }

        currentNodeId = nodeId;

        if (node.getTriggerAction() != null) {
            handleTrigger(node.getTriggerAction(), node.getActionParameter());
        }

        if (node.getBackgroundPath() != null) {
            view.setBackgroundImage(node.getBackgroundPath());
        }

        view.setPortraits(node.getLeftPortrait(), node.getRightPortrait());
        view.setActiveSide(node.getActiveSide());
        view.showLine(node.getSpeaker(), node.getText());

        List<DialogueView.ChoiceOption> options = new ArrayList<>();
        for (DialogueChoice choice : node.getChoices()) {options.add(new DialogueView.ChoiceOption(choice.getText(), () -> handleChoice(choice), true));}

        view.setChoices(options);
    }

    private void handleChoice(DialogueChoice choice) {
        String destinationFile = (choice.getTargetStoryFile() != null) ? choice.getTargetStoryFile() : storyFile;

        if (choice.getMinigameId() != null) {
            ScenePayload minigamePayload = new ScenePayload("MINIGAME", payload.activeHeroId())
                    .withMetadata("MINIGAME_ID", choice.getMinigameId())
                    .withMetadata("RETURN_STORY_FILE", destinationFile)
                    .withMetadata("RETURN_NODE_ID", choice.getTargetNodeId())
                    .withMetadata("RETURN_NODE_ID_HIGH", choice.getTargetNodeIdHigh())
                    .withMetadata("RETURN_NODE_ID_MEDIUM", choice.getTargetNodeIdMedium())
                    .withMetadata("RETURN_NODE_ID_LOW", choice.getTargetNodeIdLow());
            SceneDirector.switchScene(new MiniGameScene(), minigamePayload);
        } else if (choice.getTargetStoryFile() != null) {
            ScenePayload nextChapterPayload = new ScenePayload("DIALOGUE", payload.activeHeroId())
                    .withMetadata("STORY_FILE", choice.getTargetStoryFile())
                    .withMetadata("START_NODE", choice.getTargetNodeId());
            SceneDirector.switchScene(new StandardDialogueScene(), nextChapterPayload);
        } else {
            showNode(choice.getTargetNodeId());
        }
    }

    private void handleTrigger(String action, String param) {

        DatabaseManager db = DatabaseManager.getInstance();

        switch (action) {

            case "SET_FLAG_AND_END":

                // 1. Flag speichern
                if (param != null) {
                    db.setMetaFlag(param, true);
                }

                // 2. aktueller Charakter
                String current = payload.activeHeroId();

                // 3. Run speichern
                db.savePlaythrough(current, "BAD_END");

                // 4. Zurück zur Character Selection, damit ein neuer Run gestartet werden kann.
                ScenePayload charCreatorPayload = new ScenePayload("CHAR_CREATOR", "unassigned");
                SceneDirector.switchScene(new CharCreatorScene(), charCreatorPayload);

                break;
        }
    }

}