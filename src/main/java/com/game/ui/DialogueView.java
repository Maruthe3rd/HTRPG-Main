package com.game.ui;

import com.game.scenes.MainMenuScene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class DialogueView extends StackPane {
    public static Font BBTitle = Font.loadFont(
            MainMenuScene.class.getResource("/fonts/pixel/MxPlus_ToshibaTxL1_8x16.ttf").toExternalForm(),
            150);

    public static Font BBTiverent = Font.loadFont(
            MainMenuScene.class.getResource("/fonts/pixel/Mx437_ToshibaTxL2_8x8.ttf").toExternalForm(),
            75);

    private final ImageView backgroundContainer;
    private final Label speakerLabel;
    private final Label dialogueLabel;
    private final VBox choiceContainer;
    private final StackPane textOverlayBox;

    public DialogueView() {
        backgroundContainer = new ImageView();
        backgroundContainer.setFitWidth(1920);
        backgroundContainer.setFitHeight(1080);
        backgroundContainer.setPreserveRatio(false);

        VBox bottomLayout = new VBox(15);
        bottomLayout.setAlignment(Pos.BOTTOM_CENTER);
        bottomLayout.setPadding(new Insets(40));

        choiceContainer = new VBox(10);
        choiceContainer.setAlignment(Pos.CENTER);

        textOverlayBox = new StackPane();
        textOverlayBox.getStyleClass().add("dialogue-text-frame");
        textOverlayBox.setMinHeight(250);
        textOverlayBox.setMaxWidth(1600);

        VBox textStack = new VBox(10);
        textStack.setAlignment(Pos.TOP_LEFT);
        textStack.setPadding(new Insets(25));

        speakerLabel = new Label();
        speakerLabel.setStyle("-fx-font-family: 'Monospace'; -fx-font-size: 28px; -fx-text-fill: #00FF00; -fx-font-weight: bold;");

        dialogueLabel = new Label();
        dialogueLabel.setWrapText(true);
        dialogueLabel.setStyle("-fx-font-family: 'Monospace'; -fx-font-size: 22px; -fx-text-fill: #FFFFFF;");

        textStack.getChildren().addAll(speakerLabel, dialogueLabel);
        textOverlayBox.getChildren().add(textStack);

        bottomLayout.getChildren().addAll(choiceContainer, textOverlayBox);

        this.getChildren().addAll(backgroundContainer, bottomLayout);

    }

    public void setBackgroundImage(String resourcePath) {
        try {
            Image bg = new Image(getClass().getResourceAsStream(resourcePath));
            backgroundContainer.setImage(bg);
        } catch (Exception e) {
            System.err.println("Failed to load dialogue background asset: " + resourcePath);
        }
    }

    public void setSpeakerName(String name) {
        speakerLabel.setText(name != null ? name.toUpperCase() : "");
    }

    public void setDialogueText(String currentVisibleText) {
        dialogueLabel.setText(currentVisibleText);
    }

    public void clearChoices() {
        choiceContainer.getChildren().clear();
    }

    public void addChoiceButton(String text, Runnable onSelectAction, boolean isUnlocked) {
        Button choiceBtn = new Button(text);
        choiceBtn.setPrefWidth(800);
        choiceBtn.setMinHeight(50);

        if (isUnlocked) {
            choiceBtn.setFont(BBTiverent);
            choiceBtn.setOnAction(e -> onSelectAction.run());
        } else {
            choiceBtn.setText("[LOCKED] " + text);
            choiceBtn.setDisable(true);
            choiceBtn.setFont(BBTiverent);
        }

        choiceContainer.getChildren().add(choiceBtn);
    }

    public StackPane getTextOverlayBox() {
        return textOverlayBox;
    }
}