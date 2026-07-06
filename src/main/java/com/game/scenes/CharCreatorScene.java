package com.game.scenes;

import com.game.core.GameCharacter;
import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import com.game.state.DatabaseManager;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
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
import java.util.Set;

public class CharCreatorScene extends ModularScene {

    private static final GameCharacter[] ROSTER = GameCharacter.values();

    private static final Font CARD_LABEL_FONT = Font.loadFont(
            MainMenuScene.class.getResource("/fonts/pixel/Mx437_ToshibaTxL2_8x8.ttf").toExternalForm(),
            36);

    private int selectedIndex = 0;
    private List<StackPane> cards;
    private List<StackPane> portraitStacks; // rectangle + portrait image, animated together
    private boolean[] completed;            // characters already played this loop are locked out

    public CharCreatorScene() {
        super();
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
        completed = new boolean[ROSTER.length];

        Set<String> completedCharacters = DatabaseManager.getInstance().getCompletedCharacters();

        for (int i = 0; i < ROSTER.length; i++) {
            int index = i; // effectively final for lambdas
            completed[i] = completedCharacters.contains(ROSTER[i].displayName());
            StackPane card = buildCard(ROSTER[i], index);
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
                case LEFT -> selectCard(nextSelectable(selectedIndex, -1));
                case RIGHT -> selectCard(nextSelectable(selectedIndex, +1));
                case ENTER, SPACE -> confirmSelection();
                default -> {}
            }
        });

        selectCard(firstSelectable()); // default highlight on the first still-playable character
        return root;
    }

    private StackPane buildCard(GameCharacter character, int index) {
        boolean locked = completed[index];

        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);

        // Background square (border/frame for the portrait).
        Rectangle portrait = new Rectangle(300, 300);
        portrait.setFill(Color.web("#222222"));
        portrait.setStroke(Color.web(locked ? "#555555" : "#00FF66"));
        portrait.setStrokeWidth(3);

        // Portrait art layered on top of the square.
        ImageView portraitImage = new ImageView();
        try {
            portraitImage.setImage(new Image(getClass().getResourceAsStream(character.portraitPath())));
            portraitImage.setFitWidth(280);
            portraitImage.setFitHeight(280);
            portraitImage.setPreserveRatio(true);
            portraitImage.setSmooth(false); // keep pixel-art crisp
        } catch (Exception e) {
            System.err.println("Upsi hat nicht funktioniert (kein portrait fuer " + character.raceId() + ") :(");
        }

        StackPane portraitStack = new StackPane(portrait, portraitImage);
        portraitStack.setAlignment(Pos.CENTER);
        portraitStacks.add(portraitStack);

        if (locked) {
            // grey out already-played characters and stamp them as done
            ColorAdjust desaturate = new ColorAdjust();
            desaturate.setSaturation(-0.9);
            desaturate.setBrightness(-0.35);
            portraitStack.setEffect(desaturate);

            Label doneStamp = new Label("PLAYED");
            doneStamp.setFont(CARD_LABEL_FONT);
            doneStamp.setTextFill(Color.web("#FF4C4C"));
            doneStamp.setRotate(-18);
            portraitStack.getChildren().add(doneStamp);
        }

        Label label = new Label(character.displayName());
        label.setFont(CARD_LABEL_FONT);
        label.setTextFill(Color.web(locked ? "#666666" : "#00FF66"));

        box.getChildren().addAll(portraitStack, label);

        StackPane card = new StackPane(box);
        card.setMinWidth(400);   // 300 * 1.25 scale + margin, so growth stays inside this card's own space
        card.setPrefWidth(400);
        card.setAlignment(Pos.CENTER);

        if (!locked) {
            card.setCursor(Cursor.HAND);
            card.setOnMouseEntered(e -> selectCard(index));      // hover = arrow key selection
            card.setOnMouseClicked(e -> { selectCard(index); confirmSelection(); }); // click = confirm
        }

        return card;
    }

    private int firstSelectable() {
        for (int i = 0; i < completed.length; i++) {
            if (!completed[i]) return i;
        }
        return 0; // everything locked (shouldn't happen: flow routes to the final screen instead)
    }

    /** Walks in {@code direction} from {@code from}, wrapping, until a still-playable card is found. */
    private int nextSelectable(int from, int direction) {
        int n = completed.length;
        for (int step = 1; step <= n; step++) {
            int candidate = ((from + direction * step) % n + n) % n;
            if (!completed[candidate]) return candidate;
        }
        return from;
    }

    private void selectCard(int index) {
        if (completed[index]) return; // can't highlight a locked character
        selectedIndex = index;
        for (int i = 0; i < cards.size(); i++) {
            if (completed[i]) continue;
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
        if (completed[selectedIndex]) return;

        GameCharacter chosen = ROSTER[selectedIndex];
        System.out.println("Character selected: " + chosen.displayName());

        // activeHeroId now carries the canonical character name for the whole run,
        // so the ending is recorded against the right character.
        ScenePayload dialoguePayload = new ScenePayload("DIALOGUE_SCENE", chosen.displayName())
                .withMetadata("SELECTED_RACE", chosen.raceId())
                .withMetadata("STORY_FILE", chosen.prologueStoryFile());
        SceneDirector.switchScene(new StandardDialogueScene(), dialoguePayload);
    }
}
