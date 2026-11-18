package com.manyl.mod.item;

import com.manyl.mod.data.ManylSavedData;
import com.manyl.mod.network.ModNetworking;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Посох призыва воинов Manyl_mod:
 * - ПКМ: открывает круговое меню (Призвать / Отозвать / Управление);
 * - Shift + ПКМ по блоку: привязывает точку патрулирования/охраны.
 */
public class SummonStaffItem extends Item {
    public SummonStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendOpenStaffMenu(serverPlayer, false);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            BlockPos pos = context.getClickedPos();
            if (level.getBlockEntity(pos) instanceof Container || level.getBlockState(pos).isSolid()) {
                if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                    ManylSavedData data = ManylSavedData.get(((ServerLevel) level).getServer());
                    data.setHomePos(sp.getUUID(), pos);
                    ManylSavedData.applySettingsToActiveGolems(sp, data.getSettings(sp.getUUID()));
                    sp.displayClientMessage(Component.translatable("msg.manyl_mod.bound_home", pos.getX(), pos.getY(), pos.getZ()), true);
                    ((ServerLevel) level).sendParticles(ModParticles.SUMMON_RUNE,
                            pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, 8, 0.25D, 0.15D, 0.25D, 0.02D);
                    level.playSound(null, pos, ModSounds.GOLEM_SUMMON, sp.getSoundSource(), 0.5F, 1.2F);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return use(level, player, context.getHand());
    }
}
