package com.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

/**
 * High-fidelity representation of your Dialogue Scene layout.
 * Designed exactly around your 1920x1080 pixel coordinate specifications.
 */
public class DialogueView extends StackPane {

    // --- DESIGN CONSTANTS ---
    private static final String NEON_GREEN = "#00FF66";
    private static final String GLOW_EFFECT_GREEN = "rgba(0, 255, 102, 0.4)";
    private static final String RETRO_FONT_FAMILY = "Courier New"; // Fallback pixel/monospace font

    // --- PRESENTATION NODES ---
    private final Pane coordinateCanvas = new Pane(); // Strict 1920x1080 coordinate wrapper
    private final ImageView backgroundView = new ImageView();
    private final ImageView characterView = new ImageView();

    // Dialogue Window elements
    private final VBox dialogueContainer = new VBox(25);
    private final ScrollPane dialogueScrollPane = new ScrollPane();

    // Choose Button elements
    private final Pane choiceButtonContainer = new Pane();
    private final Label optionViewerLabel = new Label("1/1:");
    private final SVGPath upArrow = new SVGPath();
    private final SVGPath downArrow = new SVGPath();
    private final TextFlow choiceTextFlow = new TextFlow();

    // --- LOGICAL STATE HOOKS ---
    public static class ChoiceOption {
        private final String title;
        private final String dialogueContent;
        private final Runnable onSelectAction;

        public ChoiceOption(String title, String dialogueContent, Runnable onSelectAction) {
            this.title = title;
            this.dialogueContent = dialogueContent;
            this.onSelectAction = onSelectAction;
        }
    }

    private final List<ChoiceOption> currentChoices = new ArrayList<>();
    private int activeChoiceIndex = 0;

    public DialogueView() {
        // Base Setup
        this.setStyle("-fx-background-color: #000000;");
        this.setPrefSize(1920, 1080);

        // Lock coordinate space to 1920x1080
        coordinateCanvas.setPrefSize(1920, 1080);
        coordinateCanvas.setMaxSize(1920, 1080);
        coordinateCanvas.setMinSize(1920, 1080);

        // 1. Background Image Layer
        backgroundView.setFitWidth(1920);
        backgroundView.setFitHeight(1080);
        backgroundView.setPreserveRatio(true);


        characterView.setFitHeight(1080);
        characterView.setFitWidth(1100);
        characterView.setPreserveRatio(true);
        characterView.setVisible(true);


        characterView.layoutXProperty().bind(coordinateCanvas.widthProperty().subtract(characterView.fitWidthProperty()).add(20.0));
        characterView.layoutYProperty().bind(coordinateCanvas.heightProperty().subtract(characterView.fitHeightProperty()));


        VBox textBoxContainer = createTranslucentGlassCard(850, 750);
        textBoxContainer.setLayoutX(80);
        textBoxContainer.setLayoutY(50);


        dialogueScrollPane.setFitToWidth(true);
        dialogueScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        dialogueScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        dialogueScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        dialogueScrollPane.setPrefSize(750, 650);

        dialogueContainer.setPadding(new Insets(20));
        dialogueContainer.setPrefWidth(710);
        dialogueScrollPane.setContent(dialogueContainer);
        textBoxContainer.getChildren().add(dialogueScrollPane);


        Pane choiceButton = buildChooseButtonUI();
        choiceButton.setLayoutX(80);
        choiceButton.setLayoutY(1080 - 150 - 80);


        coordinateCanvas.getChildren().addAll(backgroundView, characterView, textBoxContainer, choiceButton);
        this.getChildren().add(coordinateCanvas);


        this.widthProperty().addListener((obs, oldVal, newVal) -> scaleCanvasToFit());
        this.heightProperty().addListener((obs, oldVal, newVal) -> scaleCanvasToFit());
    }

    // --- API PUBLIC SETTERS FOR CONTROLLER ENGINE ---

    public void setBackgroundImage(Image bgImage) {
        backgroundView.setImage(bgImage);
    }

    public void setCharacterImage(Image charImage) {
        characterView.setImage(charImage);
    }

