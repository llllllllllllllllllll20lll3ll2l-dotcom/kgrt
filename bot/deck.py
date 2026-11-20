"""Колода пользователя."""
from dataclasses import dataclass, field
from typing import List

@dataclass
class DeckCard:
    id: str
    name_ru: str
    elixir: int
    role: str  # tank, support, defense, cycle, spell

# Колода пользователя из ответов:
# Эволюция ведьмы, героическое надгробье, эволюция скелетов, дровосек,
# миньон гигант, принцесса, летучие мыши, бревно
USER_DECK = [
    DeckCard("witch_evo", "Ведьма (Эволюция)", 5, "support"),
    DeckCard("hero_tombstone", "Надгробье героя", 3, "defense"),
    DeckCard("skeletons_evo", "Скелеты (Эволюция)", 1, "cycle"),
    DeckCard("lumberjack", "Дровосек", 4, "cycle"),
    DeckCard("giant_minion", "Гигантский миньон", 4, "tank"),
    DeckCard("princess", "Принцесса", 3, "support"),
    DeckCard("bats", "Летучие мыши", 2, "cycle"),
    DeckCard("log", "Бревно", 2, "spell"),
]

DECK_NAMES = [c.name_ru for c in USER_DECK]
AVG_ELIXIR = sum(c.elixir for c in USER_DECK) / len(USER_DECK)

# Контр-таблица: чем бить какие угрозы
COUNTERS = {
    "giant": ["giant_minion", "hero_tombstone", "skeletons_evo"],
    "golem": ["giant_minion", "hero_tombstone", "bats"],
    "pekka": ["hero_tombstone", "skeletons_evo", "bats"],
    "hog_rider": ["hero_tombstone", "skeletons_evo", "giant_minion"],
    "balloon": ["princess", "bats", "giant_minion"],
    "baby_dragon": ["princess", "bats", "giant_minion"],
    "musketeer": ["log", "lumberjack", "bats"],
    "wizard": ["log", "lumberjack", "bats"],
    "witch": ["log", "princess", "lumberjack"],
    "valkyrie": ["princess", "bats", "giant_minion"],
    "skeleton_army": ["log", "princess", "lumberjack"],
    "minion_horde": ["log", "princess", "lumberjack"],
    "sparky": ["hero_tombstone", "skeletons_evo", "bats"],
    "mega_knight": ["hero_tombstone", "bats", "giant_minion"],
    "electro_giant": ["hero_tombstone", "bats", "giant_minion"],
    "royal_giant": ["hero_tombstone", "giant_minion", "bats"],
    "x_bow": ["lumberjack", "giant_minion", "bats"],
    "mortar": ["lumberjack", "giant_minion", "bats"],
    "inferno_tower": ["skeletons_evo", "bats", "lumberjack"],
    "tesla": ["lumberjack", "giant_minion", "bats"],
    "cannon": ["lumberjack", "giant_minion", "bats"],
    "princess": ["log", "lumberjack", "bats"],
    "archers": ["log", "princess", "lumberjack"],
    "goblins": ["log", "princess", "lumberjack"],
    "skeletons": ["log", "princess", "lumberjack"],
    "barbarians": ["princess", "lumberjack", "giant_minion"],
    "knight": ["princess", "bats", "giant_minion"],
    "minipekka": ["hero_tombstone", "skeletons_evo", "bats"],
    "prince": ["hero_tombstone", "skeletons_evo", "bats"],
    "dark_prince": ["hero_tombstone", "skeletons_evo", "bats"],
    "battle_ram": ["hero_tombstone", "skeletons_evo", "giant_minion"],
    "executioner": ["log", "bats", "giant_minion"],
    "hunter": ["log", "bats", "giant_minion"],
    "electro_wizard": ["log", "lumberjack", "bats"],
    "electro_dragon": ["princess", "bats", "giant_minion"],
    "inferno_dragon": ["princess", "bats", "giant_minion"],
    "mega_minion": ["princess", "bats", "log"],
    "minions": ["log", "princess", "lumberjack"],
    "goblin_gang": ["log", "princess", "lumberjack"],
    "spear_goblins": ["log", "princess", "lumberjack"],
    "bomber": ["log", "lumberjack", "bats"],
    "ice_golem": ["princess", "bats", "giant_minion"],
    "ice_spirit": ["log", "princess", "lumberjack"],
    "electro_spirit": ["log", "princess", "lumberjack"],
    "golem_night": ["log", "princess", "lumberjack"],
    "goblin_machine": ["hero_tombstone", "bats", "giant_minion"],
    "goblin_giant": ["hero_tombstone", "bats", "giant_minion"],
    "goblin_drill": ["hero_tombstone", "bats", "giant_minion"],
    "goblin_cage": ["log", "princess", "lumberjack"],
    "royal_recruit": ["princess", "lumberjack", "giant_minion"],
    "royal_hog": ["hero_tombstone", "skeletons_evo", "giant_minion"],
    "giant_skeleton": ["hero_tombstone", "bats", "giant_minion"],
    "furnace": ["log", "lumberjack", "bats"],
    "barbarian_hut": ["log", "lumberjack", "bats"],
    "goblin_hut": ["log", "lumberjack", "bats"],
    "bomb_tower": ["lumberjack", "giant_minion", "bats"],
    "tombstone": ["lumberjack", "giant_minion", "bats"],
}
