package com.manyl.mod.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.Optional;

/**
 * Персональные настройки големов-воинов игрока.
 */
public class PlayerGolemSettings {
    public static final Codec<PlayerGolemSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("follow_owner", true).forGetter(PlayerGolemSettings::isFollowOwner),
            BlockPos.CODEC.optionalFieldOf("home_pos").forGetter(s -> Optional.ofNullable(s.getHomePos())),
            Codec.STRING.optionalFieldOf("guard_mode", "follow").forGetter(PlayerGolemSettings::getGuardMode),
            Codec.STRING.optionalFieldOf("target_filter", "").forGetter(PlayerGolemSettings::getTargetFilter)
    ).apply(instance, PlayerGolemSettings::new));

    private boolean followOwner;
    private BlockPos homePos;
    private String guardMode;
    private String targetFilter;

    public PlayerGolemSettings() {
        this(true, Optional.empty(), "follow", "");
    }

    public PlayerGolemSettings(boolean followOwner, Optional<BlockPos> homePos, String guardMode, String targetFilter) {
        this.followOwner = followOwner;
        this.homePos = homePos.orElse(null);
        this.guardMode = (guardMode == null || guardMode.isEmpty()) ? "follow" : guardMode;
        this.targetFilter = targetFilter == null ? "" : targetFilter;
    }

    public boolean isFollowOwner() { return followOwner; }
    public void setFollowOwner(boolean followOwner) { this.followOwner = followOwner; }

    public BlockPos getHomePos() { return homePos; }
    public void setHomePos(BlockPos homePos) { this.homePos = homePos; }

    public String getGuardMode() { return guardMode; }
    public void setGuardMode(String guardMode) { this.guardMode = (guardMode == null || guardMode.isEmpty()) ? "follow" : guardMode; }

    public String getTargetFilter() { return targetFilter; }
    public void setTargetFilter(String targetFilter) { this.targetFilter = targetFilter == null ? "" : targetFilter; }
}
