package com.manyl.mod.item;

import com.manyl.mod.data.ManylSavedData;
import com.manyl.mod.data.PlayerGolemSettings;
import com.manyl.mod.network.ModNetworking;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Второй посох — Посох разметки карьера:
 * 1-й клик ПКМ по блоку — выделяет первую точку;
 * 2-й клик ПКМ по блоку — выделяет вторую точку и сразу открывает меню настроек карьера;
 * Shift + ПКМ — сбрасывает разметку карьера.
 */
public class QuarryStaffItem extends Item {
    public QuarryStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerLevel sl = (ServerLevel) level;
            MinecraftServer server = sl.getServer();
            ManylSavedData data = ManylSavedData.get(server);
            PlayerGolemSettings settings = data.getSettings(serverPlayer.getUUID());

            if (player.isShiftKeyDown()) {
                clearQuarry(serverPlayer, data, settings, sl);
                return InteractionResult.SUCCESS;
            }

            BlockPos clicked = ctx.getClickedPos().immutable();

            // Если 1-я точка ещё не задана ИЛИ уже заданы обе точки -> начинаем новую разметку с 1-й точки
            if (settings.getQuarryPos1() == null || settings.getQuarryPos2() != null) {
                settings.setQuarryPos1(clicked);
                settings.setQuarryPos2(null);
                settings.setQuarryActive(false);
                data.setDirty();

                sl.playSound(null, clicked, ModSounds.GOLEM_SUMMON, player.getSoundSource(), 0.45F, 1.25F);
                spawnCornerParticles(sl, serverPlayer, clicked);
                serverPlayer.displayClientMessage(Component.translatable(
                        "msg.manyl_mod.quarry_pos1", clicked.getX(), clicked.getY(), clicked.getZ()), false);
            } else {
                // Задана 1-я точка, ставим 2-ю точку и открываем меню настроек карьера!
                BlockPos p1 = settings.getQuarryPos1();
                int dx = Math.abs(clicked.getX() - p1.getX()) + 1;
                int dz = Math.abs(clicked.getZ() - p1.getZ()) + 1;
                if (dx > 24 || dz > 24) {
                    serverPlayer.displayClientMessage(Component.translatable("msg.manyl_mod.quarry_too_large", dx, dz), false);
                    return InteractionResult.FAIL;
                }

                settings.setQuarryPos2(clicked);
                data.setDirty();

                sl.playSound(null, clicked, ModSounds.GOLEM_SUMMON, player.getSoundSource(), 0.55F, 1.05F);
                spawnCornerParticles(sl, serverPlayer, clicked);
                serverPlayer.displayClientMessage(Component.translatable(
                        "msg.manyl_mod.quarry_pos2", clicked.getX(), clicked.getY(), clicked.getZ(), dx, dz), false);

                ModNetworking.sendOpenQuarryConfig(serverPlayer);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerLevel sl = (ServerLevel) level;
            MinecraftServer server = sl.getServer();
            ManylSavedData data = ManylSavedData.get(server);
            PlayerGolemSettings settings = data.getSettings(serverPlayer.getUUID());

            if (player.isShiftKeyDown()) {
                clearQuarry(serverPlayer, data, settings, sl);
                return InteractionResult.SUCCESS;
            }
            if (settings.hasQuarryBounds()) {
                ModNetworking.sendOpenQuarryConfig(serverPlayer);
                return InteractionResult.SUCCESS;
            } else {
                serverPlayer.displayClientMessage(Component.translatable("msg.manyl_mod.quarry_hint"), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) return;
        if (player.tickCount % 10 != 0) return;

        PlayerGolemSettings settings = ManylSavedData.get(level.getServer()).getSettings(player.getUUID());
        if (settings.getQuarryPos1() == null) return;

        if (!settings.hasQuarryBounds()) {
            spawnCornerParticles(level, player, settings.getQuarryPos1());
        } else {
            int minX = settings.getQuarryMinX();
            int maxX = settings.getQuarryMaxX();
            int minZ = settings.getQuarryMinZ();
            int maxZ = settings.getQuarryMaxZ();
            double y = settings.getQuarryTopY() + 1.15D;

            // Рисуем светящийся периметр выделенного карьера частицами для держащего посох игрока
            for (int x = minX; x <= maxX; x += Math.max(1, (maxX - minX) / 8)) {
                level.sendParticles(player, ModParticles.SUMMON_RUNE, true, false, x + 0.5D, y, minZ + 0.5D, 1, 0, 0, 0, 0);
                level.sendParticles(player, ModParticles.SUMMON_RUNE, true, false, x + 0.5D, y, maxZ + 0.5D, 1, 0, 0, 0, 0);
            }
            for (int z = minZ; z <= maxZ; z += Math.max(1, (maxZ - minZ) / 8)) {
                level.sendParticles(player, ModParticles.SUMMON_RUNE, true, false, minX + 0.5D, y, z + 0.5D, 1, 0, 0, 0, 0);
                level.sendParticles(player, ModParticles.SUMMON_RUNE, true, false, maxX + 0.5D, y, z + 0.5D, 1, 0, 0, 0, 0);
            }
        }
    }

    private static void clearQuarry(ServerPlayer player, ManylSavedData data, PlayerGolemSettings settings, ServerLevel level) {
        settings.setQuarryPos1(null);
        settings.setQuarryPos2(null);
        settings.setQuarryActive(false);
        data.setDirty();
        level.playSound(null, player.blockPosition(), ModSounds.GOLEM_RECALL, player.getSoundSource(), 0.45F, 1.1F);
        player.displayClientMessage(Component.translatable("msg.manyl_mod.quarry_cleared"), true);
    }

    private static void spawnCornerParticles(ServerLevel level, ServerPlayer player, BlockPos pos) {
        level.sendParticles(player, ModParticles.SUMMON_RUNE, true, false,
                pos.getX() + 0.5D, pos.getY() + 1.15D, pos.getZ() + 0.5D, 6, 0.2D, 0.25D, 0.2D, 0.01D);
    }
}
