package com.manyl.mod.network;

import com.manyl.mod.data.ManylSavedData;
import com.manyl.mod.data.PlayerGolemSettings;
import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.entity.DigMode;
import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Уникальные чат-команды Manyl_mod (версия 1.1):
 * - !help — полный список команд для всех классов;
 * - Копатели: !extract <руда/блок|all>, !digmode <quarry|tunnel|ores>, !radius <2..8>;
 * - Фермеры: !crop <культура|all>;
 * - Сортировщики: !sort <all|ground|chests>, !find <предмет>;
 * - Воины: !guard <follow|stay|patrol>, !target <моб|all>;
 * - Общие: !status, !feed, !recall [all|digger|farmer|sorter|warrior].
 */
public final class ChatCommandHandler {
    private ChatCommandHandler() {}

    public static void init() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String raw = message.signedContent().trim();
            if (!raw.startsWith("!")) {
                return true;
            }
            // Выполняем команду на серверном потоке
            MinecraftServer server = ((ServerLevel) sender.level()).getServer();
            server.execute(() -> handleCommand(sender, raw));
            return false; // не засоряем общий чат командой
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

            // --- Копатели ---
            case "extract", "добывать", "руда" -> {
                if (arg.isEmpty()) {
                    String cur = settings.getExtractFilter().isEmpty() ? "все" : settings.getExtractFilter();
                    reply(player, "§6[Копатель]§r Текущий фильтр: §e" + cur + "§r. Пример: §b!extract алмаз §rили §b!extract all");
                    return;
                }
                String normalized = normalizeOreArg(arg);
                settings.setExtractFilter(normalized);
                data.setDirty();
                ModNetworking.sendStateSync(player);
                if (normalized.isEmpty()) {
                    reply(player, "§6[Копатель]§r Фильтр сброшен — копают всё по текущему режиму.");
                } else {
                    reply(player, "§6[Копатель]§r Нацелены на добычу: §a" + normalized + "§r.");
                }
            }
            case "digmode", "режим" -> {
                DigMode mode = parseDigMode(arg);
                if (mode == null) {
                    reply(player, "§6[Копатель]§r Укажите режим: §bquarry§r (карьер), §btunnel§r (туннель) или §bores§r (руды).");
                    return;
                }
                settings.setDigMode(mode);
                data.setDirty();
                ModNetworking.sendStateSync(player);
                reply(player, "§6[Копатель]§r Режим копки: §a" + Component.translatable(mode.getTranslationKey()).getString());
            }
            case "radius", "радиус" -> {
                try {
                    int r = Integer.parseInt(arg);
                    settings.setDiggerRadius(r);
                    data.setDirty();
                    ModNetworking.sendStateSync(player);
                    reply(player, "§6[Копатель]§r Радиус копки установлен: §a" + settings.getDiggerRadius());
                } catch (NumberFormatException e) {
                    reply(player, "§6[Копатель]§r Укажите число от 2 до 10. Пример: §b!radius 6");
                }
            }

            case "depth", "глубина" -> {
                try {
                    int d = Integer.parseInt(arg);
                    settings.setDiggerDepth(d);
                    data.setDirty();
                    ModNetworking.sendStateSync(player);
                    reply(player, "§6[Копатель]§r Глубина копания установлена: §a" + settings.getDiggerDepth() + " бл.");
                } catch (NumberFormatException e) {
                    reply(player, "§6[Копатель]§r Укажите число от 2 до 12. Пример: §b!depth 6");
                }
            }

            case "quarry", "карьер" -> {
                if ("stop".equalsIgnoreCase(arg) || "pause".equalsIgnoreCase(arg) || "стоп".equalsIgnoreCase(arg)) {
                    settings.setQuarryActive(false);
                    data.setDirty();
                    reply(player, "§6[Карьер]§r Режим размеченного карьера остановлен.");
                } else if ("clear".equalsIgnoreCase(arg) || "сброс".equalsIgnoreCase(arg)) {
                    settings.setQuarryPos1(null);
                    settings.setQuarryPos2(null);
                    settings.setQuarryActive(false);
                    data.setDirty();
                    reply(player, "§6[Карьер]§r Разметка карьера сброшена.");
                } else if ("start".equalsIgnoreCase(arg) || "старт".equalsIgnoreCase(arg)) {
                    if (!settings.hasQuarryBounds()) {
                        reply(player, "§c[Карьер]§r Сначала выделите 2 точки Посохом разметки карьера!");
                    } else {
                        settings.setQuarryActive(true);
                        data.setDirty();
                        reply(player, "§a[Карьер]§r Карьер запущен!");
                    }
                } else {
                    reply(player, "§6[Карьер]§r Используйте Посох разметки карьера (2 точки ПКМ) или §b!quarry <start|stop|clear>");
                }
            }

