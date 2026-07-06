package com.game.core;

import java.util.Map;

/**
 * Human-readable descriptions for the ending flags that a run can reach. Keeps
 * the end screens free of raw METAKEY_* strings and provides a good/bad
 * classification. Unknown keys fall back to a prettified label.
 */
public final class Endings {

    public record Ending(String title, String flavor, boolean good) {}

    private static final Map<String, Ending> KNOWN = Map.of(
            "METAKEY_DRAGONBORN_DEAD",
            new Ending("Gefallen",
                    "Der Drachengeborene stirbt, bevor sein Werk vollendet ist.", false),
            "METAKEY_DRAGONBORN_GOOD_END",
            new Ending("Der Staatsmann",
                    "Der Drachengeborene übersteht die Wirren der Politik.", true),
            "METAKEY_DWARF_DEAD_LACK_OF_EQUIPMENT",
            new Ending("Schlecht gerüstet",
                    "Der Zwerg zieht ohne genügend Waffen in den Kampf – und fällt.", false),
            "METAKEY_DWARF_DEAD_LACK_OF_PEOPLE",
            new Ending("Allein gelassen",
                    "Dem Zwerg fehlen die Verbündeten – die Revolution erstickt im Keim.", false),
            "METAKEY_DWARF_GOOD_END",
            new Ending("Der Schmied der Freiheit",
                    "Der Zwerg überlebt und rüstet die Revolution.", true)
    );

    private Endings() {}

    public static Ending describe(String key) {
        if (key == null) {
            return new Ending("Unbekanntes Ende", "Das Schicksal bleibt im Dunkeln.", false);
        }
        Ending known = KNOWN.get(key);
        if (known != null) return known;
        return new Ending(prettify(key), "", key.contains("GOOD_END"));
    }

    public static boolean isGood(String key) {
        Ending known = (key != null) ? KNOWN.get(key) : null;
        if (known != null) return known.good();
        return key != null && key.contains("GOOD_END");
    }

    private static String prettify(String key) {
        String s = key.startsWith("METAKEY_") ? key.substring("METAKEY_".length()) : key;
        return s.replace('_', ' ');
    }
}
