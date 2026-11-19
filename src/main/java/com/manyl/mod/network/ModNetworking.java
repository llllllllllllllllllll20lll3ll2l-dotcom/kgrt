package com.manyl.mod.network;

import com.manyl.mod.ManylMod;
import com.manyl.mod.data.ManylSavedData;
import com.manyl.mod.data.PlayerGolemSettings;
import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.entity.ValkyrieGolemEntity;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Сетевые пакеты (C2S и S2C) для управления отрядом Воинов, Воительниц и романтических диалогов.
 */
public final class ModNetworking {

    public record OpenStaffMenuPayload(
            int warriorCount, int valkyrieCount, boolean followOwner, String guardMode, String targetFilter, boolean openManagement
    ) implements CustomPacketPayload {
        public static final Type<OpenStaffMenuPayload> TYPE = new Type<>(ManylMod.id("open_staff_menu"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenStaffMenuPayload> CODEC = StreamCodec.of(
                (buf, p) -> {
                    buf.writeVarInt(p.warriorCount);
                    buf.writeVarInt(p.valkyrieCount);
                    buf.writeBoolean(p.followOwner);
                    buf.writeUtf(p.guardMode == null ? "follow" : p.guardMode, 32);
                    buf.writeUtf(p.targetFilter == null ? "" : p.targetFilter, 64);
                    buf.writeBoolean(p.openManagement);
                },
                buf -> new OpenStaffMenuPayload(
                        buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readUtf(32), buf.readUtf(64), buf.readBoolean()
                )
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record SyncGolemStatePayload(
            int warriorCount, int valkyrieCount, boolean followOwner, String guardMode, String targetFilter
    ) implements CustomPacketPayload {
        public static final Type<SyncGolemStatePayload> TYPE = new Type<>(ManylMod.id("sync_golem_state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncGolemStatePayload> CODEC = StreamCodec.of(
                (buf, p) -> {
                    buf.writeVarInt(p.warriorCount);
                    buf.writeVarInt(p.valkyrieCount);
                    buf.writeBoolean(p.followOwner);
                    buf.writeUtf(p.guardMode == null ? "follow" : p.guardMode, 32);
                    buf.writeUtf(p.targetFilter == null ? "" : p.targetFilter, 64);
                },
                buf -> new SyncGolemStatePayload(
                        buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readUtf(32), buf.readUtf(64)
                )
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record SummonGolemPayload(int roleOrdinal) implements CustomPacketPayload {
        public static final Type<SummonGolemPayload> TYPE = new Type<>(ManylMod.id("summon_golem"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SummonGolemPayload> CODEC =
                ByteBufCodecs.VAR_INT.map(SummonGolemPayload::new, SummonGolemPayload::roleOrdinal).cast();

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record RecallGolemsPayload(int roleOrdinal) implements CustomPacketPayload {
        public static final Type<RecallGolemsPayload> TYPE = new Type<>(ManylMod.id("recall_golems"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RecallGolemsPayload> CODEC =
                ByteBufCodecs.VAR_INT.map(RecallGolemsPayload::new, RecallGolemsPayload::roleOrdinal).cast();

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record UpdateWarriorSettingsPayload(
            boolean followOwner, String guardMode, String targetFilter
    ) implements CustomPacketPayload {
        public static final Type<UpdateWarriorSettingsPayload> TYPE = new Type<>(ManylMod.id("update_warrior_settings"));
        public static final StreamCodec<RegistryFriendlyByteBuf, UpdateWarriorSettingsPayload> CODEC = StreamCodec.of(
                (buf, p) -> {
                    buf.writeBoolean(p.followOwner);
                    buf.writeUtf(p.guardMode == null ? "follow" : p.guardMode, 32);
                    buf.writeUtf(p.targetFilter == null ? "" : p.targetFilter, 64);
                },
                buf -> new UpdateWarriorSettingsPayload(
                        buf.readBoolean(), buf.readUtf(32), buf.readUtf(64)
                )
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record RequestOpenManagementPayload() implements CustomPacketPayload {
        public static final Type<RequestOpenManagementPayload> TYPE = new Type<>(ManylMod.id("request_open_management"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestOpenManagementPayload> CODEC =
                StreamCodec.unit(new RequestOpenManagementPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record OpenValkyrieDialoguePayload(
            int entityId, boolean tamed, boolean sitting, int affection, int romanceStage, String dialogueLine
    ) implements CustomPacketPayload {
        public static final Type<OpenValkyrieDialoguePayload> TYPE = new Type<>(ManylMod.id("open_valkyrie_dialogue"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenValkyrieDialoguePayload> CODEC = StreamCodec.of(
                (buf, p) -> {
                    buf.writeVarInt(p.entityId);
                    buf.writeBoolean(p.tamed);
                    buf.writeBoolean(p.sitting);
                    buf.writeVarInt(p.affection);
                    buf.writeVarInt(p.romanceStage);
                    buf.writeUtf(p.dialogueLine == null ? "" : p.dialogueLine, 512);
                },
                buf -> new OpenValkyrieDialoguePayload(
                        buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(512)
                )
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ValkyrieRomanceActionPayload(int entityId, int actionId) implements CustomPacketPayload {
        public static final Type<ValkyrieRomanceActionPayload> TYPE = new Type<>(ManylMod.id("valkyrie_romance_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ValkyrieRomanceActionPayload> CODEC = StreamCodec.of(
                (buf, p) -> {
                    buf.writeVarInt(p.entityId);
                    buf.writeVarInt(p.actionId);
                },
                buf -> new ValkyrieRomanceActionPayload(buf.readVarInt(), buf.readVarInt())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ToggleGolemSitPayload(int entityId) implements CustomPacketPayload {
        public static final Type<ToggleGolemSitPayload> TYPE = new Type<>(ManylMod.id("toggle_golem_sit"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ToggleGolemSitPayload> CODEC =
                ByteBufCodecs.VAR_INT.map(ToggleGolemSitPayload::new, ToggleGolemSitPayload::entityId).cast();

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(OpenStaffMenuPayload.TYPE, OpenStaffMenuPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncGolemStatePayload.TYPE, SyncGolemStatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenValkyrieDialoguePayload.TYPE, OpenValkyrieDialoguePayload.CODEC);

        PayloadTypeRegistry.playC2S().register(SummonGolemPayload.TYPE, SummonGolemPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RecallGolemsPayload.TYPE, RecallGolemsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateWarriorSettingsPayload.TYPE, UpdateWarriorSettingsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestOpenManagementPayload.TYPE, RequestOpenManagementPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ValkyrieRomanceActionPayload.TYPE, ValkyrieRomanceActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleGolemSitPayload.TYPE, ToggleGolemSitPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SummonGolemPayload.TYPE, (payload, ctx) -> {
            ServerPlayer player = ctx.player();
            int roleOrdinal = payload.roleOrdinal();
            ctx.server().execute(() -> handleSummon(player, GolemRole.byOrdinal(roleOrdinal)));
        });

        ServerPlayNetworking.registerGlobalReceiver(RecallGolemsPayload.TYPE, (payload, ctx) -> {
            ServerPlayer player = ctx.player();
            int roleOrdinal = payload.roleOrdinal();
            ctx.server().execute(() -> handleRecall(player, roleOrdinal));
        });

        ServerPlayNetworking.registerGlobalReceiver(UpdateWarriorSettingsPayload.TYPE, (payload, ctx) -> {
            ServerPlayer player = ctx.player();
            ctx.server().execute(() -> {
                MinecraftServer server = ((ServerLevel) player.level()).getServer();
                ManylSavedData data = ManylSavedData.get(server);
                data.updateWarriorSettings(player.getUUID(), payload.followOwner(), payload.guardMode(), payload.targetFilter());
                ManylSavedData.applySettingsToActiveGolems(player, data.getSettings(player.getUUID()));
                sendStateSync(player);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(RequestOpenManagementPayload.TYPE, (payload, ctx) -> {
            ServerPlayer player = ctx.player();
            ctx.server().execute(() -> sendOpenStaffMenu(player, true));
        });

        ServerPlayNetworking.registerGlobalReceiver(ValkyrieRomanceActionPayload.TYPE, (payload, ctx) -> {
            ServerPlayer player = ctx.player();
            int entityId = payload.entityId();
            int actionId = payload.actionId();
            ctx.server().execute(() -> {
                Entity entity = player.level().getEntity(entityId);
                if (entity instanceof ValkyrieGolemEntity valkyrie && valkyrie.isAlive()
                        && valkyrie.distanceToSqr(player) <= 100.0D) {
                    if (actionId == ValkyrieGolemEntity.ACTION_OPEN_INVENTORY) {
                        if (valkyrie.isTamed()) {
                            valkyrie.openInventoryMenuFromDialogue(player);
                        }
                        return;
                    }
                    String replyLine = valkyrie.handleRomanceAction(player, actionId);
                    sendValkyrieDialogueWithLine(player, valkyrie, replyLine);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ToggleGolemSitPayload.TYPE, (payload, ctx) -> {
            ServerPlayer player = ctx.player();
            int entityId = payload.entityId();
            ctx.server().execute(() -> {
                Entity entity = player.level().getEntity(entityId);
                if (entity instanceof BaseGolemEntity golem && golem.isAlive()
                        && player.getUUID().equals(golem.getOwnerUuid())
                        && golem.distanceToSqr(player) <= 100.0D) {
                    golem.setSitting(!golem.isSitting());
                    golem.playSound(ModSounds.GOLEM_STEP, 0.16F, golem.isSitting() ? 0.8F : 1.2F);
                }
            });
        });
    }

    public static void sendOpenValkyrieDialogue(ServerPlayer player, ValkyrieGolemEntity valkyrie) {
        sendValkyrieDialogueWithLine(player, valkyrie, valkyrie.getGreetingLine(valkyrie.isTamed()));
    }

    public static void sendValkyrieDialogueWithLine(ServerPlayer player, ValkyrieGolemEntity valkyrie, String line) {
        ServerPlayNetworking.send(player, new OpenValkyrieDialoguePayload(
                valkyrie.getId(),
                valkyrie.isTamed(),
                valkyrie.isSitting(),
                valkyrie.getAffection(),
                valkyrie.getRomanceStage(),
                line
        ));
    }

    public static void sendOpenStaffMenu(ServerPlayer player, boolean openManagement) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        int wCount = ManylSavedData.getWarriorCount(server, player.getUUID());
        int vCount = ManylSavedData.getValkyrieCount(server, player.getUUID());
        PlayerGolemSettings s = ManylSavedData.get(server).getSettings(player.getUUID());
        ServerPlayNetworking.send(player, new OpenStaffMenuPayload(
                wCount, vCount, s.isFollowOwner(), s.getGuardMode(), s.getTargetFilter(), openManagement
        ));
    }

    public static void sendStateSync(ServerPlayer player) {
        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        int wCount = ManylSavedData.getWarriorCount(server, player.getUUID());
        int vCount = ManylSavedData.getValkyrieCount(server, player.getUUID());
        PlayerGolemSettings s = ManylSavedData.get(server).getSettings(player.getUUID());
        ServerPlayNetworking.send(player, new SyncGolemStatePayload(
                wCount, vCount, s.isFollowOwner(), s.getGuardMode(), s.getTargetFilter()
        ));
    }

    private static void handleSummon(ServerPlayer player, GolemRole role) {
        ServerLevel level = (ServerLevel) player.level();
        MinecraftServer server = level.getServer();
        int current = ManylSavedData.getRoleCount(server, player.getUUID(), role);

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
            golem.setSitting("stay".equals(settings.getGuardMode()));
            golem.setSummonTicks(24);
            level.addFreshEntity(golem);

            level.playSound(null, sx, sy, sz, ModSounds.GOLEM_SUMMON, player.getSoundSource(), 0.7F,
                    role == GolemRole.VALKYRIE ? 1.18F : 1.0F);
            level.sendParticles(ModParticles.SUMMON_RUNE, sx, sy + 0.5D, sz, 14, 0.35D, 0.4D, 0.35D, 0.04D);

            player.displayClientMessage(Component.translatable("msg.manyl_mod.summoned",
                    Component.translatable(role.getTranslationKey()), current + 1), true);
        }
        sendStateSync(player);
    }

    private static void handleRecall(ServerPlayer player, int roleOrdinal) {
        ServerLevel level = (ServerLevel) player.level();
        MinecraftServer server = level.getServer();
        List<BaseGolemEntity> golems = ManylSavedData.getPlayerGolems(server, player.getUUID());
        GolemRole filterRole = (roleOrdinal >= 0) ? GolemRole.byOrdinal(roleOrdinal) : null;
        int recalled = 0;

        for (BaseGolemEntity golem : golems) {
            if (filterRole != null && golem.getRole() != filterRole) continue;
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
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.GOLEM_RECALL, player.getSoundSource(), 0.65F, 1.0F);
            player.displayClientMessage(Component.translatable("msg.manyl_mod.recalled", recalled), true);
        }
        sendStateSync(player);
    }
}
