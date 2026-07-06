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

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
            return MAPPER.readValue(in, DialogueTemplate.class);
        } catch (IOException e) {
            System.err.println("Failed to parse story file '" + resourcePath + "': " + e.getMessage());
            return null;
        }
    }

    private void showNode(String nodeId) {
        DialogueNode node = (nodeId != null) ? template.getNodes().get(nodeId) : null;

        if (node == null) {
            System.err.println("Dialogue node not found in '" + storyFile + "': " + nodeId);
            view.showLine("System", "Missing dialogue node: " + nodeId);
            view.setChoices(new ArrayList<>());
            return;
        }

        currentNodeId = nodeId;

        if (node.getBackgroundPath() != null) {
            view.setBackgroundImage(node.getBackgroundPath());
        }
        view.setPortraits(node.getLeftPortrait(), node.getRightPortrait());
        view.setActiveSide(node.getActiveSide());
        view.showLine(node.getSpeaker(), node.getText());

        // Ending node: let the player read the closing line, then continue into the
        // run-end screen via a synthetic "continue" choice (instead of snapping away).
        if (isEndingTrigger(node.getTriggerAction())) {
            String endingKey = node.getActionParameter();
            List<DialogueView.ChoiceOption> endOption = new ArrayList<>();
            endOption.add(new DialogueView.ChoiceOption("» Weiter", () -> endRun(endingKey), true));
            view.setChoices(endOption);
            return;
        }

        // Mid-story trigger: persist its flag immediately so later runs can read it.
        if (node.getTriggerAction() != null) {
            handleTrigger(node.getTriggerAction(), node.getActionParameter());
        }

        DatabaseManager db = DatabaseManager.getInstance();
        List<DialogueView.ChoiceOption> options = new ArrayList<>();
        if (node.getChoices() != null) {
            for (DialogueChoice choice : node.getChoices()) {
                boolean unlocked = choice.getRequiredFlag() == null || db.hasMetaFlag(choice.getRequiredFlag());
                options.add(new DialogueView.ChoiceOption(choice.getText(), () -> handleChoice(choice), unlocked));
            }
        }
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

    private static boolean isEndingTrigger(String action) {
        return "SET_FLAG_AND_END".equals(action);
    }

    /**
     * Persists a mid-story trigger. Every {@code SET_FLAG_*} action writes its
     * parameter into the meta-timeline as a flag, which is the mechanism a later
     * run reads back to open new paths.
     */
    private void handleTrigger(String action, String param) {
        if (action == null) return;
        if (action.startsWith("SET_FLAG") && param != null) {
            DatabaseManager.getInstance().setMetaFlag(param, true);
        }
    }

    /** Finalises the current character's run and hands off to the run-end screen. */
    private void endRun(String endingKey) {
        DatabaseManager db = DatabaseManager.getInstance();
        if (endingKey != null) {
            db.setMetaFlag(endingKey, true);
        }
        String character = payload.activeHeroId();
        db.savePlaythrough(character, endingKey != null ? endingKey : "UNKNOWN_END");

        ScenePayload runEndPayload = new ScenePayload("RUN_END", character)
                .withMetadata("CHARACTER", character)
                .withMetadata("ENDING_KEY", endingKey);
        SceneDirector.switchScene(new RunEndScene(), runEndPayload);
    }

}