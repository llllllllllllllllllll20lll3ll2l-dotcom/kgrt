package com.manyl.mod.item;

import com.manyl.mod.data.ManylSavedData;
import com.manyl.mod.data.PlayerGolemSettings;
import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.entity.ValkyrieGolemEntity;
import com.manyl.mod.network.ModNetworking;
import com.manyl.mod.registry.ModEntities;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Крафтовое яйцо призыва стража (Воина или Воительницы) без ограничений на количество спутников.
 */
public class GuardianEggItem extends Item {
    private final GolemRole role;

    public GuardianEggItem(GolemRole role, Properties properties) {
        super(properties);
        this.role = role;
    }

    public GolemRole getRole() {
        return role;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos clicked = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = level.getBlockState(clicked).getCollisionShape(level, clicked).isEmpty()
                ? clicked
                : clicked.relative(face);

        ServerLevel serverLevel = (ServerLevel) level;
        BaseGolemEntity golem = ModEntities.forRole(role).create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
        if (golem == null) {
            return InteractionResult.FAIL;
        }

        double sx = spawnPos.getX() + 0.5D;
        double sy = spawnPos.getY();
        double sz = spawnPos.getZ() + 0.5D;
        golem.setPos(sx, sy, sz);
        if (player != null) {
            golem.setYRot(player.getYRot() + 180.0F);
            golem.setOwnerUuid(player.getUUID());
            MinecraftServer server = serverLevel.getServer();
            PlayerGolemSettings settings = ManylSavedData.get(server).getSettings(player.getUUID());
            golem.setHomePos(settings.getHomePos() != null ? settings.getHomePos() : spawnPos);
            golem.setSitting("stay".equals(settings.getGuardMode()));
        } else {
            golem.setHomePos(spawnPos);
        }
        golem.setSummonTicks(24);
        if (golem instanceof ValkyrieGolemEntity valkyrie) {
            valkyrie.setAffection(15);
        }

        serverLevel.addFreshEntity(golem);
        serverLevel.playSound(null, sx, sy, sz, ModSounds.GOLEM_SUMMON,
                player != null ? player.getSoundSource() : net.minecraft.sounds.SoundSource.NEUTRAL,
                0.75F, role == GolemRole.VALKYRIE ? 1.18F : 1.0F);
        serverLevel.sendParticles(ModParticles.SUMMON_RUNE, sx, sy + 0.6D, sz, 16, 0.35D, 0.45D, 0.35D, 0.04D);

        if (player != null && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            int total = ManylSavedData.getRoleCount(serverLevel.getServer(), serverPlayer.getUUID(), role);
            serverPlayer.displayClientMessage(Component.translatable("msg.manyl_mod.summoned",
                    Component.translatable(role.getTranslationKey()), total), true);
            ModNetworking.sendStateSync(serverPlayer);
        }

        return InteractionResult.CONSUME;
    }
}