            // --- Фермеры ---
            case "crop", "урожай", "ферма" -> {
                if (arg.isEmpty()) {
                    String cur = settings.getCropFilter().isEmpty() ? "все" : settings.getCropFilter();
                    reply(player, "§a[Фермер]§r Текущий фильтр культур: §e" + cur + "§r. Пример: §b!crop пшеница §rили §b!crop all");
                    return;
                }
                String normalized = normalizeCropArg(arg);
                settings.setCropFilter(normalized);
                data.setDirty();
                if (normalized.isEmpty()) {
                    reply(player, "§a[Фермер]§r Фильтр сброшен — собирают все созревшие культуры.");
                } else {
                    reply(player, "§a[Фермер]§r Собирают только культуру: §a" + normalized);
                }
            }

            // --- Сортировщики ---
            case "sort", "сорт" -> {
                String mode = normalizeSortMode(arg);
                if (mode == null) {
                    reply(player, "§b[Сортировщик]§r Укажите режим: §ball§r (всё), §bground§r (с земли), §bchests§r (из сундука).");
                    return;
                }
                settings.setSortMode(mode);
                data.setDirty();
                reply(player, "§b[Сортировщик]§r Режим сортировки: §a" + mode);
            }
            case "find", "найти" -> {
                if (arg.isEmpty()) {
                    reply(player, "§b[Сортировщик]§r Укажите предмет для поиска в сундуках. Пример: §b!find алмаз");
                    return;
                }
                findInNearbyChests(player, arg);
            }

