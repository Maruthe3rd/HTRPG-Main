package com.game;

import com.game.core.SceneDirector;
import com.game.dialogue.DialogueNode;
import com.game.entity.Enemy;
import com.game.scenes.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane rootStack = new StackPane();
        SceneDirector director = new SceneDirector(rootStack);

        // 1. Register our Universal Engines!
        director.registerScene("DIALOGUE_ENGINE", new DialogueTemplate(director));
        director.registerScene("COMBAT_ENGINE", new CombatTemplate(director));

        // 2. Build the Dialogue Tree (Data)
        DialogueNode barkeepAngry = new DialogueNode("Bartender", "Then get out of my tavern!");
        barkeepAngry.addChoice("Leave", () -> System.exit(0));

        DialogueNode barkeepFight = new DialogueNode("Bartender", "Oh you want a fight? Hey Bruno, get him!");
        barkeepFight.addChoice("Raise your fists!", () -> {
            // Configure the Universal Combat Engine dynamically!
            director.getPayload().put("ENEMY_INSTANCE", new Enemy("Bouncer Bruno", 100, "UPPERCUT", 15));
            director.getPayload().put("WIN_SCENE", "TAVERN_TALK"); // Loop back if you win
            director.getPayload().put("LOSE_SCENE", "GAME_OVER");  // Game over if you lose
            director.navigateTo("COMBAT_ENGINE");
        });

        DialogueNode tavernStart = new DialogueNode("Bartender", "What do you want, traveler?");
        //tavernStart.addChoice("Just a drink.", () -> ((DialogueTemplate) director.getScene("DIALOGUE_ENGINE")).renderNode(barkeepAngry));
        //tavernStart.addChoice("I want to fight!", () -> ((DialogueTemplate) director.getScene("DIALOGUE_ENGINE")).renderNode(barkeepFight));

        // 3. Start the game by feeding the first node into the engine
        director.getPayload().put("START_NODE", tavernStart);
        director.navigateTo("DIALOGUE_ENGINE");

        Scene scene = new Scene(rootStack, 1024, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}