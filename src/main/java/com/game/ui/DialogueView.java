package com.game.ui;

import com.game.scenes.MainMenuScene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

public class DialogueView extends StackPane {
    
    public static Font BBTitle = Font.loadFont(
            MainMenuScene.class.getResource("/fonts/pixel/MxPlus_ToshibaTxL1_8x16.ttf").toExternalForm(),
            150);

    public static Font BBTiverent = Font.loadFont(
            MainMenuScene.class.getResource("/fonts/pixel/Mx437_ToshibaTxL2_8x8.ttf").toExternalForm(),
            75);

    private static final Font SPEAKER_FONT = (BBTiverent != null)
            ? Font.font(BBTiverent.getFamily(), 26)
            : Font.font("Monospace", 26);
    private static final Font BODY_FONT = (BBTiverent != null)
            ? Font.font(BBTiverent.getFamily(), 22)
            : Font.font("Monospace", 22);
    private static final Font CHOICE_INDEX_FONT = (BBTiverent != null)
            ? Font.font(BBTiverent.getFamily(), 22)
            : Font.font("Monospace", 22);
    private static final Font CHOICE_FONT = (BBTiverent != null)
            ? Font.font(BBTiverent.getFamily(), 20)
            : Font.font("Monospace", 20);
    private static final Font ARROW_FONT = (BBTiverent != null)
            ? Font.font(BBTiverent.getFamily(), 20)
            : Font.font("Monospace", 20);

    private static final String ACCENT_GREEN = "#00FF66";
    private static final String ACCENT_GREEN_DIM = "#4F9E6C";
    private static final String ACCENT_UNDERLINE = "#39B6FF";

    private static final String PANEL_BG =
            "-fx-background-color: rgba(10,12,10,0.72); -fx-background-radius: 22;";

    private static final String CURRENT_LINE_STYLE =
            "-fx-border-color: transparent transparent " + ACCENT_UNDERLINE + " transparent;" +
                    "-fx-border-width: 0 0 2 0;" +
                    "-fx-padding: 0 4 8 4;";

    private static final String CHOICE_BOX_STYLE =
            "-fx-background-color: rgba(0,0,0,0.72);" +
                    "-fx-border-color: " + ACCENT_GREEN + ";" +
                    "-fx-border-width: 2;" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-radius: 10;" +
                    "-fx-padding: 14 26 14 26;";

    private static final String CHOICE_BOX_STYLE_HOVER =
            "-fx-background-color: rgba(0,70,35,0.82);" +
                    "-fx-border-color: " + ACCENT_GREEN + ";" +
                    "-fx-border-width: 2;" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-radius: 10;" +
                    "-fx-padding: 14 26 14 26;";

    private static final String CHOICE_BOX_STYLE_LOCKED =
            "-fx-background-color: rgba(0,0,0,0.45);" +
                    "-fx-border-color: #555555;" +
                    "-fx-border-width: 2;" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-radius: 10;" +
                    "-fx-padding: 14 26 14 26;";

    private static final String ARROW_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: " + ACCENT_GREEN + "; -fx-cursor: hand;";

    private static final String ARROW_STYLE_DISABLED =
            "-fx-background-color: transparent; -fx-text-fill: #444444;";

    public static class ChoiceOption {
        private final String text;
        private final Runnable onSelect;
        private final boolean unlocked;

        public ChoiceOption(String text, Runnable onSelect, boolean unlocked) {
            this.text = text;
            this.onSelect = onSelect;
            this.unlocked = unlocked;
        }

        public String getText() { return text; }
        public Runnable getOnSelect() { return onSelect; }
        public boolean isUnlocked() { return unlocked; }
    }

    private final ImageView backgroundContainer;
    private final ImageView leftPortraitView;
    private final ImageView rightPortraitView;

    private final TextFlow previousFlow;
    private final VBox currentLineBox;
    private final TextFlow currentFlow;

    private final javafx.scene.control.Label choiceIndexLabel;
    private final TextFlow choiceTextFlow;
    private final Text choiceTextNode;
    private final StackPane choiceBox;
    private final javafx.scene.control.Button upArrow;
    private final javafx.scene.control.Button downArrow;

    private String previousSpeaker;
    private String previousText;

    private List<ChoiceOption> choices = new ArrayList<>();
    private int selectedChoiceIndex = 0;

    private boolean end;

