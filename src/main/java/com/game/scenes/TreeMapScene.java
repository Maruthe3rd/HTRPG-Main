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
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
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
 * everything beyond an unexplored node stays hidden. Reachable only from the
 * final overview once both characters have been played.
 */
public class TreeMapScene extends ModularScene {

    private static final double CANVAS_W = 1520;
    private static final double CANVAS_H = 700;
    private static final double NODE = 30;

    private Set<String> explored;
    private GameCharacter selected;
    private Canvas canvas;
    private Label caption;
    private final List<StackPane> tabs = new ArrayList<>();

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
        title.setFont(RetroUi.title(64));
        title.setTextFill(Color.web(RetroUi.ACCENT_GREEN));

        HBox tabRow = buildTabs();

        canvas = new Canvas(CANVAS_W, CANVAS_H);
        StackPane canvasFrame = new StackPane(canvas);
        canvasFrame.setMaxSize(CANVAS_W, CANVAS_H);
        canvasFrame.setStyle(
                "-fx-border-color: " + RetroUi.ACCENT_GREEN + "; -fx-border-width: 2; -fx-background-color: #050705;");

        caption = new Label();
        caption.setFont(RetroUi.body(24));
        caption.setTextFill(Color.web(RetroUi.ACCENT_GOLD));

        Label legend = new Label("■ erkundet    ? unerforscht    ■ gutes Ende    ■ schlechtes Ende");
        legend.setFont(RetroUi.body(18));
        legend.setTextFill(Color.web("#8A948A"));

        Label back = RetroUi.menuOption("» Zurück", this::goToFinal);

        VBox column = new VBox(18, title, tabRow, canvasFrame, caption, legend, back);
        column.setAlignment(Pos.CENTER);
        column.setPadding(new Insets(30));
        root.getChildren().add(column);

        root.setFocusTraversable(true);
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) root.requestFocus();
        });
        root.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE, ENTER, SPACE -> goToFinal();
                case LEFT -> selectCharacter((selected.ordinal() - 1 + GameCharacter.values().length)
                        % GameCharacter.values().length);
                case RIGHT -> selectCharacter((selected.ordinal() + 1) % GameCharacter.values().length);
                default -> {}
            }
        });

        drawSelected();
        return root;
    }

    private HBox buildTabs() {
        HBox row = new HBox(24);
        row.setAlignment(Pos.CENTER);
        tabs.clear();
        for (GameCharacter c : GameCharacter.values()) {
            ImageView portrait = new ImageView();
            try {
                portrait.setImage(new Image(getClass().getResourceAsStream(c.portraitPath())));
            } catch (Exception ignored) {
                // portrait missing — the framed box alone still works as a tab
            }
            portrait.setFitWidth(84);
            portrait.setFitHeight(84);
            portrait.setPreserveRatio(true);

            StackPane tab = new StackPane(portrait);
            tab.setPrefSize(96, 96);
            tab.setCursor(javafx.scene.Cursor.HAND);
            int index = c.ordinal();
            tab.setOnMouseClicked(e -> selectCharacter(index));
            tabs.add(tab);
            row.getChildren().add(tab);
        }
        return row;
    }

    private void selectCharacter(int index) {
        selected = GameCharacter.values()[index];
        drawSelected();
    }

    private void drawSelected() {
        // highlight the active tab
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
        drawGraph(canvas.getGraphicsContext2D(), StoryGraph.forCharacter(selected));
    }

    private void drawGraph(GraphicsContext gc, StoryGraph graph) {
        gc.clearRect(0, 0, CANVAS_W, CANVAS_H);
        drawGrid(gc);

        // --- layout: column per depth, spread vertically within the column ---
        Map<Integer, List<StoryGraph.Node>> byDepth = new TreeMap<>();
        int maxDepth = 0;
        for (StoryGraph.Node n : graph.nodes()) {
            byDepth.computeIfAbsent(n.depth, k -> new ArrayList<>()).add(n);
            maxDepth = Math.max(maxDepth, n.depth);
        }
        double left = 70, right = CANVAS_W - 70, top = 50, bottom = CANVAS_H - 50;
        Map<String, double[]> pos = new HashMap<>();
        for (Map.Entry<Integer, List<StoryGraph.Node>> e : byDepth.entrySet()) {
            List<StoryGraph.Node> col = e.getValue();
            double x = (maxDepth == 0) ? (left + right) / 2 : left + (right - left) * e.getKey() / maxDepth;
            for (int i = 0; i < col.size(); i++) {
                double y = top + (bottom - top) * (i + 1) / (col.size() + 1.0);
                pos.put(col.get(i).key, new double[]{x, y});
            }
        }

        // --- fog of war: explored nodes solid, their direct children as "?" ---
        Set<String> normal = new HashSet<>();
        Set<String> question = new HashSet<>();
        for (StoryGraph.Node n : graph.nodes()) {
            if (explored.contains(n.key)) normal.add(n.key);
        }
        for (String k : normal) {
            for (String t : graph.node(k).targets) {
                if (graph.node(t) != null && !normal.contains(t)) question.add(t);
            }
        }
        if (normal.isEmpty()) question.add(graph.rootKey()); // nothing explored yet: at least show the entrance

        // --- edges (only from explored nodes to a visible child) ---
        gc.setStroke(Color.web("#3A5A3A"));
        gc.setLineWidth(2);
        for (String k : normal) {
            double[] a = pos.get(k);
            if (a == null) continue;
            for (String t : graph.node(k).targets) {
                boolean visible = normal.contains(t) || question.contains(t);
                double[] b = pos.get(t);
                if (visible && b != null) gc.strokeLine(a[0], a[1], b[0], b[1]);
            }
        }

        // --- nodes ---
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        Color tint = characterTint(selected);
        for (StoryGraph.Node n : graph.nodes()) {
            double[] p = pos.get(n.key);
            if (p == null) continue;
            boolean isNormal = normal.contains(n.key);
            boolean isQuestion = question.contains(n.key);
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
            if (n.ending) {
                gc.setFill(Color.web(Endings.isGood(n.endingKey) ? "#8CFFB0" : "#FF9C9C"));
                gc.setFont(RetroUi.body(13));
                gc.fillText(Endings.isGood(n.endingKey) ? "GOOD" : "BAD", p[0], p[1] + NODE / 2 + 12);
            }
        }

        int total = graph.nodes().size();
        caption.setText(selected.displayName() + "  —  erkundet: " + normal.size() + " / " + total + " Knoten");
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
