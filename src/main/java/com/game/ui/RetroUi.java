package com.game.ui;

import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Shared retro-terminal look-and-feel: the two pixel fonts, the accent palette
 * and a couple of factory helpers so the menu-style scenes stay consistent
 * without each one re-loading fonts and re-declaring colours.
 */
public final class RetroUi {

    public static final String ACCENT_GREEN = "#00FF66";
    public static final String ACCENT_RED   = "#FF4C4C";
    public static final String ACCENT_GOLD  = "#FFC94D";
    public static final String BG_DARK      = "#0A0C0A";

    // Loading a font once registers its family for the whole app; the size here is irrelevant.
    private static final Font TITLE_SRC = load("/fonts/pixel/MxPlus_ToshibaTxL1_8x16.ttf");
    private static final Font BODY_SRC  = load("/fonts/pixel/Mx437_ToshibaTxL2_8x8.ttf");

    private RetroUi() {}

    private static Font load(String path) {
        var url = RetroUi.class.getResource(path);
        return (url != null) ? Font.loadFont(url.toExternalForm(), 12) : null;
    }

    public static Font title(double size) { return derive(TITLE_SRC, size); }
    public static Font body(double size)  { return derive(BODY_SRC, size); }

    private static Font derive(Font src, double size) {
        return (src != null) ? Font.font(src.getFamily(), size) : Font.font("Monospace", size);
    }

    /** A clickable menu label that glows white on hover, like the main menu options. */
    public static Label menuOption(String text, Runnable onClick) {
        Label label = new Label(text);
        label.setFont(body(46));
        label.setTextFill(Color.web(ACCENT_GREEN));
        label.setCursor(Cursor.HAND);
        label.setOnMouseEntered(e -> label.setTextFill(Color.WHITE));
        label.setOnMouseExited(e -> label.setTextFill(Color.web(ACCENT_GREEN)));
        label.setOnMouseClicked(e -> onClick.run());
        return label;
    }

    /** Full-viewport background image; returns an empty view if the asset is missing. */
    public static ImageView background(String resourcePath) {
        ImageView bg = new ImageView();
        try {
            bg.setImage(new Image(RetroUi.class.getResourceAsStream(resourcePath)));
            bg.setFitWidth(1920);
            bg.setFitHeight(1080);
            bg.setPreserveRatio(false);
            bg.setOpacity(0.35); // dim it so foreground text stays readable
        } catch (Exception e) {
            System.err.println("RetroUi background missing: " + resourcePath);
        }
        return bg;
    }
}
