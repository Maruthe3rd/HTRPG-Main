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

    private static final Font SPEAKER_FONT = (BBTiverent != null)
            ? Font.font(BBTiverent.getFamily(), 26)
            : Font.font("Monospace", 26);
    private static final Font BODY_FONT = (BBTiverent != null)
            ? Font.font(BBTiverent.getFamily(), 20)
            : Font.font("Monospace", 20);
    private static final Font CHOICE_FONT = (BBTiverent != null)
            ? Font.font(BBTiverent.getFamily(), 20)
            : Font.font("Monospace", 20);

    private static final String PANEL_BG =
            "-fx-background-color: rgba(0,0,0,0.78); -fx-background-radius: 8;";

    private static final String CHOICE_STYLE =
            "-fx-background-color: rgba(0,0,0,0.7);" +
                    "-fx-text-fill: #00FF66;" +
                    "-fx-border-color: #00FF66;" +
                    "-fx-border-width: 2;" +
                    "-fx-background-radius: 4;" +
                    "-fx-border-radius: 4;" +
                    "-fx-padding: 12 20 12 20;";

    private static final String CHOICE_STYLE_HOVER =
            "-fx-background-color: rgba(0,80,40,0.85);" +
                    "-fx-text-fill: #FFFFFF;" +
                    "-fx-border-color: #00FF66;" +
                    "-fx-border-width: 2;" +
                    "-fx-background-radius: 4;" +
                    "-fx-border-radius: 4;" +
                    "-fx-padding: 12 20 12 20;";

    private static final String CHOICE_STYLE_LOCKED =
            "-fx-background-color: rgba(0,0,0,0.4);" +
                    "-fx-text-fill: #666666;" +
                    "-fx-border-color: #444444;" +
                    "-fx-border-width: 2;" +
                    "-fx-background-radius: 4;" +
                    "-fx-border-radius: 4;" +
                    "-fx-padding: 12 20 12 20;";

    private final ImageView backgroundContainer;
    private final Label speakerLabel;
    private final Label dialogueLabel;
    private final VBox choiceContainer;
    private final StackPane textOverlayBox;

    public DialogueView() {
        setStyle("-fx-background-color: #101010;"); // fallback so it's never blank grey

        backgroundContainer = new ImageView();
        backgroundContainer.setFitWidth(1920);
        backgroundContainer.setFitHeight(1080);
        backgroundContainer.setPreserveRatio(false);

        VBox bottomLayout = new VBox(15);
        bottomLayout.setAlignment(Pos.BOTTOM_CENTER);
        bottomLayout.setPadding(new Insets(40));
        bottomLayout.setMaxWidth(1600);

        choiceContainer = new VBox(10);
        choiceContainer.setAlignment(Pos.CENTER);
        choiceContainer.setFillWidth(true);
        choiceContainer.setMaxWidth(1600);

        textOverlayBox = new StackPane();
        textOverlayBox.setStyle(PANEL_BG);
        textOverlayBox.setMinHeight(200);
        textOverlayBox.setMaxWidth(1600);

        VBox textStack = new VBox(10);
        textStack.setAlignment(Pos.TOP_LEFT);
        textStack.setPadding(new Insets(25));
        textStack.setMaxWidth(1550);

        speakerLabel = new Label();
        speakerLabel.setFont(SPEAKER_FONT);
        speakerLabel.setTextFill(Color.web("#00FF66"));

        dialogueLabel = new Label();
        dialogueLabel.setFont(BODY_FONT);
        dialogueLabel.setTextFill(Color.WHITE);
        dialogueLabel.setWrapText(true);
        dialogueLabel.setMaxWidth(1550);

        textStack.getChildren().addAll(speakerLabel, dialogueLabel);
        textOverlayBox.getChildren().add(textStack);

        bottomLayout.getChildren().addAll(choiceContainer, textOverlayBox);

        StackPane.setAlignment(bottomLayout, Pos.BOTTOM_CENTER);

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
        choiceBtn.setMaxWidth(Double.MAX_VALUE);
        choiceBtn.setMinHeight(50);
        choiceBtn.setWrapText(true);
        choiceBtn.setFont(CHOICE_FONT);
        choiceBtn.setAlignment(Pos.CENTER);

        if (isUnlocked) {
            choiceBtn.setStyle(CHOICE_STYLE);
            choiceBtn.setOnMouseEntered(e -> choiceBtn.setStyle(CHOICE_STYLE_HOVER));
            choiceBtn.setOnMouseExited(e -> choiceBtn.setStyle(CHOICE_STYLE));
            choiceBtn.setOnAction(e -> onSelectAction.run());
        } else {
            choiceBtn.setText("[LOCKED] " + text);
            choiceBtn.setDisable(true);
            choiceBtn.setStyle(CHOICE_STYLE_LOCKED);
        }

        VBox.setVgrow(choiceBtn, Priority.NEVER);
        choiceContainer.getChildren().add(choiceBtn);
    }

    public StackPane getTextOverlayBox() {
        return textOverlayBox;
    }
}