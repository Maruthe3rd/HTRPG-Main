package com.game.scenes;

import com.game.audio.AudioManager;
import com.game.core.Endings;
import com.game.core.GameCharacter;
import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import com.game.core.StoryGraph;
import com.game.state.DatabaseManager;
import com.game.ui.RetroUi;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * End-of-game timeline map. Shows each character's branch graph with fog of war:
 * visited nodes are drawn solid, their not-yet-taken branches appear as "?", and
 * everything beyond an unexplored node stays hidden. Clicking a discovered node
 * opens its detail panel with the decisions available at that point. Reachable
 * only from the final overview once every character has been played.
 */
public class TreeMapScene extends ModularScene {

    private static final double CANVAS_W = 1150;
    private static final double CANVAS_H = 690;
    private static final double NODE = 30;

    private Set<String> explored;
    private GameCharacter selected;
    private Canvas canvas;
    private Label caption;
    private VBox detailContent;
    private final List<StackPane> tabs = new ArrayList<>();

    // per-draw state, kept so mouse clicks can hit-test the rendered nodes
    private StoryGraph currentGraph;
    private final Map<String, double[]> positions = new HashMap<>();
    private final Set<String> normalVisible = new HashSet<>();
    private final Set<String> questionVisible = new HashSet<>();
    private String selectedKey;

    @Override
    protected void onEnter(ScenePayload payload) {
        explored = DatabaseManager.getInstance().getExploredNodes();
        selected = GameCharacter.values()[0];
    }

    @Override
    protected void onExit() {}

