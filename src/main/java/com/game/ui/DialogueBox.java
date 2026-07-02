package com.game.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.logging.Logger;


public final class DialogueBox extends StackPane {

    private static final Logger LOGGER = Logger.getLogger(DialogueBox.class.getName());

    private static final double DEFAULT_CHARS_PER_MS = 30.0;
    private static final double BOX_HEIGHT            = 160.0;
    private static final double NAME_FONT_SIZE        = 16.0;
    private static final double BODY_FONT_SIZE        = 15.0;
    private static final Color  BACKGROUND_COLOR      = Color.color(0, 0, 0, 0.78);
    private static final Color  NAME_COLOR            = Color.GOLD;
    private static final Color  BODY_COLOR            = Color.WHITE;
    private static final Color  CARET_COLOR           = Color.color(1, 1, 1, 0.55);
    private static final String CARET_SYMBOL          = "▌";


    private final Text nameText   = new Text();
    private final Text bodyText   = new Text();
    private final Text caretText  = new Text(CARET_SYMBOL);

    private final Timeline typewriterTimeline = new Timeline();
    private String  fullBody   = "";
    private int     charIndex  = 0;
    private boolean isTyping   = false;

    private double msPerChar = DEFAULT_CHARS_PER_MS;

    public DialogueBox() {
        buildLayout();
        bindInputHandlers();
        configureFocus();
    }

    public void present(String speakerName, String body) {
        stopTypewriter();
        nameText.setText((speakerName != null && !speakerName.isBlank()) ? speakerName : "");
        fullBody  = (body != null) ? body : "";
        charIndex = 0;
        bodyText.setText("");
        caretText.setVisible(true);

        if (fullBody.isEmpty()) {
            isTyping = false;
            caretText.setVisible(false);
            return;
        }

        isTyping = true;
        buildTypewriterTimeline();
        typewriterTimeline.play();
    }

    public void setMsPerChar(double msPerChar) {
        if (msPerChar <= 0) {
            throw new IllegalArgumentException("msPerChar must be positive, got: " + msPerChar);
        }
        this.msPerChar = msPerChar;
    }

    public boolean isTyping() {
        return isTyping;
    }

    private void buildLayout() {
        setAlignment(Pos.BOTTOM_CENTER);
        setBackground(new Background(
                new BackgroundFill(BACKGROUND_COLOR, new CornerRadii(6), Insets.EMPTY)));
        setMaxHeight(BOX_HEIGHT);
        setPadding(new Insets(14, 20, 14, 20));

        nameText.setFont(resolveFont(NAME_FONT_SIZE, true));
        nameText.setFill(NAME_COLOR);

        BorderPane nameRow = new BorderPane(nameText);
        nameRow.setPadding(new Insets(0, 0, 6, 0));

        bodyText.setFont(resolveFont(BODY_FONT_SIZE, false));
        bodyText.setFill(BODY_COLOR);
        bodyText.setWrappingWidth(0);

        caretText.setFont(resolveFont(BODY_FONT_SIZE, false));
        caretText.setFill(CARET_COLOR);
        caretText.setVisible(false);

        TextFlow bodyFlow = new TextFlow(bodyText, caretText);
        bodyFlow.setPrefWidth(Double.MAX_VALUE);

        VBox column = new VBox(4, nameRow, bodyFlow);
        column.setAlignment(Pos.TOP_LEFT);
        column.setFillWidth(true);

        getChildren().add(column);
    }

    private Font resolveFont(double size, boolean bold) {
        return bold
                ? Font.font("MxPlus ToshibaTxL1 8x16", javafx.scene.text.FontWeight.BOLD, size)
                : Font.font("MxPlus ToshibaTxL1 8x16", size);
    }

    private void buildTypewriterTimeline() {
        typewriterTimeline.stop();
        typewriterTimeline.getKeyFrames().clear();
        typewriterTimeline.setCycleCount(fullBody.length());

        KeyFrame frame = new KeyFrame(Duration.millis(msPerChar), e -> revealNextChar());
        typewriterTimeline.getKeyFrames().add(frame);

        typewriterTimeline.setOnFinished(e -> onTypewriterComplete());
    }

    private void revealNextChar() {
        if (charIndex < fullBody.length()) {
            charIndex++;
            bodyText.setText(fullBody.substring(0, charIndex));
        }
    }

    private void onTypewriterComplete() {
        bodyText.setText(fullBody);
        caretText.setVisible(false);
        isTyping = false;
        LOGGER.finest("Typewriter complete.");
    }

    private void skipToEnd() {
        if (!isTyping) return;
        stopTypewriter();
        bodyText.setText(fullBody);
        caretText.setVisible(false);
        isTyping = false;
        LOGGER.finest("Typewriter skipped to end.");
    }

    private void stopTypewriter() {
        typewriterTimeline.stop();
        typewriterTimeline.getKeyFrames().clear();
    }


    private void bindInputHandlers() {
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleClick);
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.SPACE || code == KeyCode.ENTER) {
            handleAdvanceInput();
            event.consume();
        }
    }

    private void handleClick(MouseEvent event) {
        handleAdvanceInput();
    }


    private void handleAdvanceInput() {
        if (isTyping) {
            skipToEnd();
        } else {
            fireEvent(new DialogueAdvanceEvent());
        }
    }

    private void configureFocus() {
        setFocusTraversable(true);

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((wObs, oldWin, newWin) -> {
                    if (newWin != null) {
                        requestFocus();
                    }
                });
            }
        });
    }
}