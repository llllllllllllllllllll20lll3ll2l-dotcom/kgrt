package com.manyl.mod.data;

import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.entity.DigMode;
import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.entity.WarriorGolemEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

/**
 * Серверное хранилище данных Manyl_mod.
 */
public class ManylSavedData extends SavedData {
    private static final Codec<ManylSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, PlayerGolemSettings.CODEC)
                    .optionalFieldOf("player_settings", Map.of())
                    .forGetter(d -> d.playerSettings)
    ).apply(instance, ManylSavedData::new));

    private static final SavedDataType<ManylSavedData> TYPE = new SavedDataType<>(
            "manyl_mod_data",
            ManylSavedData::new,
            CODEC,
            null
    );

    private final Map<String, PlayerGolemSettings> playerSettings = new HashMap<>();

    public ManylSavedData() {}

    private ManylSavedData(Map<String, PlayerGolemSettings> settings) {
        this.playerSettings.putAll(settings);
    }

    public static ManylSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public PlayerGolemSettings getSettings(UUID playerUuid) {
        return playerSettings.computeIfAbsent(playerUuid.toString(), k -> {
            setDirty();
            return new PlayerGolemSettings();
        });
    }

    public void updateDiggerSettings(UUID playerUuid, int radius, int depth, DigMode mode, boolean followOwner,
                                     String extractFilter, boolean dropTrash, boolean tunnelToOre) {
        PlayerGolemSettings s = getSettings(playerUuid);
        s.setDiggerRadius(radius);
        s.setDiggerDepth(depth);
        s.setDigMode(mode);
        s.setFollowOwner(followOwner);
        s.setExtractFilter(extractFilter);
        s.setDiggerDropTrash(dropTrash);
        s.setDiggerTunnelToOre(tunnelToOre);
        setDirty();
    }

    public void setHomePos(UUID playerUuid, BlockPos pos) {
        PlayerGolemSettings s = getSettings(playerUuid);
        s.setHomePos(pos);
        setDirty();
    }

    public static List<BaseGolemEntity> getPlayerGolems(MinecraftServer server, UUID ownerUuid) {
        List<BaseGolemEntity> result = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof BaseGolemEntity golem && golem.isAlive() && ownerUuid.equals(golem.getOwnerUuid())) {
                    result.add(golem);
                }
            }
        }
        return result;
    }

    public static int[] getRoleCounts(MinecraftServer server, UUID ownerUuid) {
        int[] counts = new int[GolemRole.values().length];
        for (BaseGolemEntity golem : getPlayerGolems(server, ownerUuid)) {
            counts[golem.getRole().ordinal()]++;
        }
        return counts;
    }

    public static void applySettingsToActiveGolems(ServerPlayer player, PlayerGolemSettings settings) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        for (BaseGolemEntity golem : getPlayerGolems(server, player.getUUID())) {
            if (golem instanceof WarriorGolemEntity) {
                String guard = settings.getGuardMode();
                if ("stay".equals(guard)) {
                    golem.setSitting(true);
                } else {
                    golem.setSitting(false);
                    if ("patrol".equals(guard) && golem.getHomePos() == null) {
                        golem.setHomePos(player.blockPosition());
                    }
                }
            } else if (settings.isFollowOwner()) {
                golem.setSitting(false);
            }
            if (settings.getHomePos() != null) {
                golem.setHomePos(settings.getHomePos());
            }
        }
    }
}
