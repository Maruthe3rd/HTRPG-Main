
//----Just sth 4 fun--------------------------

package com.game.minigameSetup;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

public class ShakeEffect {
    public static void shake(Node node, double intensity, int cycles) {
        double originalX = node.getTranslateX();
        Timeline timeline = new Timeline();
        Duration step = Duration.millis(40);

        for (int i = 1; i <= cycles; i++) {
            double decay = 1.0 - ((double) i / cycles);
            double direction = (i % 2 == 0) ? 1 : -1;
            double offset = direction * intensity * decay;

            timeline.getKeyFrames().add(
                    new KeyFrame(step.multiply(i), new KeyValue(node.translateXProperty(), originalX + offset))
            );
        }
        timeline.getKeyFrames().add(
                new KeyFrame(step.multiply(cycles + 1), new KeyValue(node.translateXProperty(), originalX))
        );

        timeline.play();
    }

    public static void shakeScreen(Node root, double intensity, double durationSeconds) {
        double originalX = root.getTranslateX();
        double originalY = root.getTranslateY();

        int frames = (int) (durationSeconds * 30);
        Duration step = Duration.seconds(durationSeconds / frames);

        Timeline timeline = new Timeline();
        for (int i = 1; i <= frames; i++) {
            double decay = 1.0 - ((double) i / frames);
            double dx = (Math.random() * 2 - 1) * intensity * decay;
            double dy = (Math.random() * 2 - 1) * intensity * decay;

            timeline.getKeyFrames().add(new KeyFrame(step.multiply(i),
                    new KeyValue(root.translateXProperty(), originalX + dx),
                    new KeyValue(root.translateYProperty(), originalY + dy)));
        }
        timeline.getKeyFrames().add(new KeyFrame(step.multiply(frames + 1),
                new KeyValue(root.translateXProperty(), originalX),
                new KeyValue(root.translateYProperty(), originalY)));

        timeline.play();
    }
}