    public DialogueView() {
        setStyle("-fx-background-color: #101010;");
        setFocusTraversable(true);

        backgroundContainer = new ImageView();
        setBackgroundImage("/images/exampleSceneBG.jpg");

        VBox textPanel = new VBox(10);
        textPanel.setStyle(PANEL_BG);
        textPanel.setMaxWidth(1000);
        textPanel.setMinHeight(190);
        textPanel.setMaxHeight(640);
        textPanel.setPadding(new Insets(28, 34, 22, 34));
        textPanel.setTranslateY(0);
        StackPane.setAlignment(textPanel, Pos.TOP_CENTER);
        StackPane.setMargin(textPanel, new Insets(110, 0, 0, 0));

        previousFlow = new TextFlow();
        previousFlow.setMaxWidth(1290);
        previousFlow.setOpacity(0.5);
        previousFlow.setManaged(false);
        previousFlow.setVisible(false);

        currentFlow = new TextFlow();
        currentFlow.setMaxWidth(1290);

        currentLineBox = new VBox(currentFlow);
        currentLineBox.setStyle(CURRENT_LINE_STYLE);

        textPanel.getChildren().addAll(previousFlow, currentLineBox);

        upArrow = new javafx.scene.control.Button("\u25B2");
        upArrow.setFont(ARROW_FONT);
        upArrow.setStyle(ARROW_STYLE);
        upArrow.setFocusTraversable(false);
        upArrow.setOnAction(e -> moveSelection(-1));

        downArrow = new javafx.scene.control.Button("\u25BC");
        downArrow.setFont(ARROW_FONT);
        downArrow.setStyle(ARROW_STYLE);
        downArrow.setFocusTraversable(false);
        downArrow.setOnAction(e -> moveSelection(1));

        choiceIndexLabel = new javafx.scene.control.Label();
        choiceIndexLabel.setFont(CHOICE_INDEX_FONT);
        choiceIndexLabel.setTextFill(Color.web(ACCENT_GREEN));

        choiceTextNode = new Text();
        choiceTextNode.setFont(CHOICE_FONT);
        choiceTextNode.setFill(Color.web(ACCENT_GREEN));

        choiceTextFlow = new TextFlow(choiceTextNode);
        choiceTextFlow.setMaxWidth(620);

        HBox choiceRow = new HBox(14, choiceIndexLabel, choiceTextFlow);
        choiceRow.setAlignment(Pos.CENTER_LEFT);

        choiceBox = new StackPane(choiceRow);
        choiceBox.setStyle(CHOICE_BOX_STYLE);
        choiceBox.setMaxWidth(760);
        choiceBox.setCursor(javafx.scene.Cursor.HAND);
        choiceBox.setOnMouseEntered(e -> refreshChoiceBoxStyle(true));
        choiceBox.setOnMouseExited(e -> refreshChoiceBoxStyle(false));
        choiceBox.setOnMouseClicked(e -> confirmSelection());

        VBox choiceWidget = new VBox(6, upArrow, choiceBox, downArrow);
        choiceWidget.setAlignment(Pos.CENTER);
        choiceWidget.setTranslateY(400);
        StackPane.setAlignment(choiceWidget, Pos.BOTTOM_CENTER);
        StackPane.setMargin(choiceWidget, new Insets(0, 0, 46, 0));


        leftPortraitView = new ImageView();
        leftPortraitView.setPreserveRatio(true);
        leftPortraitView.setFitHeight(820);
        leftPortraitView.setTranslateX(-175);
        leftPortraitView.setTranslateY(100);
        StackPane.setAlignment(leftPortraitView, Pos.BOTTOM_LEFT);

        rightPortraitView = new ImageView();
        rightPortraitView.setPreserveRatio(true);
        rightPortraitView.setFitHeight(820);
        rightPortraitView.setTranslateX(175);
        rightPortraitView.setTranslateY(100);
        StackPane.setAlignment(rightPortraitView, Pos.BOTTOM_RIGHT);

        // Portraits are assigned per node by the scene (protagonist + speaking NPC);
        // no generic defaults, so narrator-only lines don't show a random character.
        applyPortraitFocus(leftPortraitView, true);
        applyPortraitFocus(rightPortraitView, true);

        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP) { moveSelection(-1); e.consume(); }
            else if (e.getCode() == KeyCode.DOWN) { moveSelection(1); e.consume(); }
            else if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) { confirmSelection(); e.consume(); }
        });

        this.getChildren().addAll(backgroundContainer, textPanel, leftPortraitView, rightPortraitView, choiceWidget);

        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getRoot().requestFocus();
                requestFocus();
            }
        });
    }

    public void setBackgroundImage(String resourcePath) {
        try {
            Image bg = new Image(getClass().getResourceAsStream(resourcePath));
            backgroundContainer.setImage(bg);
            backgroundContainer.setTranslateZ(0);
        } catch (Exception e) {
            System.err.println("Failed to load dialogue background asset: " + resourcePath);
        }
    }

    public void setPortraits(String leftResourcePath, String rightResourcePath) {
        if (leftResourcePath != null) {
            setPortraitImage(leftPortraitView, leftResourcePath);
        }
        if (rightResourcePath != null) {
            setPortraitImage(rightPortraitView, rightResourcePath);
        }
    }

    private void setPortraitImage(ImageView target, String resourcePath) {
        try {
            Image img = new Image(getClass().getResourceAsStream(resourcePath));
            target.setImage(img);
        } catch (Exception e) {
            System.err.println("Failed to load portrait asset: " + resourcePath);
        }
    }

    public void setActiveSide(String side) {
        boolean leftActive = "LEFT".equalsIgnoreCase(side);
        boolean rightActive = "RIGHT".equalsIgnoreCase(side);
        boolean anyoneActive = leftActive || rightActive;

        applyPortraitFocus(leftPortraitView, !anyoneActive || leftActive);
        applyPortraitFocus(rightPortraitView, !anyoneActive || rightActive);
    }

    private void applyPortraitFocus(ImageView portrait, boolean active) {
        if (active) {
            portrait.setEffect(null);
            portrait.setOpacity(1.0);
        } else {
            ColorAdjust dim = new ColorAdjust();
            dim.setSaturation(-0.6);
            dim.setBrightness(-0.25);
            portrait.setEffect(dim);
            portrait.setOpacity(0.55);
        }
    }

    public void showLine(String speaker, String text) {
        if (previousSpeaker != null || previousText != null) {
            previousFlow.getChildren().setAll(buildLineNodes(previousSpeaker, previousText, false));
            previousFlow.setManaged(true);
            previousFlow.setVisible(true);
        } else {
            previousFlow.getChildren().clear();
            previousFlow.setManaged(false);
            previousFlow.setVisible(false);
        }

        currentFlow.getChildren().setAll(buildLineNodes(speaker, text, true));

        previousSpeaker = speaker;
        previousText = text;
    }

    public void resetHistory() {
        previousSpeaker = null;
        previousText = null;
        previousFlow.getChildren().clear();
        previousFlow.setManaged(false);
        previousFlow.setVisible(false);
    }

    private List<Text> buildLineNodes(String speaker, String text, boolean current) {
        List<Text> nodes = new ArrayList<>();

        if (speaker != null && !speaker.isEmpty()) {
            Text speakerText = new Text(speaker.toUpperCase() + ":  ");
            speakerText.setFont(SPEAKER_FONT);
            speakerText.setFill(Color.web(current ? ACCENT_GREEN : ACCENT_GREEN_DIM));
            speakerText.setStyle("-fx-font-weight: bold;");
            nodes.add(speakerText);
        }

        Text bodyText = new Text(text != null ? text : "");
        bodyText.setFont(BODY_FONT);
        bodyText.setFill(Color.web(current ? ACCENT_GREEN : ACCENT_GREEN_DIM));
        nodes.add(bodyText);

        return nodes;
    }
    
    public void setChoices(List<ChoiceOption> newChoices) {
        this.choices = new ArrayList<>(newChoices);
        this.selectedChoiceIndex = 0;
        renderSelection();
    }

    private void moveSelection(int delta) {
        if (choices.isEmpty()) return;
        int size = choices.size();
        selectedChoiceIndex = ((selectedChoiceIndex + delta) % size + size) % size;
        renderSelection();
    }

    private void confirmSelection() {
        if (choices.isEmpty()) return;
        ChoiceOption selected = choices.get(selectedChoiceIndex);
        if (selected.isUnlocked() && selected.getOnSelect() != null) {
            selected.getOnSelect().run();
        }
    }

    private void renderSelection() {
        boolean hasMultiple = choices.size() > 1;
        upArrow.setVisible(hasMultiple);
        upArrow.setManaged(hasMultiple);
        downArrow.setVisible(hasMultiple);
        downArrow.setManaged(hasMultiple);
        upArrow.setDisable(!hasMultiple);
        downArrow.setDisable(!hasMultiple);
        upArrow.setStyle(hasMultiple ? ARROW_STYLE : ARROW_STYLE_DISABLED);
        downArrow.setStyle(hasMultiple ? ARROW_STYLE : ARROW_STYLE_DISABLED);

        if (choices.isEmpty()) {
            choiceIndexLabel.setText("");
            choiceTextNode.setText("");
            choiceBox.setVisible(false);
            return;
        }

        choiceBox.setVisible(true);
        ChoiceOption current = choices.get(selectedChoiceIndex);
        choiceIndexLabel.setText((selectedChoiceIndex + 1) + "/" + choices.size() + ":");

        if (current.isUnlocked()) {
            choiceTextNode.setText(current.getText());
            choiceTextNode.setFill(Color.web(ACCENT_GREEN));
            choiceIndexLabel.setTextFill(Color.web(ACCENT_GREEN));
            choiceBox.setCursor(javafx.scene.Cursor.HAND);
        } else {
            choiceTextNode.setText("[LOCKED] " + current.getText());
            choiceTextNode.setFill(Color.web("#777777"));
            choiceIndexLabel.setTextFill(Color.web("#777777"));
            choiceBox.setCursor(javafx.scene.Cursor.DEFAULT);
        }

        refreshChoiceBoxStyle(false);
    }

    private void refreshChoiceBoxStyle(boolean hovering) {
        if (choices.isEmpty()) return;
        boolean unlocked = choices.get(selectedChoiceIndex).isUnlocked();
        if (!unlocked) {
            choiceBox.setStyle(CHOICE_BOX_STYLE_LOCKED);
        } else {
            choiceBox.setStyle(hovering ? CHOICE_BOX_STYLE_HOVER : CHOICE_BOX_STYLE);
        }
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }
}
