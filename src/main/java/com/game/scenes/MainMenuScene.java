package com.game.scenes;

import com.game.core.ScenePayload;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MainMenuScene extends ModularScene {

    private static final Logger LOGGER = Logger.getLogger(MainMenuScene.class.getName());

    private static final String PIXEL_FONT_PATH = "/fonts/pixel/MxPlus_ToshibaTxL1_8x16.ttf";
    private static final double FONT_SIZE = 24;

    private static final Color ACTIVE_COLOR = Color.GOLD;
    private static final Color INACTIVE_COLOR = Color.WHITE;
    private static final String ACTIVE_PREFIX = "> ";
    private static final String INACTIVE_PREFIX = "";

    private static final double SCENE_WIDTH = 800;
    private static final double SCENE_HEIGHT = 600;

    /** Menu content + behavior, kept separate from how it's drawn or how input is read. */
    private enum MenuOption {
        PLAY("Play", MainMenuScene::onPlaySelected),
        CHARACTER_CREATOR("Character Creator", MainMenuScene::onCharacterCreatorSelected),
        SETTINGS("Settings", MainMenuScene::onSettingsSelected),
        EXIT("Exit", MainMenuScene::onExitSelected);

        private final String label;
        private final Consumer<MainMenuScene> action;

        MenuOption(String label, Consumer<MainMenuScene> action) {
            this.label = label;
            this.action = action;
        }
    }

    private final List<Label> optionLabels = new ArrayList<>();
    private Font pixelFont;
    private int currentIndex = 0;

    @Override
    public void init(ScenePayload payload) {
        this.payload = payload;
        this.currentIndex = 0;
        this.pixelFont = loadPixelFont(FONT_SIZE);
    }

    @Override
    public void buildUI() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(12);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: black;");

        optionLabels.clear();
        for (MenuOption option : MenuOption.values()) {
            Label label = new Label(option.label);
            label.setFont(pixelFont);
            optionLabels.add(label);
            root.getChildren().add(label);
        }
        renderHighlight();

        root.setFocusTraversable(true);
        root.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);

        this.scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);

        this.scene.windowProperty().addListener((obs, oldWindow, newWindow) -> {
            if (newWindow != null) {
                root.requestFocus();
            }
        });
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        switch (code) {
            case DOWN -> moveSelection(1);
            case UP -> moveSelection(-1);
            case ENTER -> activateCurrentSelection();
            default -> { /* ignore everything else */ }
        }
    }

    private void moveSelection(int direction) {
        int optionCount = MenuOption.values().length;
        currentIndex = Math.floorMod(currentIndex + direction, optionCount);
        renderHighlight();
    }

    private void activateCurrentSelection() {
        MenuOption selected = MenuOption.values()[currentIndex];
        LOGGER.info(() -> "Main menu: '" + selected.label + "' activated.");
        selected.action.accept(this);
    }

    private void renderHighlight() {
        MenuOption[] options = MenuOption.values();
        for (int i = 0; i < options.length; i++) {
            Label label = optionLabels.get(i);
            boolean active = (i == currentIndex);
            label.setText((active ? ACTIVE_PREFIX : INACTIVE_PREFIX) + options[i].label);
            label.setTextFill(active ? ACTIVE_COLOR : INACTIVE_COLOR);
        }
    }

    private Font loadPixelFont(double size) {
        URL resource = MainMenuScene.class.getResource(PIXEL_FONT_PATH);
        if (resource == null) {
            LOGGER.log(Level.WARNING,
                    "Pixel font not found at {0}; falling back to the system default font.",
                    PIXEL_FONT_PATH);
            return Font.font(size);
        }

        Font loaded = Font.loadFont(resource.toExternalForm(), size);
        if (loaded == null) {
            LOGGER.log(Level.WARNING,
                    "Font.loadFont() failed to parse {0}; falling back to the system default font.",
                    PIXEL_FONT_PATH);
            return Font.font(size);
        }
        return loaded;
    }

    private void onPlaySelected() {
        // TODO: once a story-select / first-character intro scene exists, route to it, e.g.:
        LOGGER.info("Play selected — target scene not wired up yet.");
    }

    private void onCharacterCreatorSelected() {
        // TODO: SceneDirector.switchScene(new CharacterCreatorScene(), payload);
        LOGGER.info("Character Creator selected — target scene not wired up yet.");
    }

    private void onSettingsSelected() {
        // TODO: SceneDirector.switchScene(new SettingsScene(), payload);
        LOGGER.info("Settings selected — target scene not wired up yet.");
    }

    private void onExitSelected() {
        LOGGER.info("Exit selected — shutting down.");
        Platform.exit();
    }
}