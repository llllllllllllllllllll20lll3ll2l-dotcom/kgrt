package com.manyl.mod.entity;

/**
 * Режимы работы голема-копателя, настраиваемые через меню управления.
 */
public enum DigMode {
    QUARRY("gui.manyl_mod.mode.quarry"),
    TUNNEL("gui.manyl_mod.mode.tunnel"),
    ORES_ONLY("gui.manyl_mod.mode.ores");

    private final String translationKey;

    DigMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public DigMode next() {
        DigMode[] vals = values();
        return vals[(this.ordinal() + 1) % vals.length];
    }

    public static DigMode byOrdinal(int index) {
        DigMode[] vals = values();
        if (index < 0 || index >= vals.length) return QUARRY;
        return vals[index];
    }
}
