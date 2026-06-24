package com.game.scenes;

import com.game.core.*;
import com.game.ui.DialogueView;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class TavernScene extends ModularScene {
    private DialogueView ui;
    private int bartenderPatience = 50;

    public TavernScene(SceneDirector director) { super(director); }

    @Override
    protected void buildUI() {
        this.ui = new DialogueView();
        root.getChildren().add(ui);
    }

    @Override
    public void onEnter(ScenePayload payload) {
        ui.setBackgroundImage(new Image(getClass().getResourceAsStream("/exampleSceneBG.jpg")));;
        this.bartenderPatience = 50;
        ui.clearDialogue();
        node_1_Greeting();
    }

    private void node_1_Greeting() {
        ui.setDialogueContent("Gruff Bartender",
                "\"We don't get many soft-handed city folk in the Rusty Flagon. State your business.\" \n[Bartender Patience: " + bartenderPatience + "/100]");

        List<DialogueView.ChoiceOption> choices = new ArrayList<>();

        choices.add(new DialogueView.ChoiceOption("Aggressive", "'Pour a drink and keep your snide comments to yourself.' (-25 Patience)", () -> {
            bartenderPatience -= 25;
            evaluateBartenderMood("He slams a dirty mug down. 'Watch your tone, whelp.'");
        }));

        choices.add(new DialogueView.ChoiceOption("Confident", "'A pint of your best ale, my friend! It looks like a slow night.' (+15 Patience)", () -> {
            bartenderPatience += 15;
            evaluateBartenderMood("He smirks, pouring a fresh pint. 'Aye. Goblins have the trade routes choked.'");
        }));

        choices.add(new DialogueView.ChoiceOption("Apologetic", "'Oh, my apologies! I just wanted to ask for some directions...' (-5 Patience)", () -> {
            bartenderPatience -= 5;
            evaluateBartenderMood("He rolls his eyes. 'This is a tavern, kid. Buy something or get out.'");
        }));

        ui.setChoices(choices);
    }

    private void evaluateBartenderMood(String bartenderResponseText) {
        List<DialogueView.ChoiceOption> choices = new ArrayList<>();

        if (bartenderPatience <= 20) {
            ui.setDialogueContent("Gruff Bartender", "\"ALRIGHT, THAT'S IT! HEY BRUNO, THROW THIS TRASH OUT!\"");
            choices.add(new DialogueView.ChoiceOption("Fight", "Raise your fists! [FIGHT THE BOUNCER]", () -> {
                director.getPayload().put("ENEMY_NAME", "Bruno the Bouncer");
                director.getPayload().put("ENEMY_SPELL", "UPPERCUT");
                director.navigateTo("COMBAT_ARENA");
            }));
        } else if (bartenderPatience >= 65) {
            ui.setDialogueContent("Gruff Bartender", "\"You know what? You're alright. Here, take this old brass key. It opens the cave's back door.\"");
            choices.add(new DialogueView.ChoiceOption("Key", "Take the VIP Key and leave for the Cave.", () -> {
                director.getPayload().put("HAS_VIP_KEY", true);
                director.navigateTo("CAVE_GATE");
            }));
        } else {
            node_2_Information(bartenderResponseText);
            return;
        }

        ui.setChoices(choices);
    }

    private void node_2_Information(String bartenderText) {
        ui.setDialogueContent("Gruff Bartender", bartenderText + "\n\"So... you looking for the Fireball tome or what?\"");

        List<DialogueView.ChoiceOption> choices = new ArrayList<>();
        choices.add(new DialogueView.ChoiceOption("Direct", "'Yes. Where is it?'", () -> director.navigateTo("CAVE_GATE")));
        choices.add(new DialogueView.ChoiceOption("Evasive", "'None of your business.'", () -> director.navigateTo("CAVE_GATE")));

        ui.setChoices(choices);
    }

    @Override public void onExit() {}
}