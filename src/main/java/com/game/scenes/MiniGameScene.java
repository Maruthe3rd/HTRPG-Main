package com.game.scenes;

import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import com.game.minigameSetup.MiniGame;
import com.game.minigameSetup.minigames.CashDrop;
import com.game.minigameSetup.minigames.PolicySwiper;
import com.game.minigameSetup.minigames.SmithyGame;
import javafx.animation.AnimationTimer;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

/**
 * Generic host for any MiniGame. A dialogue choice sends the player here instead
 * of straight to the next node; once the MiniGame reports isFinished(), control
 * goes back to StandardDialogueScene at whatever node the choice pointed to.
 */
public class MiniGameScene extends ModularScene {

    private static final double FIXED_TIMESTEP = 1.0 / 60.0;

    private MiniGame activeGame;
    private AnimationTimer loop;
    private Canvas canvas;

    private String returnStoryFile;
    private String returnNodeId;
    private String returnNodeIdHigh;
    private String returnNodeIdMedium;
    private String returnNodeIdLow;

    public MiniGameScene(StackPane masterViewport) {
        super(masterViewport);
    }

    @Override
    protected void onEnter(ScenePayload payload) {
        String minigameId = payload.metadata("MINIGAME_ID", String.class);
        returnStoryFile = payload.metadata("RETURN_STORY_FILE", String.class);
        returnNodeId = payload.metadata("RETURN_NODE_ID", String.class);
        returnNodeIdHigh = payload.metadata("RETURN_NODE_ID_HIGH", String.class);
        returnNodeIdMedium = payload.metadata("RETURN_NODE_ID_MEDIUM", String.class);
        returnNodeIdLow = payload.metadata("RETURN_NODE_ID_LOW", String.class);

        canvas = new Canvas(1920, 1080);
        activeGame = createMiniGame(minigameId);
    }

    // Add a case here every time a new minigame becomes selectable from dialogue.
    private MiniGame createMiniGame(String minigameId) {
        if (minigameId == null) {
            throw new IllegalStateException("MiniGameScene entered without a MINIGAME_ID");
        }
        return switch (minigameId) {
            case "SMITHY" -> new SmithyGame(canvas);
            case "POLICY_SWIPER" -> new PolicySwiper();
            case "CASH_DROP" -> new CashDrop();
            default -> throw new IllegalArgumentException("Unknown minigame id: " + minigameId);
        };
    }

    @Override
    protected Parent initializeLayout() {
        StackPane root = new StackPane(canvas);
        root.setPrefSize(1920, 1080);
        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> activeGame.onKeyPress(e));
        root.setOnKeyReleased(e -> activeGame.onKeyRelease(e));

        GraphicsContext gc = canvas.getGraphicsContext2D();

        loop = new AnimationTimer() {
            private long lastNanos = -1;
            private double accumulator = 0;

            @Override
            public void handle(long now) {
                if (lastNanos < 0) {
                    lastNanos = now;
                    return;
                }
                double frameSeconds = (now - lastNanos) / 1_000_000_000.0;
                lastNanos = now;
                accumulator += frameSeconds;

                while (accumulator >= FIXED_TIMESTEP) {
                    activeGame.update(FIXED_TIMESTEP);
                    accumulator -= FIXED_TIMESTEP;
                }

                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                activeGame.draw(gc);

                if (activeGame.isFinished()) {
                    stop();
                    returnToDialogue();
                }
            }
        };
        loop.start();

        javafx.application.Platform.runLater(root::requestFocus);
        return root;
    }

    private void returnToDialogue() {
        String resolvedNodeId = returnNodeId;
        String tier = activeGame.getResultTier();
        if (tier != null) {
            String tiered = switch (tier) {
                case "HIGH" -> returnNodeIdHigh;
                case "MEDIUM" -> returnNodeIdMedium;
                case "LOW" -> returnNodeIdLow;
                default -> null;
            };
            if (tiered != null) {
                resolvedNodeId = tiered;
            }
        }

        ScenePayload dialoguePayload = new ScenePayload("DIALOGUE", payload.activeHeroId())
                .withMetadata("STORY_FILE", returnStoryFile)
                .withMetadata("START_NODE", resolvedNodeId);
        SceneDirector.switchScene(new StandardDialogueScene(masterViewport), dialoguePayload);
    }

    @Override
    protected void onExit() {
        if (loop != null) loop.stop();
    }
}