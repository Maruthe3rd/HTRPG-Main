package com.game.scenes;

import com.game.core.*;
import com.game.ui.DialogueView;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class CaveGateScene extends ModularScene {
    private DialogueView ui;

    public CaveGateScene(SceneDirector director) { super(director); }

    @Override
    protected void buildUI() {
        this.ui = new DialogueView();
        root.getChildren().add(ui);
    }

    @Override
    public void onEnter(ScenePayload payload) {
        ui.setBackgroundImage(new Image(getClass().getResourceAsStream("/exampleSceneBG.jpg")));
        ui.clearDialogue();
        Boolean hasKey = payload.get("HAS_VIP_KEY", Boolean.class);
        boolean unlockedSecretPath = (hasKey != null && hasKey);

        ui.setDialogueContent("Ancient Gatekeeper", "\"The Cavern of the Red Eye lies ahead. The beasts are hungry today.\"");

        List<DialogueView.ChoiceOption> choices = new ArrayList<>();

        choices.add(new DialogueView.ChoiceOption("Stealth", "Creep through the high air vents (Start at Combat Wave 2)", () -> {
            director.getPayload().put("STARTING_WAVE", 1);
            director.navigateTo("COMBAT_ARENA");
        }));

        choices.add(new DialogueView.ChoiceOption("Loud", "Kick the main iron gates open! (Fight all waves)", () -> {
            director.getPayload().put("STARTING_WAVE", 0);
            director.navigateTo("COMBAT_ARENA");
        }));

        if (unlockedSecretPath) {
            choices.add(new DialogueView.ChoiceOption("VIP Pass", "Slide the Bartender's brass key into the hidden service lock.", () -> {
                ui.setDialogueContent("Ancient Gatekeeper", "*The stone wall slides open silently* \"Ah... a friend of the Flagon. Pass in peace.\"");

                List<DialogueView.ChoiceOption> successChoice = new ArrayList<>();
                successChoice.add(new DialogueView.ChoiceOption("Proceed", "Walk straight into the Treasure Room!", () -> director.navigateTo("TAVERN_TALK")));
                ui.setChoices(successChoice);
            }));
        }

        ui.setChoices(choices);
    }

    @Override public void onExit() {}
}