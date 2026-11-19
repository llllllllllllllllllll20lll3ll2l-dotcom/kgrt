package com.manyl.mod.network;

import com.manyl.mod.data.ManylSavedData;
import com.manyl.mod.data.PlayerGolemSettings;
import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

/**
 * Чат-команды Manyl_mod (только Воины и общие команды).
 */
public final class ChatCommandHandler {
    private ChatCommandHandler() {}

    public static void init() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String raw = message.signedContent().trim();
            if (!raw.startsWith("!")) {
                return true;
            }
            MinecraftServer server = ((ServerLevel) sender.level()).getServer();
            server.execute(() -> handleCommand(sender, raw));
            return false;
        });
    }

    private static void handleCommand(ServerPlayer player, String raw) {
        String withoutBang = raw.substring(1).trim();
        if (withoutBang.isEmpty()) {
            sendHelp(player);
            return;
        }
        String[] parts = withoutBang.split("\\s+", 2);
        String cmd = parts[0].toLowerCase(Locale.ROOT);
        String arg = parts.length > 1 ? parts[1].trim() : "";

        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        ManylSavedData data = ManylSavedData.get(server);
        PlayerGolemSettings settings = data.getSettings(player.getUUID());

        switch (cmd) {
            case "help", "помощь", "?" -> sendHelp(player);

            case "guard", "охрана" -> {
                String mode = normalizeGuardMode(arg);
                if (mode == null) {
                    reply(player, "§c[Воин]§r Укажите приказ: §bfollow§r (следовать), §bstay§r (стоять), §bpatrol§r (патруль точки).");
                    return;
                }
                settings.setGuardMode(mode);
                settings.setFollowOwner("follow".equals(mode));
                data.setDirty();
                ManylSavedData.applySettingsToActiveGolems(player, settings);
                ModNetworking.sendStateSync(player);
                reply(player, "§c[Воин]§r Режим охраны: §a" + mode);
            }
            case "target", "цель" -> {
                if (arg.isEmpty()) {
                    String cur = settings.getTargetFilter().isEmpty() ? "все враги" : settings.getTargetFilter();
                    reply(player, "§c[Воин]§r Текущая цель: §e" + cur + "§r. Пример: §b!target крипер §rили §b!target all");
                    return;
                }
                String normalized = normalizeMobArg(arg);
                settings.setTargetFilter(normalized);
                data.setDirty();
                ModNetworking.sendStateSync(player);
                if (normalized.isEmpty()) {
                    reply(player, "§c[Воин]§r Атакуют всех враждебных мобов.");
                } else {
                    reply(player, "§c[Воин]§r Приоритетная цель: §c" + normalized);
                }
            }

            case "status", "статус" -> sendStatus(player, server, settings);
            case "feed", "покормить" -> feedAllGolems(player, server);
            case "recall", "отозвать" -> recallAll(player, server);

            default -> reply(player, "§cНеизвестная команда: §f!" + cmd + "§c. Введите §b!help§c для списка команд.");
        }
    }

    private static void sendHelp(ServerPlayer player) {
        player.displayClientMessage(Component.literal("§c§l=== Команды Manyl_mod (Воины) ==="), false);
        player.displayClientMessage(Component.literal("§e!help §7— показать список команд"), false);
        player.displayClientMessage(Component.literal("§c[Воин] §b!guard <follow|stay|patrol> §7— следовать, стоять или патрулировать"), false);
        player.displayClientMessage(Component.literal("§c[Воин] §b!target <моб|all> §7— атаковать конкретных мобов"), false);
        player.displayClientMessage(Component.literal("§d[Общее] §b!status §7| §b!feed §7| §b!recall"), false);
    }

    private static void sendStatus(ServerPlayer player, MinecraftServer server, PlayerGolemSettings s) {
        List<BaseGolemEntity> golems = ManylSavedData.getPlayerGolems(server, player.getUUID());
        int wCount = ManylSavedData.getWarriorCount(server, player.getUUID());
        int vCount = ManylSavedData.getValkyrieCount(server, player.getUUID());
        reply(player, String.format("§c[Статус]§r Воины: §c%d§r | Воительницы: §d%d§r (без лимита) | Режим: §e%s§r | Цель: §e%s",
                wCount, vCount,
                s.getGuardMode(),
                s.getTargetFilter().isEmpty() ? "все" : s.getTargetFilter()));
        for (BaseGolemEntity g : golems) {
            player.displayClientMessage(Component.literal(String.format(" §8• §f%s §7[HP: §c%.0f/%.0f§7, Сытость: §e%d%%§7]",
                    g.getDisplayName().getString(), g.getHealth(), g.getMaxHealth(), g.getHunger())), false);
        }
    }

    private static void feedAllGolems(ServerPlayer player, MinecraftServer server) {
        int fed = 0;
        for (BaseGolemEntity g : ManylSavedData.getPlayerGolems(server, player.getUUID())) {
            if (g.tryEatFromInventory()) fed++;
        }
        reply(player, "§aПокормили воинов из их запасов: §f" + fed);
    }

    private static void recallAll(ServerPlayer player, MinecraftServer server) {
        List<BaseGolemEntity> golems = ManylSavedData.getPlayerGolems(server, player.getUUID());
        int count = 0;
        for (BaseGolemEntity g : golems) {
            for (int i = 0; i < BaseGolemEntity.INVENTORY_SIZE; i++) {
                ItemStack st = g.getGolemInventory().getItem(i);
                if (!st.isEmpty()) {
                    if (!player.getInventory().add(st)) player.drop(st, false);
                    g.getGolemInventory().setItem(i, ItemStack.EMPTY);
                }
            }
            if (g.level() instanceof ServerLevel gl) {
                gl.sendParticles(ModParticles.SUMMON_RUNE, g.getX(), g.getY() + 0.45D, g.getZ(), 10, 0.25D, 0.3D, 0.25D, 0.03D);
            }
            g.discard();
            count++;
        }
        if (count > 0) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.GOLEM_RECALL, player.getSoundSource(), 0.6F, 1.0F);
        }
        ModNetworking.sendStateSync(player);
        reply(player, "§dОтозвано воинов: §f" + count);
    }

    private static String normalizeGuardMode(String raw) {
        return switch (raw.toLowerCase(Locale.ROOT).trim()) {
            case "follow", "следовать", "за мной" -> "follow";
            case "stay", "стоять", "место" -> "stay";
            case "patrol", "патруль", "точка" -> "patrol";
            default -> null;
        };
    }

    private static String normalizeMobArg(String raw) {
        String s = raw.toLowerCase(Locale.ROOT).trim();
        return switch (s) {
            case "all", "reset", "все", "всё", "враги", "сброс" -> "";
            case "крипер" -> "creeper";
            case "зомби" -> "zombie";
            case "скелет" -> "skeleton";
            case "паук" -> "spider";
            case "эндермен", "странник" -> "enderman";
            case "ведьма" -> "witch";
            case "слизень" -> "slime";
            case "фантом" -> "phantom";
            case "утопленник" -> "drowned";
            case "ифрит" -> "blaze";
            case "пиглин" -> "piglin";
            default -> s;
        };
    }

    private static void reply(ServerPlayer player, String msg) {
        player.displayClientMessage(Component.literal(msg), false);
    }
}
