package com.manyl.mod.data;

import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.entity.DigMode;
import com.manyl.mod.entity.GolemRole;
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
 * Серверное хранилище данных Manyl_mod:
 * сохраняет настройки копателей/прислужников для каждого игрока между перезапусками мира
 * и управляет подсчётом, призывом и отзывом големов с учётом лимитов (4 / 2 / 6 / 5).
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

    public void updateSettings(UUID playerUuid, int radius, DigMode mode, boolean followOwner) {
        PlayerGolemSettings s = getSettings(playerUuid);
        s.setDiggerRadius(radius);
        s.setDigMode(mode);
        s.setFollowOwner(followOwner);
        setDirty();
    }

    public void setHomePos(UUID playerUuid, BlockPos pos) {
        PlayerGolemSettings s = getSettings(playerUuid);
        s.setHomePos(pos);
        setDirty();
    }

    /**
     * Собирает всех живых прислужников игрока во всех загруженных измерениях сервера.
     */
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

    /**
     * Возвращает количество активных големов каждой роли у игрока.
     */
    public static int[] getRoleCounts(MinecraftServer server, UUID ownerUuid) {
        int[] counts = new int[GolemRole.values().length];
        for (BaseGolemEntity golem : getPlayerGolems(server, ownerUuid)) {
            counts[golem.getRole().ordinal()]++;
        }
        return counts;
    }

    /**
     * Применяет обновлённые настройки следования и точки дома ко всем големам игрока.
     */
    public static void applySettingsToActiveGolems(ServerPlayer player, PlayerGolemSettings settings) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        for (BaseGolemEntity golem : getPlayerGolems(server, player.getUUID())) {
            golem.setSitting(!settings.isFollowOwner());
            if (settings.getHomePos() != null) {
                golem.setHomePos(settings.getHomePos());
            }
        }
    }
}
