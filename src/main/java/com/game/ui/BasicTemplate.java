package com.game.ui;

import com.game.scenes.MainMenuScene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

//TODO: Change into (abstract)? class that gets used for the most basic design that every scene and minigame has
public class BasicTemplate extends StackPane {

    private final ImageView backgroundContainer;
    private static Font BBTitle = Font.loadFont(
            MainMenuScene.class.getResource("/fonts/pixel/MxPlus_ToshibaTxL1_8x16.ttf").toExternalForm(),
            150);

    private static Font BBTiverent = Font.loadFont(
            MainMenuScene.class.getResource("/fonts/pixel/Mx437_ToshibaTxL2_8x8.ttf").toExternalForm(),
            75);


    public BasicTemplate() {
        backgroundContainer = new ImageView();
        backgroundContainer.setFitWidth(1920);
        backgroundContainer.setFitHeight(1080);
        backgroundContainer.setPreserveRatio(false);
        setBackgroundImage("/resources/images/exampleSceneBG.png");


    }

    public void setBackgroundImage(String resourcePath) {
        try {
            Image bg = new Image(getClass().getResourceAsStream(resourcePath));
            backgroundContainer.setImage(bg);
        } catch (Exception e) {
            System.err.println("Wo bild?: " + resourcePath);
        }
    }

}