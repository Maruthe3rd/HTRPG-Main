/*
package com.game.scenes;

import com.game.core.*;
import com.game.entities.*;
import com.game.ui.CombatView;

public class CombatTemplate extends ModularScene {
    private CombatView ui;
    private Enemy currentEnemy;
    private String winSceneId;
    private String loseSceneId;

    public CombatTemplate(SceneDirector director) { super(director); }

    @Override
    protected void buildUI() {
        this.ui = new CombatView();
        root.getChildren().add(ui);
    }

    @Override
    public void onEnter(ScenePayload payload) {
        this.currentEnemy = payload.get("ENEMY_INSTANCE", Enemy.class);
        this.winSceneId = payload.get("WIN_SCENE", String.class);
        this.loseSceneId = payload.get("LOSE_SCENE", String.class);

        Player player = director.getHero();
        ui.appendLog("Ein " + currentEnemy.getName() + " blockiert den Weg!");

        ui.setCallbacks(
                (accuracy) -> processTurn(player, (int)(30 * accuracy), "Physischer Angriff"),
                (speedMult, isCrit) -> processMagic(player, speedMult, isCrit),
                () -> processTurn(player, 0, "Pazifist"),
                () -> tryFlee()
        );
    }

    private void processMagic(Player player, double speedMult, boolean isCrit) {
        int cost = isCrit ? 20 : 10;
        if (!player.tryUseMana(cost)) {
            ui.appendLog("Nicht genug Mana!");
            return;
        }
        int dmg = (int)((isCrit ? 40 : 20) * speedMult);
        processTurn(player, dmg, "Magie");
    }

    private void processTurn(Player player, int playerDamage, String actionType) {
        if (actionType.equals("Pazifist")) {
            ui.appendLog("Du versuchst zu reden... " + currentEnemy.getName() + " greift trotzdem an!");
        } else {
            currentEnemy.takeDamage(playerDamage);
            ui.appendLog(actionType + " trifft für " + playerDamage + " Schaden! (Gegner HP: " + currentEnemy.getHp() + ")");
        }

        if (currentEnemy.isDead()) {
            ui.appendLog("\n*** KAMPF GEWONNEN! ***\nDrücke Flucht (🏃) um fortzufahren.");
            ui.setCallbacks(null, null, null, () -> director.navigateTo(winSceneId));
            return;
        }

        player.takeDamage(currentEnemy.getAttackPower());
        ui.appendLog(currentEnemy.getName() + " schlägt zurück für " + currentEnemy.getAttackPower() + " Schaden! (Deine HP: " + player.getHp() + ")");

        if (player.isDead()) {
            ui.appendLog("\n*** DU BIST GESTORBEN ***\nDrücke Flucht (🏃) um neu zu starten.");
            ui.setCallbacks(null, null, null, () -> director.navigateTo(loseSceneId));
        } else {
            ui.resetUi();
        }
    }

    private void tryFlee() {
        if (Math.random() > 0.4) {
            ui.appendLog("Erfolgreich geflohen!");
            director.navigateTo(winSceneId);
        } else {
            ui.appendLog("Flucht fehlgeschlagen!");
            processTurn(director.getHero(), 0, "Fluchtversuch");
        }
    }

    @Override public void onExit() {}
}
*/