    public void toggleCharacter(boolean visible) {
        characterView.setVisible(visible);
    }

    /**
     * Appends a dual-person dialog log neatly into the textbox frame.
     */
    public void setDialogueContent(String personName, String dialogueText) {
        // Name Header Node
        Text speakerText = new Text(personName + ": ");
        speakerText.setFont(Font.font(RETRO_FONT_FAMILY, FontWeight.BOLD, 45));
        speakerText.setFill(Color.web(NEON_GREEN));

        // Dialogue Body Node
        Text bodyText = new Text(dialogueText + "\n");
        bodyText.setFont(Font.font(RETRO_FONT_FAMILY, FontWeight.NORMAL, 35));
        bodyText.setFill(Color.web(NEON_GREEN));

        TextFlow logBlock = new TextFlow(speakerText, bodyText);
        logBlock.setPrefWidth(710);

        // Add to main vertical trace container and scroll down automatically
        dialogueContainer.getChildren().add(logBlock);

        // Auto Scroll to Bottom to prevent window overflow issues
        dialogueScrollPane.setVvalue(1.0);
    }

    public void clearDialogue() {
        dialogueContainer.getChildren().clear();
    }

    /**
     * Feeds options into our interactive choose button selector
     */
    public void setChoices(List<ChoiceOption> options) {
        currentChoices.clear();
        currentChoices.addAll(options);
        activeChoiceIndex = 0;
        updateActiveChoiceDisplay();
    }

    // --- UI HELPER BUILDERS ---

    private VBox createTranslucentGlassCard(double width, double height) {
        VBox card = new VBox();
        card.setPrefSize(width, height);
        card.setMinSize(width, height);
        card.setMaxSize(width, height);
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);

