package com.game.ui;

import java.util.Map;

/**
 * Maps a dialogue node's speaker to the portrait sprite that should appear on
 * the NPC side. The protagonist's own sprite is chosen separately from the
 * active character; this only resolves the "other" person in the scene.
 *
 * <p>{@code THE VOICE} is the narrator and has no portrait (returns {@code null},
 * meaning "leave the NPC side as-is / nobody is speaking on screen").
 */
public final class Portraits {

    private static final Map<String, String> SPEAKER_SPRITES = Map.of(
            "Bruder",     "/images/characters/brotherDB.png",
            "Vater",      "/images/characters/npc_father.png",
            "Assistent",  "/images/characters/npc_assistant.png",
            "Nachbar",    "/images/characters/npc_neighbor.png",
            "Anführer",   "/images/characters/npc_leader.png"
    );

    private static final String GENERIC_NPC = "/images/characters/npc_generic.png";

    private Portraits() {}

    /** Sprite for the given speaker, or {@code null} for the narrator / empty speaker. */
    public static String spriteForSpeaker(String speaker) {
        if (speaker == null || speaker.isBlank()) return null;
        if (isNarrator(speaker)) return null;

        String mapped = SPEAKER_SPRITES.get(speaker.trim());
        if (mapped != null) return mapped;

        // A named-but-unmapped speaker (e.g. an anonymous voice) still gets a face.
        return GENERIC_NPC;
    }

    public static boolean isNarrator(String speaker) {
        return speaker != null && speaker.trim().equalsIgnoreCase("THE VOICE");
    }
}
