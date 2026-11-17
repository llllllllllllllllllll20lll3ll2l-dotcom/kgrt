package com.manyl.mod.data;

import com.manyl.mod.entity.DigMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.Optional;

/**
 * Персональные настройки прислужников игрока (версия 1.1):
 * - Копатели: радиус, режим копки, фильтр !extract (конкретная руда/блок);
 * - Фермеры: фильтр !crop (культура);
 * - Сортировщики: режим !sort (all / ground / chests);
 * - Воины: режим !guard (follow / stay / patrol) и приоритетная цель !target;
 * - Общие: следование за владельцем и привязанная точка дома/сундука.
 */
public class PlayerGolemSettings {
    public static final Codec<PlayerGolemSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("digger_radius", 4).forGetter(PlayerGolemSettings::getDiggerRadius),
            Codec.INT.optionalFieldOf("dig_mode", 0).forGetter(s -> s.getDigMode().ordinal()),
            Codec.BOOL.optionalFieldOf("follow_owner", true).forGetter(PlayerGolemSettings::isFollowOwner),
            BlockPos.CODEC.optionalFieldOf("home_pos").forGetter(s -> Optional.ofNullable(s.getHomePos())),
            Codec.STRING.optionalFieldOf("extract_filter", "").forGetter(PlayerGolemSettings::getExtractFilter),
            Codec.STRING.optionalFieldOf("crop_filter", "").forGetter(PlayerGolemSettings::getCropFilter),
            Codec.STRING.optionalFieldOf("sort_mode", "all").forGetter(PlayerGolemSettings::getSortMode),
            Codec.STRING.optionalFieldOf("guard_mode", "follow").forGetter(PlayerGolemSettings::getGuardMode),
            Codec.STRING.optionalFieldOf("target_filter", "").forGetter(PlayerGolemSettings::getTargetFilter)
    ).apply(instance, PlayerGolemSettings::new));

    private int diggerRadius;
    private DigMode digMode;
    private boolean followOwner;
    private BlockPos homePos;
    private String extractFilter;
    private String cropFilter;
    private String sortMode;
    private String guardMode;
    private String targetFilter;

    public PlayerGolemSettings() {
        this(4, 0, true, Optional.empty(), "", "", "all", "follow", "");
    }

    public PlayerGolemSettings(int diggerRadius, int digModeOrdinal, boolean followOwner, Optional<BlockPos> homePos,
                               String extractFilter, String cropFilter, String sortMode, String guardMode, String targetFilter) {
        this.diggerRadius = Math.max(2, Math.min(8, diggerRadius));
        this.digMode = DigMode.byOrdinal(digModeOrdinal);
        this.followOwner = followOwner;
        this.homePos = homePos.orElse(null);
        this.extractFilter = extractFilter == null ? "" : extractFilter;
        this.cropFilter = cropFilter == null ? "" : cropFilter;
        this.sortMode = (sortMode == null || sortMode.isEmpty()) ? "all" : sortMode;
        this.guardMode = (guardMode == null || guardMode.isEmpty()) ? "follow" : guardMode;
        this.targetFilter = targetFilter == null ? "" : targetFilter;
    }

    public int getDiggerRadius() { return diggerRadius; }
    public void setDiggerRadius(int radius) { this.diggerRadius = Math.max(2, Math.min(8, radius)); }

    public DigMode getDigMode() { return digMode; }
    public void setDigMode(DigMode mode) { this.digMode = mode; }

    public boolean isFollowOwner() { return followOwner; }
    public void setFollowOwner(boolean followOwner) { this.followOwner = followOwner; }

    public BlockPos getHomePos() { return homePos; }
    public void setHomePos(BlockPos homePos) { this.homePos = homePos; }

    public String getExtractFilter() { return extractFilter; }
    public void setExtractFilter(String extractFilter) { this.extractFilter = extractFilter == null ? "" : extractFilter; }

    public String getCropFilter() { return cropFilter; }
    public void setCropFilter(String cropFilter) { this.cropFilter = cropFilter == null ? "" : cropFilter; }

    public String getSortMode() { return sortMode; }
    public void setSortMode(String sortMode) { this.sortMode = sortMode == null ? "all" : sortMode; }

    public String getGuardMode() { return guardMode; }
    public void setGuardMode(String guardMode) { this.guardMode = guardMode == null ? "follow" : guardMode; }

    public String getTargetFilter() { return targetFilter; }
    public void setTargetFilter(String targetFilter) { this.targetFilter = targetFilter == null ? "" : targetFilter; }
}
