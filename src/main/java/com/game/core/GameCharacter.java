package com.game.core;

/**
 * The playable characters of the meta-timeline. Central source of truth for a
 * character's identity so that image lookups, the canonical name stored in the
 * database and the prologue entry point never drift apart.
 *
 * <p>Three classes of the same society: the noble Dragonborn, the artisan Dwarf
 * and the labouring Furry underclass.
 */
public enum GameCharacter {

    DRAGONBORN("dragonborn", "Dragonborn", "/story/Dragonborn/prologueDB.json"),
    DWARF("dwarf", "Dwarf", "/story/Dwarf/prologueDwarf.json"),
    FURRY("furry", "Furry", "/story/Furry/prologueFurry.json");

    private final String raceId;             // lowercase id used for image asset lookups
    private final String displayName;        // canonical name persisted in the DB and shown in the UI
    private final String prologueStoryFile;  // where a fresh run for this character begins

    GameCharacter(String raceId, String displayName, String prologueStoryFile) {
        this.raceId = raceId;
        this.displayName = displayName;
        this.prologueStoryFile = prologueStoryFile;
    }

    public String raceId() { return raceId; }
    public String displayName() { return displayName; }
    public String prologueStoryFile() { return prologueStoryFile; }

    public String portraitPath() { return "/images/characters/" + raceId + ".png"; }
    public String portraitBigPath() { return "/images/characters/" + raceId + "Big.png"; }

    /** True when every playable character appears in the given set of completed display-names. */
    public static boolean allCompleted(java.util.Set<String> completedDisplayNames) {
        for (GameCharacter c : values()) {
            if (!completedDisplayNames.contains(c.displayName())) return false;
        }
        return true;
    }

    public static GameCharacter fromRaceId(String raceId) {
        if (raceId == null) return null;
        for (GameCharacter c : values()) {
            if (c.raceId.equalsIgnoreCase(raceId)) return c;
        }
        return null;
    }

    public static GameCharacter fromDisplayName(String displayName) {
        if (displayName == null) return null;
        for (GameCharacter c : values()) {
            if (c.displayName.equalsIgnoreCase(displayName)) return c;
        }
        return null;
    }
}
