package com.manyl.mod.network;

import com.manyl.mod.ManylMod;
import com.manyl.mod.data.ManylSavedData;
import com.manyl.mod.data.PlayerGolemSettings;
import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.entity.DigMode;
import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.registry.ModEntities;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Сетевые пакеты (C2S и S2C) для работы кругового меню и окна управления прислужниками.
 */
public final class ModNetworking {

    // S2C: открытие кругового меню или панели управления с актуальными счётчиками и настройками
    public record OpenStaffMenuPayload(
            int diggerCount, int farmerCount, int sorterCount, int warriorCount,
            int diggerRadius, int digModeOrdinal, boolean followOwner, boolean openManagement
    ) implements CustomPacketPayload {
        public static final Type<OpenStaffMenuPayload> TYPE = new Type<>(ManylMod.id("open_staff_menu"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenStaffMenuPayload> CODEC = StreamCodec.of(
                (buf, p) -> {
                    buf.writeVarInt(p.diggerCount);
                    buf.writeVarInt(p.farmerCount);
                    buf.writeVarInt(p.sorterCount);
                    buf.writeVarInt(p.warriorCount);
                    buf.writeVarInt(p.diggerRadius);
                    buf.writeVarInt(p.digModeOrdinal);
                    buf.writeBoolean(p.followOwner);
                    buf.writeBoolean(p.openManagement);
                },
                buf -> new OpenStaffMenuPayload(
                        buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                        buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean()
                )
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public int countFor(GolemRole role) {
            return switch (role) {
                case DIGGER -> diggerCount;
                case FARMER -> farmerCount;
                case SORTER -> sorterCount;
                case WARRIOR -> warriorCount;
            };
        }
    }

    // S2C: обновление живых счётчиков и настроек без переоткрытия окна
    public record SyncGolemStatePayload(
            int diggerCount, int farmerCount, int sorterCount, int warriorCount,
            int diggerRadius, int digModeOrdinal, boolean followOwner
    ) implements CustomPacketPayload {
        public static final Type<SyncGolemStatePayload> TYPE = new Type<>(ManylMod.id("sync_golem_state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncGolemStatePayload> CODEC = StreamCodec.of(
                (buf, p) -> {
                    buf.writeVarInt(p.diggerCount);
                    buf.writeVarInt(p.farmerCount);
                    buf.writeVarInt(p.sorterCount);
                    buf.writeVarInt(p.warriorCount);
                    buf.writeVarInt(p.diggerRadius);
                    buf.writeVarInt(p.digModeOrdinal);
                    buf.writeBoolean(p.followOwner);
                },
                buf -> new SyncGolemStatePayload(
                        buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                        buf.readVarInt(), buf.readVarInt(), buf.readBoolean()
                )
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public int countFor(GolemRole role) {
            return switch (role) {
                case DIGGER -> diggerCount;
                case FARMER -> farmerCount;
                case SORTER -> sorterCount;
                case WARRIOR -> warriorCount;
            };
        }
    }

    // C2S: запрос призыва прислужника определённой роли
    public record SummonGolemPayload(int roleOrdinal) implements CustomPacketPayload {
        public static final Type<SummonGolemPayload> TYPE = new Type<>(ManylMod.id("summon_golem"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SummonGolemPayload> CODEC =
                ByteBufCodecs.VAR_INT.map(SummonGolemPayload::new, SummonGolemPayload::roleOrdinal).cast();

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // C2S: запрос отзыва (-1 = всех прислужников, иначе конкретная роль)
    public record RecallGolemsPayload(int roleOrdinal) implements CustomPacketPayload {
        public static final Type<RecallGolemsPayload> TYPE = new Type<>(ManylMod.id("recall_golems"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RecallGolemsPayload> CODEC =
                ByteBufCodecs.VAR_INT.map(RecallGolemsPayload::new, RecallGolemsPayload::roleOrdinal).cast();

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // C2S: изменение настроек копателей и режима следования
    public record UpdateDiggerSettingsPayload(int diggerRadius, int digModeOrdinal, boolean followOwner) implements CustomPacketPayload {
        public static final Type<UpdateDiggerSettingsPayload> TYPE = new Type<>(ManylMod.id("update_digger_settings"));
        public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDiggerSettingsPayload> CODEC = StreamCodec.of(
                (buf, p) -> {
                    buf.writeVarInt(p.diggerRadius);
                    buf.writeVarInt(p.digModeOrdinal);
                    buf.writeBoolean(p.followOwner);
                },
                buf -> new UpdateDiggerSettingsPayload(buf.readVarInt(), buf.readVarInt(), buf.readBoolean())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // C2S: открыть панель управления из кругового меню
    public record RequestOpenManagementPayload() implements CustomPacketPayload {
        public static final Type<RequestOpenManagementPayload> TYPE = new Type<>(ManylMod.id("request_open_management"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestOpenManagementPayload> CODEC =
                StreamCodec.unit(new RequestOpenManagementPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(OpenStaffMenuPayload.TYPE, OpenStaffMenuPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncGolemStatePayload.TYPE, SyncGolemStatePayload.CODEC);

        PayloadTypeRegistry.playC2S().register(SummonGolemPayload.TYPE, SummonGolemPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RecallGolemsPayload.TYPE, RecallGolemsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateDiggerSettingsPayload.TYPE, UpdateDiggerSettingsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestOpenManagementPayload.TYPE, RequestOpenManagementPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SummonGolemPayload.TYPE, (payload, ctx) -> {
            ServerPlayer player = ctx.player();
            ctx.server().execute(() -> handleSummon(player, GolemRole.byOrdinal(payload.roleOrdinal())));
        });

        ServerPlayNetworking.registerGlobalReceiver(RecallGolemsPayload.TYPE, (payload, ctx) -> {
            ServerPlayer player = ctx.player();
            ctx.server().execute(() -> handleRecall(player, payload.roleOrdinal()));
        });

        ServerPlayNetworking.registerGlobalReceiver(UpdateDiggerSettingsPayload.TYPE, (payload, ctx) -> {
            ServerPlayer player = ctx.player();
            ctx.server().execute(() -> {
                MinecraftServer server = ((ServerLevel) player.level()).getServer();
                ManylSavedData data = ManylSavedData.get(server);
                data.updateSettings(player.getUUID(), payload.diggerRadius(), DigMode.byOrdinal(payload.digModeOrdinal()), payload.followOwner());
                ManylSavedData.applySettingsToActiveGolems(player, data.getSettings(player.getUUID()));
                sendStateSync(player);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(RequestOpenManagementPayload.TYPE, (payload, ctx) -> {
            ServerPlayer player = ctx.player();
            ctx.server().execute(() -> sendOpenStaffMenu(player, true));
        });
    }

    public static void sendOpenStaffMenu(ServerPlayer player, boolean openManagement) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        int[] counts = ManylSavedData.getRoleCounts(server, player.getUUID());
        PlayerGolemSettings settings = ManylSavedData.get(server).getSettings(player.getUUID());
        ServerPlayNetworking.send(player, new OpenStaffMenuPayload(
                counts[0], counts[1], counts[2], counts[3],
                settings.getDiggerRadius(), settings.getDigMode().ordinal(), settings.isFollowOwner(), openManagement
        ));
    }

    public static void sendStateSync(ServerPlayer player) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        int[] counts = ManylSavedData.getRoleCounts(server, player.getUUID());
        PlayerGolemSettings settings = ManylSavedData.get(server).getSettings(player.getUUID());
        ServerPlayNetworking.send(player, new SyncGolemStatePayload(
                counts[0], counts[1], counts[2], counts[3],
                settings.getDiggerRadius(), settings.getDigMode().ordinal(), settings.isFollowOwner()
        ));
    }

    private static void handleSummon(ServerPlayer player, GolemRole role) {
        ServerLevel level = (ServerLevel) player.level();
        MinecraftServer server = level.getServer();
        int[] counts = ManylSavedData.getRoleCounts(server, player.getUUID());
        int current = counts[role.ordinal()];
        if (current >= role.getMaxPerPlayer()) {
            player.displayClientMessage(Component.translatable("msg.manyl_mod.limit_reached", current, role.getMaxPerPlayer()), true);
            sendStateSync(player);
            return;
        }

        Vec3 look = player.getLookAngle();
        double sx = player.getX() + look.x * 1.8D + (player.getRandom().nextDouble() - 0.5D) * 0.8D;
        double sz = player.getZ() + look.z * 1.8D + (player.getRandom().nextDouble() - 0.5D) * 0.8D;
        double sy = player.getY();

        BaseGolemEntity golem = ModEntities.forRole(role).create(level, EntitySpawnReason.MOB_SUMMONED);
        if (golem != null) {
            PlayerGolemSettings settings = ManylSavedData.get(server).getSettings(player.getUUID());
            golem.setPos(sx, sy, sz);
            golem.setYRot(player.getYRot() + 180.0F);
            golem.setOwnerUuid(player.getUUID());
            golem.setHomePos(settings.getHomePos());
            golem.setSitting(!settings.isFollowOwner());
            golem.setSummonTicks(24);
            level.addFreshEntity(golem);

            level.playSound(null, sx, sy, sz, ModSounds.GOLEM_SUMMON, player.getSoundSource(), 1.0F, 1.0F);
            level.sendParticles(ModParticles.SUMMON_RUNE, sx, sy + 0.5D, sz, 14, 0.35D, 0.4D, 0.35D, 0.04D);

            player.displayClientMessage(Component.translatable("msg.manyl_mod.summoned",
                    Component.translatable(role.getTranslationKey()), current + 1, role.getMaxPerPlayer()), true);
        }
        sendStateSync(player);
    }

    private static void handleRecall(ServerPlayer player, int roleOrdinal) {
        ServerLevel level = (ServerLevel) player.level();
        MinecraftServer server = level.getServer();
        List<BaseGolemEntity> golems = ManylSavedData.getPlayerGolems(server, player.getUUID());
        int recalled = 0;

        for (BaseGolemEntity golem : golems) {
            if (roleOrdinal >= 0 && golem.getRole().ordinal() != roleOrdinal) continue;

            // Сохраняем предметы из инвентаря голема при отзыве: передаём игроку или бросаем у ног
            for (int i = 0; i < BaseGolemEntity.INVENTORY_SIZE; i++) {
                ItemStack stack = golem.getGolemInventory().getItem(i);
                if (!stack.isEmpty()) {
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                    golem.getGolemInventory().setItem(i, ItemStack.EMPTY);
                }
            }

            if (golem.level() instanceof ServerLevel gl) {
                gl.sendParticles(ModParticles.SUMMON_RUNE, golem.getX(), golem.getY() + 0.45D, golem.getZ(), 10, 0.25D, 0.3D, 0.25D, 0.03D);
            }
            golem.discard();
            recalled++;
        }

        if (recalled > 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.GOLEM_RECALL, player.getSoundSource(), 0.9F, 1.0F);
            player.displayClientMessage(Component.translatable("msg.manyl_mod.recalled", recalled), true);
        }
        sendStateSync(player);
    }
}
