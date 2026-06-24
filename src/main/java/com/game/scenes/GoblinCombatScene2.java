package com.game.scenes;

import com.game.core.*;
import com.game.entity.*;
import com.game.ui.CombatView;

public class GoblinCombatScene2 extends ModularScene {
    private final CombatView ui = new CombatView();
    private Enemy monster;

    public GoblinCombatScene2(SceneDirector director) { super(director); }

    @Override protected void buildUI() { root.getChildren().add(ui); }

    @Override
    public void onEnter(ScenePayload payload) {
        // 1. Grab the global persistent player
        Player player = director.getHero();

        // 2. Spawn the instance enemy
        this.monster = new Enemy("Zorniger Goblin", 60, "FIREBALL", 12);

        ui.appendLog("Ein " + monster.getName() + " greift an!");

        ui.setCallbacks(
                // PHYSICAL HOOK
                (accuracy) -> {
                    int dmg = (int)(25 * accuracy);
                    monster.takeDamage(dmg);
                    ui.appendLog("Du triffst mit " + Math.round(accuracy*100) + "% Präzision für " + dmg + " Schaden!");

                    checkWinOrCounterAttack(player);
                },

                // MAGIC HOOK
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

                // TALK HOOK
                () -> ui.appendLog("Der Goblin versteht kein Wort und fletscht die Zähne."),

                // FLEE HOOK
                () -> director.navigateTo("TAVERN_TALK")
        );
    }

    private void checkWinOrCounterAttack(Player player) {
        if (monster.isDead()) {
            ui.appendLog("\n*** GEGNER BESIEGT! ***");
            // Give player 20 HP back as a reward
            player.heal(20);
        } else {
            // Monster strikes back!
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