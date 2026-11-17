package com.manyl.mod.data;

import com.manyl.mod.entity.DigMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.Optional;

/**
 * Персональные настройки прислужников игрока:
 * радиус копки, режим копки, следование за владельцем и привязанная точка дома/сундука.
 */
public class PlayerGolemSettings {
    public static final Codec<PlayerGolemSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("digger_radius", 4).forGetter(PlayerGolemSettings::getDiggerRadius),
            Codec.INT.optionalFieldOf("dig_mode", 0).forGetter(s -> s.getDigMode().ordinal()),
            Codec.BOOL.optionalFieldOf("follow_owner", true).forGetter(PlayerGolemSettings::isFollowOwner),
            BlockPos.CODEC.optionalFieldOf("home_pos").forGetter(s -> Optional.ofNullable(s.getHomePos()))
    ).apply(instance, PlayerGolemSettings::new));

    private int diggerRadius;
    private DigMode digMode;
    private boolean followOwner;
    private BlockPos homePos;

    public PlayerGolemSettings() {
        this(4, 0, true, Optional.empty());
    }

    public PlayerGolemSettings(int diggerRadius, int digModeOrdinal, boolean followOwner, Optional<BlockPos> homePos) {
        this.diggerRadius = Math.max(2, Math.min(8, diggerRadius));
        this.digMode = DigMode.byOrdinal(digModeOrdinal);
        this.followOwner = followOwner;
        this.homePos = homePos.orElse(null);
    }

    public int getDiggerRadius() { return diggerRadius; }
    public void setDiggerRadius(int radius) { this.diggerRadius = Math.max(2, Math.min(8, radius)); }

    public DigMode getDigMode() { return digMode; }
    public void setDigMode(DigMode mode) { this.digMode = mode; }

    public boolean isFollowOwner() { return followOwner; }
    public void setFollowOwner(boolean followOwner) { this.followOwner = followOwner; }

    public BlockPos getHomePos() { return homePos; }
    public void setHomePos(BlockPos homePos) { this.homePos = homePos; }
}