    @Override
    protected Parent initializeLayout() {
        AudioManager.menuMood();

        StackPane root = new StackPane();
        root.setPrefSize(1920, 1080);
        root.setStyle("-fx-background-color: " + RetroUi.BG_DARK + ";");

        Label title = new Label("Zeitlinien-Karte");
        title.setFont(RetroUi.title(56));
        title.setTextFill(Color.web(RetroUi.ACCENT_GREEN));

        HBox tabRow = buildTabs();

        canvas = new Canvas(CANVAS_W, CANVAS_H);
        canvas.setOnMouseClicked(e -> handleClick(e.getX(), e.getY()));
        canvas.setOnMouseMoved(e -> canvas.setCursor(
                nodeAt(e.getX(), e.getY(), normalVisible) != null ? Cursor.HAND : Cursor.DEFAULT));
        StackPane canvasFrame = new StackPane(canvas);
        canvasFrame.setMaxSize(CANVAS_W, CANVAS_H);
        canvasFrame.setStyle(
                "-fx-border-color: " + RetroUi.ACCENT_GREEN + "; -fx-border-width: 2; -fx-background-color: #050705;");

        HBox mapRow = new HBox(16, canvasFrame, buildDetailPanel());
        mapRow.setAlignment(Pos.CENTER);

        caption = new Label();
        caption.setFont(RetroUi.body(20));
        caption.setTextFill(Color.web(RetroUi.ACCENT_GOLD));

        Label legend = new Label("■ erkundet (klickbar)    ? unerforscht    ■ gutes Ende    ■ schlechtes Ende");
        legend.setFont(RetroUi.body(16));
        legend.setTextFill(Color.web("#8A948A"));

        Label back = RetroUi.menuOption("» Zurück", this::goToFinal);

        VBox column = new VBox(14, title, tabRow, mapRow, caption, legend, back);
        column.setAlignment(Pos.CENTER);
        column.setPadding(new Insets(20));
        root.getChildren().add(column);

        root.setFocusTraversable(true);
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) root.requestFocus();
        });
        root.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> goToFinal();
                case LEFT -> selectCharacter((selected.ordinal() - 1 + GameCharacter.values().length)
                        % GameCharacter.values().length);
                case RIGHT -> selectCharacter((selected.ordinal() + 1) % GameCharacter.values().length);
                default -> {}
            }
        });

        drawSelected();
        return root;
    }

    private ScrollPane buildDetailPanel() {
        detailContent = new VBox(10);
        detailContent.setPadding(new Insets(18));
        showDetailHint();

        ScrollPane scroll = new ScrollPane(detailContent);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(420, CANVAS_H);
        scroll.setMinSize(420, CANVAS_H);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;"
                + "-fx-border-color: " + RetroUi.ACCENT_GREEN + "; -fx-border-width: 2;");
        return scroll;
    }

    private HBox buildTabs() {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);
        tabs.clear();
        for (GameCharacter c : GameCharacter.values()) {
            ImageView portrait = new ImageView();
            try {
                portrait.setImage(new Image(getClass().getResourceAsStream(c.portraitPath())));
            } catch (Exception ignored) {
                // portrait missing — the framed box alone still works as a tab
            }
            portrait.setFitWidth(76);
            portrait.setFitHeight(76);
            portrait.setPreserveRatio(true);

            StackPane tab = new StackPane(portrait);
            tab.setPrefSize(88, 88);
            tab.setCursor(Cursor.HAND);
            int index = c.ordinal();
            tab.setOnMouseClicked(e -> selectCharacter(index));
            tabs.add(tab);
            row.getChildren().add(tab);
        }
        return row;
    }

    private void selectCharacter(int index) {
        selected = GameCharacter.values()[index];
        selectedKey = null;
        showDetailHint();
        drawSelected();
    }

    private void drawSelected() {
        for (int i = 0; i < tabs.size(); i++) {
            boolean active = (i == selected.ordinal());
            StackPane tab = tabs.get(i);
            tab.setStyle("-fx-border-width: 3; -fx-border-color: "
                    + (active ? RetroUi.ACCENT_GREEN : "#333333") + "; -fx-background-color: #0C0F0C;");
            ImageView pv = (ImageView) tab.getChildren().get(0);
            ColorAdjust dim = new ColorAdjust();
            dim.setBrightness(active ? 0 : -0.45);
            pv.setEffect(dim);
        }
        currentGraph = StoryGraph.forCharacter(selected);
        drawGraph();
    }

    // ---- click handling ------------------------------------------------------

    private void handleClick(double x, double y) {
        String hit = nodeAt(x, y, null); // any visible node
        if (hit == null) return;
        if (normalVisible.contains(hit)) {
            selectedKey = hit;
            showDetail(currentGraph.node(hit));
            AudioManager.playSfx(AudioManager.UI_CLICK);
            drawGraph();
        } else {
            selectedKey = null;
            showNotDiscovered();
            drawGraph();
        }
    }

    /** Returns the key of a node whose box contains (x,y); if {@code restrictTo} is set, only those. */
    private String nodeAt(double x, double y, Set<String> restrictTo) {
        for (Map.Entry<String, double[]> e : positions.entrySet()) {
            if (restrictTo != null && !restrictTo.contains(e.getKey())) continue;
            if (!normalVisible.contains(e.getKey()) && !questionVisible.contains(e.getKey())) continue;
            double[] p = e.getValue();
            if (Math.abs(x - p[0]) <= NODE / 2 + 4 && Math.abs(y - p[1]) <= NODE / 2 + 4) {
                return e.getKey();
            }
        }
        return null;
    }

    // ---- detail panel --------------------------------------------------------

    private void showDetailHint() {
        detailContent.getChildren().setAll(
                wrapped("Klicke einen erkundeten (farbigen) Knoten, um die dortigen Entscheidungen zu sehen.",
                        20, "#8A948A"));
    }

    private void showNotDiscovered() {
        detailContent.getChildren().setAll(
                wrapped("Dieser Pfad ist noch nicht erforscht.", 22, RetroUi.ACCENT_GOLD));
    }

    private void showDetail(StoryGraph.Node node) {
        detailContent.getChildren().clear();

        Label speaker = new Label(node.speaker != null && !node.speaker.isBlank() ? node.speaker : "—");
        speaker.setFont(RetroUi.body(24));
        speaker.setTextFill(Color.web(RetroUi.ACCENT_GREEN));
        detailContent.getChildren().add(speaker);

        detailContent.getChildren().add(wrapped(node.text != null ? node.text : "", 19, "#D8E0D8"));

        if (node.ending) {
            Endings.Ending ending = Endings.describe(node.endingKey);
            detailContent.getChildren().add(wrapped(
                    "▶ Ende: " + ending.title() + (Endings.isGood(node.endingKey) ? "  (gut)" : "  (schlecht)"),
                    20, Endings.isGood(node.endingKey) ? "#00C24A" : "#FF6B6B"));
            return;
        }

        Label header = new Label("Entscheidungen:");
        header.setFont(RetroUi.body(22));
        header.setTextFill(Color.web(RetroUi.ACCENT_GOLD));
        VBox.setMargin(header, new Insets(10, 0, 0, 0));
        detailContent.getChildren().add(header);

        if (node.decisions.isEmpty()) {
            detailContent.getChildren().add(wrapped("(keine – hier geht es geradeaus weiter)", 18, "#8A948A"));
            return;
        }

        DatabaseManager db = DatabaseManager.getInstance();
        for (StoryGraph.Decision d : node.decisions) {
            VBox row = new VBox(2);
            row.setPadding(new Insets(6, 0, 6, 0));

            Label choice = new Label("› " + d.text());
            choice.setWrapText(true);
            choice.setMaxWidth(360);
            choice.setFont(RetroUi.body(18));
            choice.setTextFill(Color.WHITE);
            row.getChildren().add(choice);

            Label outcome = new Label(outcomeText(d, db));
            outcome.setWrapText(true);
            outcome.setMaxWidth(360);
            outcome.setFont(RetroUi.body(15));
            outcome.setTextFill(Color.web("#8FB89A"));
            row.getChildren().add(outcome);

            detailContent.getChildren().add(row);
        }
    }

    private String outcomeText(StoryGraph.Decision d, DatabaseManager db) {
        StringBuilder sb = new StringBuilder();
        if (d.minigameId() != null) sb.append("[Minispiel] ");
        if (d.requiredFlag() != null) {
            sb.append(db.hasMetaFlag(d.requiredFlag()) ? "[freigeschaltet] " : "[gesperrt] ");
        }

        StoryGraph.Node target = (d.targetKey() != null && currentGraph != null)
                ? currentGraph.node(d.targetKey()) : null;
        if (target == null) {
            sb.append("→ …");
        } else if (normalVisible.contains(target.key)) {
            if (target.ending) {
                sb.append("→ Ende (").append(Endings.isGood(target.endingKey) ? "gut" : "schlecht").append(")");
            } else {
                sb.append("→ ").append(target.speaker != null ? target.speaker : "weiter");
            }
        } else {
            sb.append("→ unerforscht");
        }
        return sb.toString();
    }

    private Label wrapped(String text, double size, String colorHex) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setMaxWidth(360);
        l.setFont(RetroUi.body(size));
        l.setTextFill(Color.web(colorHex));
        return l;
    }

    // ---- graph rendering -----------------------------------------------------

    private void drawGraph() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_W, CANVAS_H);
        drawGrid(gc);

        StoryGraph graph = currentGraph;
        positions.clear();
        normalVisible.clear();
        questionVisible.clear();

        Map<Integer, List<StoryGraph.Node>> byDepth = new TreeMap<>();
        int maxDepth = 0;
        for (StoryGraph.Node n : graph.nodes()) {
            byDepth.computeIfAbsent(n.depth, k -> new ArrayList<>()).add(n);
            maxDepth = Math.max(maxDepth, n.depth);
        }
        double left = 60, right = CANVAS_W - 60, top = 45, bottom = CANVAS_H - 45;
        for (Map.Entry<Integer, List<StoryGraph.Node>> e : byDepth.entrySet()) {
            List<StoryGraph.Node> col = e.getValue();
            double x = (maxDepth == 0) ? (left + right) / 2 : left + (right - left) * e.getKey() / maxDepth;
            for (int i = 0; i < col.size(); i++) {
                double y = top + (bottom - top) * (i + 1) / (col.size() + 1.0);
                positions.put(col.get(i).key, new double[]{x, y});
            }
        }

        for (StoryGraph.Node n : graph.nodes()) {
            if (explored.contains(n.key)) normalVisible.add(n.key);
        }
        for (String k : normalVisible) {
            for (String t : graph.node(k).targets) {
                if (graph.node(t) != null && !normalVisible.contains(t)) questionVisible.add(t);
            }
        }
        if (normalVisible.isEmpty()) questionVisible.add(graph.rootKey());

        gc.setStroke(Color.web("#3A5A3A"));
        gc.setLineWidth(2);
        for (String k : normalVisible) {
            double[] a = positions.get(k);
            if (a == null) continue;
            for (String t : graph.node(k).targets) {
                boolean visible = normalVisible.contains(t) || questionVisible.contains(t);
                double[] b = positions.get(t);
                if (visible && b != null) gc.strokeLine(a[0], a[1], b[0], b[1]);
            }
        }

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        Color tint = characterTint(selected);
        for (StoryGraph.Node n : graph.nodes()) {
            double[] p = positions.get(n.key);
            if (p == null) continue;
            boolean isNormal = normalVisible.contains(n.key);
            boolean isQuestion = questionVisible.contains(n.key);
            if (!isNormal && !isQuestion) continue;

            double x = p[0] - NODE / 2, y = p[1] - NODE / 2;
            if (!isNormal) {
                gc.setFill(Color.web("#1A1D1A"));
                gc.fillRoundRect(x, y, NODE, NODE, 8, 8);
                gc.setStroke(Color.web("#555555"));
                gc.setLineWidth(2);
                gc.strokeRoundRect(x, y, NODE, NODE, 8, 8);
                gc.setFill(Color.web("#888888"));
                gc.setFont(RetroUi.body(20));
                gc.fillText("?", p[0], p[1] + 1);
                continue;
            }

            Color fill = n.ending ? (Endings.isGood(n.endingKey) ? Color.web("#00C24A") : Color.web("#C23A3A")) : tint;
            gc.setFill(fill);
            gc.fillRoundRect(x, y, NODE, NODE, 8, 8);
            gc.setStroke(Color.web("#0A0C0A"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, y, NODE, NODE, 8, 8);

            if (n.key.equals(graph.rootKey())) {
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2);
                gc.strokeRoundRect(x - 4, y - 4, NODE + 8, NODE + 8, 10, 10);
            }
            if (n.key.equals(selectedKey)) {
                gc.setStroke(Color.web(RetroUi.ACCENT_GOLD));
                gc.setLineWidth(3);
                gc.strokeRoundRect(x - 6, y - 6, NODE + 12, NODE + 12, 12, 12);
            }
            if (n.ending) {
                gc.setFill(Color.web(Endings.isGood(n.endingKey) ? "#8CFFB0" : "#FF9C9C"));
                gc.setFont(RetroUi.body(13));
                gc.fillText(Endings.isGood(n.endingKey) ? "GOOD" : "BAD", p[0], p[1] + NODE / 2 + 12);
            }
        }

        caption.setText(selected.displayName() + "  —  erkundet: " + normalVisible.size()
                + " / " + graph.nodes().size() + " Knoten");
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setFill(Color.web("#050705"));
        gc.fillRect(0, 0, CANVAS_W, CANVAS_H);
        gc.setStroke(Color.web("#141814"));
        gc.setLineWidth(1);
        for (double x = 0; x <= CANVAS_W; x += 40) gc.strokeLine(x, 0, x, CANVAS_H);
        for (double y = 0; y <= CANVAS_H; y += 40) gc.strokeLine(0, y, CANVAS_W, y);
    }

    private static Color characterTint(GameCharacter c) {
        return switch (c) {
            case DRAGONBORN -> Color.web("#39B6FF");
            case DWARF -> Color.web("#FFC94D");
            default -> Color.web("#FF7BD5");
        };
    }

    private void goToFinal() {
        SceneDirector.switchScene(new FinalEndScene(), new ScenePayload("FINAL_END", "unassigned"));
    }
}
