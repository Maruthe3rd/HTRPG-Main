package com.game.scenes;

import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class CharCreatorScene extends ModularScene {

    private static final String[] RACE_IDS   = {"furry", "dwarf", "dragonborn"};
    private static final String[] RACE_NAMES = {"Furry", "Dwarf", "Dragonborn"};

    // Smaller, dedicated font for the race-name labels so "Dragonborn" fits
    // inside the card width instead of getting clipped to "Dr...".
    private static final Font CARD_LABEL_FONT = Font.loadFont(
            MainMenuScene.class.getResource("/fonts/pixel/Mx437_ToshibaTxL2_8x8.ttf").toExternalForm(),
            36);

    private int selectedIndex = 0;
    private List<StackPane> cards;
    private List<StackPane> portraitStacks; // rectangle + portrait image, animated together

    public CharCreatorScene(StackPane masterViewport) {
        super(masterViewport);
    }

    @Override
    protected void onEnter(ScenePayload payload) {}

    @Override
    protected void onExit() {}

    @Override
    protected Parent initializeLayout() {
        StackPane root = new StackPane();
        root.setPrefSize(1920, 1080);

        ImageView background = new ImageView();
        try {
            background.setImage(new Image(getClass().getResourceAsStream("/images/menuBG.png")));
            background.setFitWidth(1920);
            background.setFitHeight(1080);
            background.setPreserveRatio(false);
        } catch (Exception e) {
            System.err.println("Upsi hat nicht funktioniert (wo bild!?) :(");
        }

        VBox layoutBox = new VBox(60);
        layoutBox.setAlignment(Pos.CENTER);

        Label title = new Label("Choose Your Kind");
        title.setFont(MainMenuScene.BBTitle);
        title.setTextFill(Color.web("#00FF66"));

        HBox cardRow = new HBox(80);
        cardRow.setAlignment(Pos.CENTER);

        cards = new ArrayList<>();
        portraitStacks = new ArrayList<>();

        for (int i = 0; i < RACE_IDS.length; i++) {
            int index = i; // effectively final for lambdas
            StackPane card = buildCard(RACE_IDS[i], RACE_NAMES[i], index);
            cards.add(card);
            cardRow.getChildren().add(card);
        }

        layoutBox.getChildren().addAll(title, cardRow);
        root.getChildren().addAll(background, layoutBox);

        // --- keyboard navigation (same pattern as MainMenuScene) ---
        root.setFocusTraversable(true);
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) root.requestFocus();
        });

        root.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT -> selectCard((selectedIndex - 1 + RACE_IDS.length) % RACE_IDS.length);
                case RIGHT -> selectCard((selectedIndex + 1) % RACE_IDS.length);
                case ENTER, SPACE -> confirmSelection();
                default -> {}
            }
        });

        selectCard(0); // default highlight
        return root;
    }

    private StackPane buildCard(String raceId, String displayName, int index) {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);

        // Background square (border/frame for the portrait).
        Rectangle portrait = new Rectangle(300, 300);
        portrait.setFill(Color.web("#222222"));
        portrait.setStroke(Color.web("#00FF66"));
        portrait.setStrokeWidth(3);

        // Portrait art layered on top of the square.
        ImageView portraitImage = new ImageView();
        try {
            portraitImage.setImage(new Image(getClass().getResourceAsStream(
                    "/images/characters/" + raceId + ".png")));
            portraitImage.setFitWidth(280);
            portraitImage.setFitHeight(280);
            portraitImage.setPreserveRatio(true);
            portraitImage.setSmooth(false); // keep pixel-art crisp
        } catch (Exception e) {
            System.err.println("Upsi hat nicht funktioniert (kein portrait fuer " + raceId + ") :(");
        }

        StackPane portraitStack = new StackPane(portrait, portraitImage);
        portraitStack.setAlignment(Pos.CENTER);
        portraitStacks.add(portraitStack);

        Label label = new Label(displayName);
        label.setFont(CARD_LABEL_FONT);
        label.setTextFill(Color.web("#00FF66"));

        box.getChildren().addAll(portraitStack, label);

        StackPane card = new StackPane(box);
        card.setCursor(Cursor.HAND);
        card.setMinWidth(400);   // 300 * 1.25 scale + margin, so growth stays inside this card's own space
        card.setPrefWidth(400);
        card.setAlignment(Pos.CENTER);

        // mouse hover = same as arrow key selection
        card.setOnMouseEntered(e -> selectCard(index));
        // click = confirm
        card.setOnMouseClicked(e -> {
            selectCard(index);
            confirmSelection();
        });

        return card;
    }

    private void selectCard(int index) {
        selectedIndex = index;
        for (int i = 0; i < cards.size(); i++) {
            boolean isSelected = (i == selectedIndex);
            StackPane portraitStack = portraitStacks.get(i);

            ScaleTransition st = new ScaleTransition(Duration.millis(150), portraitStack);
            st.setToX(isSelected ? 1.15 : 1.0);
            st.setToY(isSelected ? 1.15 : 1.0);
            st.play();

            Label label = (Label) ((VBox) cards.get(i).getChildren().get(0)).getChildren().get(1);
            label.setTextFill(isSelected ? Color.WHITE : Color.web("#00FF66"));
        }
    }

    private void confirmSelection() {
        String chosenRace = RACE_IDS[selectedIndex];
        System.out.println("Race selected: " + chosenRace);
        payload = payload.withMetadata("SELECTED_RACE", chosenRace);

        if ("dragonborn".equals(chosenRace)) {
            ScenePayload dialoguePayload = new ScenePayload("DIALOGUE_SCENE", payload.activeHeroId())
                    .withMetadata("STORY_FILE", "/story/prologueDB.json");
            SceneDirector.switchScene(new StandardDialogueScene(masterViewport), dialoguePayload);
        } else {
            System.out.println("No story file wired up for '" + chosenRace + "' yet.");
        }
    }
}