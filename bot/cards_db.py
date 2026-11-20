"""База карт: загрузка, поиск, фильтрация."""
import json
import os
from typing import List, Dict, Optional

DATA_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), "data", "cards.json")

class CardsDB:
    def __init__(self, path: str = DATA_PATH):
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)
        self.meta = data.get("meta", {})
        self.cards: List[Dict] = data.get("cards", [])
        self.by_id = {c["id"]: c for c in self.cards}
        self.by_name = {c["name_ru"].lower(): c for c in self.cards}

    def get(self, card_id: str) -> Optional[Dict]:
        return self.by_id.get(card_id)

    def search(self, query: str) -> List[Dict]:
        q = query.lower().strip()
        if not q:
            return self.cards
        return [c for c in self.cards if q in c["name_ru"].lower() or q in c["name_en"].lower() or q in c["id"]]

    def filter(self, rarity=None, ctype=None, elixir=None, evo=None) -> List[Dict]:
        res = self.cards
        if rarity:
            res = [c for c in res if c["rarity"] == rarity]
        if ctype:
            res = [c for c in res if c["type"] == ctype]
        if elixir is not None:
            res = [c for c in res if c["elixir"] == elixir]
        if evo is not None:
            res = [c for c in res if ("Эволюция" in c["rarity"]) == evo]
        return res

    def rarities(self):
        return sorted(set(c["rarity"] for c in self.cards))

    def types(self):
        return sorted(set(c["type"] for c in self.cards))

    def count(self):
        return len(self.cards)

# Синглтон для GUI
db = CardsDB()
