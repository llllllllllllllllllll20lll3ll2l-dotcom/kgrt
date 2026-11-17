package com.manyl.mod.entity;

import net.minecraft.resources.Identifier;

/**
 * Перечисление всех типов прислужников-големов в Manyl_mod.
 * Хранит идентификаторы, лимиты количества на игрока, текстуры, иконки и базовые характеристики.
 */
public enum GolemRole {
    DIGGER("digger_golem", 4, 24.0D, 0.25D, 3.0D, 0xFFE59D3B,
            "entity.manyl_mod.digger_golem",
            "textures/entity/golem/digger_golem.png",
            "textures/entity/golem/digger_golem_glow.png",
            "textures/gui/icons/digger.png"),
    FARMER("farmer_golem", 2, 20.0D, 0.26D, 2.0D, 0xFF66B846,
            "entity.manyl_mod.farmer_golem",
            "textures/entity/golem/farmer_golem.png",
            "textures/entity/golem/farmer_golem_glow.png",
            "textures/gui/icons/farmer.png"),
    SORTER("sorter_golem", 6, 18.0D, 0.30D, 2.0D, 0xFF40E8DC,
            "entity.manyl_mod.sorter_golem",
            "textures/entity/golem/sorter_golem.png",
            "textures/entity/golem/sorter_golem_glow.png",
            "textures/gui/icons/sorter.png"),
    WARRIOR("warrior_golem", 5, 36.0D, 0.29D, 7.0D, 0xFFE24444,
            "entity.manyl_mod.warrior_golem",
            "textures/entity/golem/warrior_golem.png",
            "textures/entity/golem/warrior_golem_glow.png",
            "textures/gui/icons/warrior.png");

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
        if (index < 0 || index >= values.length) return DIGGER;
        return values[index];
    }
}
