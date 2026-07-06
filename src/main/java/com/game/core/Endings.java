package com.game.core;

import java.util.Map;

/**
 * Human-readable descriptions for the ending flags that a run can reach. Keeps
 * the end screens free of raw METAKEY_* strings and provides a good/bad
 * classification. Unknown keys fall back to a prettified label.
 */
public final class Endings {

    public record Ending(String title, String flavor, boolean good) {}

    private static final Map<String, Ending> KNOWN = Map.ofEntries(
            Map.entry("METAKEY_DRAGONBORN_DEAD",
                    new Ending("Gefallen",
                            "Der Drachengeborene stirbt, bevor sein Werk vollendet ist.", false)),
            Map.entry("METAKEY_DRAGONBORN_GOOD_END",
                    new Ending("Der Staatsmann",
                            "Der Drachengeborene übersteht die Wirren der Politik.", true)),
            Map.entry("METAKEY_DWARF_DEAD_LACK_OF_EQUIPMENT",
                    new Ending("Schlecht gerüstet",
                            "Der Zwerg zieht ohne genügend Waffen in den Kampf – und fällt.", false)),
            Map.entry("METAKEY_DWARF_DEAD_LACK_OF_PEOPLE",
                    new Ending("Allein gelassen",
                            "Dem Zwerg fehlen die Verbündeten – die Revolution erstickt im Keim.", false)),
            Map.entry("METAKEY_DWARF_GOOD_END",
                    new Ending("Der Schmied der Freiheit",
                            "Der Zwerg überlebt und rüstet die Revolution.", true)),
            Map.entry("METAKEY_FURRY_GOOD_END",
                    new Ending("Die Ketten zerbrechen",
                            "Der Unterschicht gelingt der Aufstand – die Unterdrückung endet.", true)),
            Map.entry("METAKEY_FURRY_DEAD_CRUSHED",
                    new Ending("Niedergeschlagen",
                            "Ohne Waffen wird der Aufstand blutig zerschlagen.", false)),
            Map.entry("METAKEY_FURRY_SUBMITS",
                    new Ending("Gebeugt",
                            "Der Kopf bleibt unten. Man überlebt – doch nichts ändert sich.", false))
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