            // --- Воины ---
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
                if (normalized.isEmpty()) {
                    reply(player, "§c[Воин]§r Атакуют всех враждебных мобов.");
                } else {
                    reply(player, "§c[Воин]§r Приоритетная цель: §c" + normalized);
                }
            }

            // --- Общие ---
            case "status", "статус" -> sendStatus(player, server, settings);
            case "feed", "покормить" -> feedAllGolems(player, server);
            case "recall", "отозвать" -> recallByCommand(player, server, arg);

            default -> {
                reply(player, "§cНеизвестная команда: §f!" + cmd + "§c. Введите §b!help§c для списка всех команд.");
            }
        }
    }

    private static void sendHelp(ServerPlayer player) {
        player.displayClientMessage(Component.literal("§6§l=== Команды Manyl_mod v1.1 ==="), false);
        player.displayClientMessage(Component.literal("§e!help §7— показать этот список команд"), false);
        player.displayClientMessage(Component.literal("§6[Копатель] §b!extract <руда/блок|all> §7— добывать только указанную руду/блок (железо, алмаз, золото, уголь, медь...)"), false);
        player.displayClientMessage(Component.literal("§6[Копатель] §b!digmode <quarry|tunnel|ores> §7— режим (карьер/туннель/руды)"), false);
        player.displayClientMessage(Component.literal("§6[Копатель] §b!radius <2..10> §7| §b!depth <2..12> §7— радиус и глубина копки"), false);
        player.displayClientMessage(Component.literal("§a[Фермер]   §b!crop <культура|all> §7— собирать только нужную культуру (пшеница, морковь, картофель, свекла, нарост)"), false);
        player.displayClientMessage(Component.literal("§b[Сортир.]  §b!sort <all|ground|chests> §7— откуда брать вещи (всё/земля/сундук)"), false);
        player.displayClientMessage(Component.literal("§b[Сортир.]  §b!find <предмет> §7— найти предмет в сундуках рядом (радиус 16)"), false);
        player.displayClientMessage(Component.literal("§c[Воин]     §b!guard <follow|stay|patrol> §7— следовать, стоять или патрулировать"), false);
        player.displayClientMessage(Component.literal("§c[Воин]     §b!target <моб|all> §7— атаковать конкретных мобов (крипер, зомби, скелет, паук...)"), false);
        player.displayClientMessage(Component.literal("§d[Общее]    §b!status §7| §b!feed §7| §b!recall [all|digger|farmer|sorter|warrior]"), false);
    }

    private static void sendStatus(ServerPlayer player, MinecraftServer server, PlayerGolemSettings s) {
        List<BaseGolemEntity> golems = ManylSavedData.getPlayerGolems(server, player.getUUID());
        int[] counts = ManylSavedData.getRoleCounts(server, player.getUUID());
        reply(player, String.format("§6[Статус]§r Копатели: §e%d/4§r | Фермеры: §a%d/2§r | Сортир.: §b%d/6§r | Воины: §c%d/5",
                counts[0], counts[1], counts[2], counts[3]));
        reply(player, "§7Фильтры: !extract=" + emptyOr(s.getExtractFilter(), "все")
                + " | !crop=" + emptyOr(s.getCropFilter(), "все")
                + " | !sort=" + s.getSortMode()
                + " | !guard=" + s.getGuardMode()
                + " | !target=" + emptyOr(s.getTargetFilter(), "все"));
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
        reply(player, "§aПокормили прислужников из их запасов: §f" + fed);
    }

    private static void recallByCommand(ServerPlayer player, MinecraftServer server, String arg) {
        int roleOrdinal = switch (arg.toLowerCase(Locale.ROOT)) {
            case "digger", "копатель", "копатели", "шахтёр", "шахтер" -> GolemRole.DIGGER.ordinal();
            case "farmer", "фермер", "фермеры" -> GolemRole.FARMER.ordinal();
            case "sorter", "сортировщик", "сортировщики" -> GolemRole.SORTER.ordinal();
            case "warrior", "воин", "воины" -> GolemRole.WARRIOR.ordinal();
            default -> -1;
        };
        List<BaseGolemEntity> golems = ManylSavedData.getPlayerGolems(server, player.getUUID());
        int count = 0;
        for (BaseGolemEntity g : golems) {
            if (roleOrdinal >= 0 && g.getRole().ordinal() != roleOrdinal) continue;
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
        reply(player, "§dОтозвано прислужников: §f" + count);
    }

    private static void findInNearbyChests(ServerPlayer player, String query) {
        String normalized = normalizeItemQuery(query);
        BlockPos origin = player.blockPosition();
        List<String> hits = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-16, -6, -16), origin.offset(16, 6, 16))) {
            BlockEntity be = player.level().getBlockEntity(pos);
            if (!(be instanceof Container c)) continue;
            int total = 0;
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack s = c.getItem(i);
                if (s.isEmpty()) continue;
                String id = BuiltInRegistries.ITEM.getKey(s.getItem()).getPath();
                String name = s.getHoverName().getString().toLowerCase(Locale.ROOT);
                if (id.contains(normalized) || name.contains(query.toLowerCase(Locale.ROOT))) {
                    total += s.getCount();
                }
            }
            if (total > 0) {
                hits.add(String.format("§a%d шт.§r в (%d, %d, %d)", total, pos.getX(), pos.getY(), pos.getZ()));
            }
        }
        if (hits.isEmpty()) {
            reply(player, "§b[Сортировщик]§r Предмет «" + query + "» не найден в сундуках рядом.");
        } else {
            reply(player, "§b[Сортировщик]§r Найдено («" + query + "»): " + String.join("; ", hits.subList(0, Math.min(5, hits.size()))));
        }
    }

    private static String normalizeOreArg(String raw) {
        String s = raw.toLowerCase(Locale.ROOT).trim();
        return switch (s) {
            case "all", "reset", "clear", "все", "всё", "сброс" -> "";
            case "железо", "железная", "железная руда" -> "iron";
            case "алмаз", "алмазы", "алмазная", "алмазная руда" -> "diamond";
            case "золото", "золотая", "золотая руда" -> "gold";
            case "медь", "медная", "медная руда" -> "copper";
            case "уголь", "угольная", "угольная руда" -> "coal";
            case "изумруд", "изумруды", "изумрудная" -> "emerald";
            case "лазурит", "лазуритовая" -> "lapis";
            case "редстоун", "красная пыль", "красный камень" -> "redstone";
            case "кварц", "кварцевая" -> "quartz";
            case "незерит", "древние обломки", "обломки" -> "debris";
            case "руда", "руды" -> "ores";
            case "камень" -> "stone";
            case "сланец", "глубинный сланец" -> "deepslate";
            case "обсидиан" -> "obsidian";
            default -> s;
        };
    }

    private static String normalizeCropArg(String raw) {
        String s = raw.toLowerCase(Locale.ROOT).trim();
        return switch (s) {
            case "all", "reset", "все", "всё", "сброс" -> "";
            case "пшеница", "хлеб" -> "wheat";
            case "морковь", "морковка" -> "carrot";
            case "картофель", "картошка" -> "potato";
            case "свекла", "свёкла" -> "beetroot";
            case "нарост", "незер нарост", "адский нарост" -> "nether_wart";
            default -> s;
        };
    }

    private static String normalizeSortMode(String raw) {
        return switch (raw.toLowerCase(Locale.ROOT).trim()) {
            case "all", "все", "всё" -> "all";
            case "ground", "земля", "дроп" -> "ground";
            case "chests", "chest", "сундук", "сундуки" -> "chests";
            default -> null;
        };
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

    private static String normalizeItemQuery(String raw) {
        String ore = normalizeOreArg(raw);
        if (!ore.isEmpty() && !ore.equals(raw.toLowerCase(Locale.ROOT).trim())) return ore;
        String crop = normalizeCropArg(raw);
        if (!crop.isEmpty() && !crop.equals(raw.toLowerCase(Locale.ROOT).trim())) return crop;
        return raw.toLowerCase(Locale.ROOT).trim();
    }

    private static DigMode parseDigMode(String raw) {
        return switch (raw.toLowerCase(Locale.ROOT).trim()) {
            case "quarry", "карьер" -> DigMode.QUARRY;
            case "tunnel", "туннель", "тоннель" -> DigMode.TUNNEL;
            case "ores", "ore", "руда", "руды" -> DigMode.ORES_ONLY;
            default -> null;
        };
    }

    private static String emptyOr(String v, String fallback) {
        return (v == null || v.isEmpty()) ? fallback : v;
    }

    private static void reply(ServerPlayer player, String msg) {
        player.displayClientMessage(Component.literal(msg).withStyle(ChatFormatting.WHITE), false);
    }
}
