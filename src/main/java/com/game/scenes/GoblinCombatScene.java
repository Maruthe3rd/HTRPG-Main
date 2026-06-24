package com.game.scenes;

import com.game.core.*;
import com.game.entity.*;
import com.game.ui.CombatView;

public class GoblinCombatScene extends ModularScene {
    private CombatView ui; // Fixed
    private Enemy monster;

    public GoblinCombatScene(SceneDirector director) { super(director); }

    @Override
    protected void buildUI() {
        this.ui = new CombatView(); // Fixed
        root.getChildren().add(ui);
    }

    @Override
    public void onEnter(ScenePayload payload) {
        Player player = director.getHero();
        this.monster = new Enemy("Zorniger Goblin", 60, "FIREBALL", 12);

        ui.appendLog("Ein " + monster.getName() + " greift an!");

        ui.setCallbacks(
                (accuracy) -> {
                    int dmg = (int)(25 * accuracy);
                    monster.takeDamage(dmg);
                    ui.appendLog("Du triffst mit " + Math.round(accuracy*100) + "% Präzision für " + dmg + " Schaden!");
                    checkWinOrCounterAttack(player);
                },
                (speedMult, isCrit) -> {
                    int cost = isCrit ? 20 : 10;
                    if (!player.tryUseMana(cost)) {
                        ui.appendLog("Nicht genug Mana!");
                        return;
                    }
                    int dmg = (int)((isCrit ? 40 : 20) * speedMult);
                    monster.takeDamage(dmg);
                    ui.appendLog("Zauber trifft für " + dmg + " Schaden! (" + monster.getHp() + "/" + monster.getMaxHp() + " HP)");
                    checkWinOrCounterAttack(player);
                },
                () -> ui.appendLog("Der Goblin versteht kein Wort und fletscht die Zähne."),
                () -> director.navigateTo("TAVERN_TALK")
        );
    }

    private void checkWinOrCounterAttack(Player player) {
        if (monster.isDead()) {
            ui.appendLog("\n*** GEGNER BESIEGT! ***");
            player.heal(20);
            ui.resetUi();
        } else {
            player.takeDamage(monster.getAttackPower());
            ui.appendLog("Der Goblin schlägt zurück für " + monster.getAttackPower() + " Schaden! (Deine HP: " + player.getHp() + ")");

            if (player.isDead()) {
                ui.appendLog("\n DU BIST GESTORBEN.");
            } else {
                ui.resetUi();
            }
        }
    }

    @Override public void onExit() {}
}