        // Base Glass Styling
        card.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.25); " +
                        "-fx-background-radius: 100px; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.1); " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 100px;"
        );

        // Apply Premium Inner shadow effects
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setRadius(25);
        innerShadow.setChoke(0.1);
        innerShadow.setColor(Color.rgb(0, 0, 0, 0.25));

        // Subtle green drop glow shadow to make card sit proudly above scenery
        DropShadow glow = new DropShadow();
        glow.setRadius(15);
        glow.setColor(Color.web(GLOW_EFFECT_GREEN, 0.15));
        innerShadow.setInput(glow);

        card.setEffect(innerShadow);
        return card;
    }

    private Pane buildChooseButtonUI() {
        choiceButtonContainer.setPrefSize(850, 150);
        choiceButtonContainer.setMinSize(850, 150);
        choiceButtonContainer.setMaxSize(850, 150);

        // Base glass styling
        choiceButtonContainer.setStyle(
                "-fx-background-color: rgba(0,0,0,0.25); " +
                        "-fx-background-radius: 100px; " +
                        "-fx-border-color: rgba(0, 255, 102, 0.2); " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 100px;"
        );

        // Visual layout shadows
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setRadius(25);
        innerShadow.setColor(Color.rgb(0,0,0, 0.25));
        choiceButtonContainer.setEffect(innerShadow);

        // 1. OPTION VIEWER LABEL (Centered vertically, 40px left)
        optionViewerLabel.setFont(Font.font(RETRO_FONT_FAMILY, FontWeight.BOLD, 45));
        optionViewerLabel.setTextFill(Color.web(NEON_GREEN));
        optionViewerLabel.setLayoutX(40);
        optionViewerLabel.setLayoutY(45); // Vertically centers text

        // 2. TRIANGLE ARROWS (Triangle ratio approx 1:2 height/width)
        upArrow.setContent("M 0 16 L 32 16 L 16 0 Z"); // Crisp vector triangle representation
        upArrow.setFill(Color.web(NEON_GREEN));
        upArrow.setLayoutX(90);
        upArrow.setLayoutY(25);

        downArrow.setContent("M 0 0 L 32 0 L 16 16 Z");
        downArrow.setFill(Color.web(NEON_GREEN));
        downArrow.setLayoutX(90);
        downArrow.setLayoutY(105);

        // Interactive hover scaling animations for navigation indicators
        setupArrowHoverAnimations(upArrow, true);
        setupArrowHoverAnimations(downArrow, false);

        // Handle cycling inputs
        upArrow.setOnMouseClicked(e -> cycleChoice(-1));
        downArrow.setOnMouseClicked(e -> cycleChoice(1));

        // 3. OPTION DETAILS CONTAINER (Starts exactly 140px offset from left, centered vertically)
        choiceTextFlow.setPrefWidth(660); // Constrain width so it wraps nicely
        choiceTextFlow.setMaxHeight(110);
        choiceTextFlow.setLayoutX(170); // 140px + some breathing room padding
        choiceTextFlow.layoutYProperty().bind(choiceButtonContainer.heightProperty().subtract(choiceTextFlow.heightProperty()).divide(2));

        // Set action triggers: Click the text block itself to confirm select
        choiceTextFlow.setOnMouseClicked(e -> executeSelectedChoice());
        choiceTextFlow.setStyle("-fx-cursor: hand;");

        // Hover effect for choice panel
        choiceButtonContainer.setOnMouseEntered(e -> choiceButtonContainer.setStyle(choiceButtonContainer.getStyle() + "-fx-border-color: #00FF66;"));
        choiceButtonContainer.setOnMouseExited(e -> choiceButtonContainer.setStyle(choiceButtonContainer.getStyle().replace("-fx-border-color: #00FF66;", "-fx-border-color: rgba(0, 255, 102, 0.2);")));

        choiceButtonContainer.getChildren().addAll(optionViewerLabel, upArrow, downArrow, choiceTextFlow);
        return choiceButtonContainer;
    }

    private void setupArrowHoverAnimations(SVGPath arrow, boolean isUp) {
        arrow.setStyle("-fx-cursor: hand;");
        arrow.setOnMouseEntered(e -> {
            arrow.setFill(Color.WHITE);
            arrow.setScaleX(1.2);
            arrow.setScaleY(1.2);
        });
        arrow.setOnMouseExited(e -> {
            arrow.setFill(Color.web(NEON_GREEN));
            arrow.setScaleX(1.0);
            arrow.setScaleY(1.0);
        });
    }

    private void cycleChoice(int direction) {
        if (currentChoices.isEmpty()) return;
        activeChoiceIndex = (activeChoiceIndex + direction + currentChoices.size()) % currentChoices.size();
        updateActiveChoiceDisplay();
    }

    private void executeSelectedChoice() {
        if (currentChoices.isEmpty()) return;
        currentChoices.get(activeChoiceIndex).onSelectAction.run();
    }

    private void updateActiveChoiceDisplay() {
        if (currentChoices.isEmpty()) {
            optionViewerLabel.setText("0/0:");
            choiceTextFlow.getChildren().clear();
            return;
        }

        // Update standard "2/n:" tracker label
        optionViewerLabel.setText((activeChoiceIndex + 1) + "/" + currentChoices.size() + ":");

        ChoiceOption active = currentChoices.get(activeChoiceIndex);
        choiceTextFlow.getChildren().clear();

        // Style Option Title
        Text boldTitle = new Text(active.title + ": ");
        boldTitle.setFont(Font.font(RETRO_FONT_FAMILY, FontWeight.BOLD, 35));
        boldTitle.setFill(Color.web(NEON_GREEN));

        // Style Action Speech content
        Text bodyText = new Text(active.dialogueContent);
        bodyText.setFont(Font.font(RETRO_FONT_FAMILY, FontWeight.NORMAL, 35));
        bodyText.setFill(Color.web(NEON_GREEN));

        choiceTextFlow.getChildren().addAll(boldTitle, bodyText);
    }

    /**
     * Scales the 1920x1080 design plane up/down to preserve layout ratios perfectly inside any window size
     */
    private void scaleCanvasToFit() {
        double scaleX = this.getWidth() / 1920.0;
        double scaleY = this.getHeight() / 1080.0;
        double scale = Math.min(scaleX, scaleY);

        coordinateCanvas.setScaleX(scale);
        coordinateCanvas.setScaleY(scale);
    }
}