package com.manyl.mod.entity;

import net.minecraft.resources.Identifier;

/**
 * Типы прислужников-воинов Manyl_mod: Голем-воин (муж.) и Голем-воительница (жен.).
 */
public enum GolemRole {
    WARRIOR("warrior_golem", -1, 36.0D, 0.29D, 7.0D, 0xFFE24444,
            "entity.manyl_mod.warrior_golem",
            "textures/entity/golem/warrior_golem.png",
            "textures/entity/golem/warrior_golem_glow.png",
            "textures/gui/icons/warrior.png"),
    VALKYRIE("valkyrie_golem", -1, 36.0D * 0.75D, 0.34D, 7.5D, 0xFFE866B8,
            "entity.manyl_mod.valkyrie_golem",
            "textures/entity/golem/valkyrie_golem.png",
            "textures/entity/golem/valkyrie_golem_glow.png",
            "textures/gui/icons/valkyrie.png");

    private final String id;
    private final int maxPerPlayer;
    private final double maxHealth;
    private final double moveSpeed;
    private final double attackDamage;
    private final int accentColor;
    private final String translationKey;
    private final Identifier texture;
    private final Identifier glowTexture;
    private final Identifier iconTexture;

    GolemRole(String id, int maxPerPlayer, double maxHealth, double moveSpeed, double attackDamage,
              int accentColor, String translationKey, String texturePath, String glowPath, String iconPath) {
        this.id = id;
        this.maxPerPlayer = maxPerPlayer;
        this.maxHealth = maxHealth;
        this.moveSpeed = moveSpeed;
        this.attackDamage = attackDamage;
        this.accentColor = accentColor;
        this.translationKey = translationKey;
        this.texture = Identifier.fromNamespaceAndPath("manyl_mod", texturePath);
        this.glowTexture = Identifier.fromNamespaceAndPath("manyl_mod", glowPath);
        this.iconTexture = Identifier.fromNamespaceAndPath("manyl_mod", iconPath);
    }

    public String getId() { return id; }
    public int getMaxPerPlayer() { return maxPerPlayer; }
    public double getMaxHealth() { return maxHealth; }
    public double getMoveSpeed() { return moveSpeed; }
    public double getAttackDamage() { return attackDamage; }
    public int getAccentColor() { return accentColor; }
    public String getTranslationKey() { return translationKey; }
    public Identifier getTexture() { return texture; }
    public Identifier getGlowTexture() { return glowTexture; }
    public Identifier getIconTexture() { return iconTexture; }

    public static GolemRole byOrdinal(int index) {
        GolemRole[] values = values();
        if (index < 0 || index >= values.length) return WARRIOR;
        return values[index];
    }
}
