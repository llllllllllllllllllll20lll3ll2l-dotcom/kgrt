package com.manyl.mod.data;

import com.manyl.mod.entity.DigMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.Optional;

/**
 * Персональные настройки прислужников игрока (версия 1.1):
 * - Копатели: радиус (2..10), глубина (2..12), режим копки, фильтр !extract, сброс лишних стройблоков, спуск к рудам;
 * - Фермеры: фильтр !crop;
 * - Сортировщики: режим !sort;
 * - Воины: режим !guard и цель !target;
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
            Codec.STRING.optionalFieldOf("target_filter", "").forGetter(PlayerGolemSettings::getTargetFilter),
            Codec.INT.optionalFieldOf("digger_depth", 4).forGetter(PlayerGolemSettings::getDiggerDepth),
            Codec.BOOL.optionalFieldOf("digger_drop_trash", true).forGetter(PlayerGolemSettings::isDiggerDropTrash),
            Codec.BOOL.optionalFieldOf("digger_tunnel_to_ore", true).forGetter(PlayerGolemSettings::isDiggerTunnelToOre)
    ).apply(instance, PlayerGolemSettings::new));

    private int diggerRadius;
    private int diggerDepth;
    private DigMode digMode;
    private boolean followOwner;
    private BlockPos homePos;
    private String extractFilter;
    private String cropFilter;
    private String sortMode;
    private String guardMode;
    private String targetFilter;
    private boolean diggerDropTrash;
    private boolean diggerTunnelToOre;

    public PlayerGolemSettings() {
        this(4, 0, true, Optional.empty(), "", "", "all", "follow", "", 4, true, true);
    }

    public PlayerGolemSettings(int diggerRadius, int digModeOrdinal, boolean followOwner, Optional<BlockPos> homePos,
                               String extractFilter, String cropFilter, String sortMode, String guardMode, String targetFilter,
                               int diggerDepth, boolean diggerDropTrash, boolean diggerTunnelToOre) {
        this.diggerRadius = Math.max(2, Math.min(10, diggerRadius));
        this.digMode = DigMode.byOrdinal(digModeOrdinal);
        this.followOwner = followOwner;
        this.homePos = homePos.orElse(null);
        this.extractFilter = extractFilter == null ? "" : extractFilter;
        this.cropFilter = cropFilter == null ? "" : cropFilter;
        this.sortMode = (sortMode == null || sortMode.isEmpty()) ? "all" : sortMode;
        this.guardMode = (guardMode == null || guardMode.isEmpty()) ? "follow" : guardMode;
        this.targetFilter = targetFilter == null ? "" : targetFilter;
        this.diggerDepth = Math.max(2, Math.min(12, diggerDepth));
        this.diggerDropTrash = diggerDropTrash;
        this.diggerTunnelToOre = diggerTunnelToOre;
    }

    public int getDiggerRadius() { return diggerRadius; }
    public void setDiggerRadius(int radius) { this.diggerRadius = Math.max(2, Math.min(10, radius)); }

    public int getDiggerDepth() { return diggerDepth; }
    public void setDiggerDepth(int depth) { this.diggerDepth = Math.max(2, Math.min(12, depth)); }

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

    public boolean isDiggerDropTrash() { return diggerDropTrash; }
    public void setDiggerDropTrash(boolean diggerDropTrash) { this.diggerDropTrash = diggerDropTrash; }

    public boolean isDiggerTunnelToOre() { return diggerTunnelToOre; }
    public void setDiggerTunnelToOre(boolean diggerTunnelToOre) { this.diggerTunnelToOre = diggerTunnelToOre; }
}
