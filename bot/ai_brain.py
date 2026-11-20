"""Мозг бота: правила + тактики. Обучается через trainer.py (подбор весов)."""
import random
from dataclasses import dataclass
from typing import List, Optional, Dict
from .deck import USER_DECK, COUNTERS
from .vision import GameState

TACTICS = {
    "aggressive": {"name": "Агрессивная", "desc": "Давит сразу, ставит танки за мостом, не ждёт эликсир", "elixir_threshold": 7, "push_when": "always", "defense_first": False},
    "defensive": {"name": "Оборонительная", "desc": "Ждёт атаки врага, отбивается и контратакует", "elixir_threshold": 9, "push_when": "counter", "defense_first": True},
    "balanced": {"name": "Сбалансированная", "desc": "Следит за эликсиром врага, играет по ситуации", "elixir_threshold": 8, "push_when": "opportunity", "defense_first": True},
    "cycle": {"name": "Цикл", "desc": "Быстрый цикл дешёвых карт, чип-урон Принцессой и Бревном", "elixir_threshold": 5, "push_when": "cycle", "defense_first": False},
    "counter": {"name": "Контр-пуш", "desc": "Отбивает атаку и сразу идёт в контратаку выжившими войсками", "elixir_threshold": 8, "push_when": "counter", "defense_first": True},
}

@dataclass
class Decision:
    card_id: Optional[str]
    card_name: str
    position: str  # left_def, right_def, left_mid, right_mid, left_atk, right_atk, center
    reason: str
    confidence: float = 0.8

class AIBrain:
    def __init__(self, tactic: str = "balanced", weights: Optional[Dict] = None):
        self.tactic = tactic
        self.weights = weights or {
            "defense": 1.0, "attack": 1.0, "cycle": 1.0,
            "elixir_save": 0.5, "counter_push": 1.0,
        }
        self.hand_ids = [c.id for c in USER_DECK]
        self.deck_by_id = {c.id: c for c in USER_DECK}

    def set_tactic(self, tactic: str):
        if tactic in TACTICS:
            self.tactic = tactic

    def decide(self, state: GameState, threats: Optional[List[Dict]] = None) -> Decision:
        """Главное решение: какую карту и куда ставить."""
        threats = threats or state.threats or []
        elixir = state.elixir
        cfg = TACTICS.get(self.tactic, TACTICS["balanced"])

        # 1. Защита: если есть угроза — ставим контр
        if threats and cfg["defense_first"]:
            d = self._defense_decision(threats, elixir, state)
            if d:
                return d

        # 2. По тактике
        if self.tactic == "aggressive":
            return self._aggressive(state, elixir)
        elif self.tactic == "defensive":
            return self._defensive(state, elixir, threats)
        elif self.tactic == "cycle":
            return self._cycle(state, elixir)
        elif self.tactic == "counter":
            return self._counter(state, elixir, threats)
        else:
            return self._balanced(state, elixir, threats)

    def _affordable(self, elixir: float, state: Optional[GameState] = None) -> List[str]:
        """Карты колоды, которые сейчас можно поставить.
        Если рука распознана — берём только карты из руки (иначе клик промахнётся)."""
        candidates = self.hand_ids
        if state is not None and getattr(state, "hand", None):
            known = [cid for cid in state.hand if cid and cid != "unknown"]
            if known:
                # В руке иконка эволюции может отличаться от id в колоде
                in_hand = []
                for cid in self.hand_ids:
                    variants = {cid}
                    if cid.endswith("_evo"):
                        variants.add(cid[:-4])
                    else:
                        variants.add(cid + "_evo")
                    if variants & set(known):
                        in_hand.append(cid)
                if in_hand:
                    candidates = in_hand
        return [cid for cid in candidates if self.deck_by_id[cid].elixir <= elixir]

    def _defense_decision(self, threats, elixir, state=None) -> Optional[Decision]:
        # Берём самую опасную угрозу
        threat = max(threats, key=lambda t: t.get("danger", 1))
        counters = COUNTERS.get(threat.get("card_id", ""), [])
        affordable = self._affordable(elixir, state)
        for cid in counters:
            if cid in affordable:
                c = self.deck_by_id[cid]
                # Позиция: между угрозой и башней
                side = threat.get("side", "left")
                pos = f"{side}_def"
                return Decision(cid, c.name_ru, pos,
                                f"Защита от {threat.get('name', 'угрозы')} картой {c.name_ru}", 0.9)
        return None

    def _aggressive(self, state, elixir) -> Decision:
        # Ставим танк за мостом или поддержку
        affordable = self._affordable(elixir, state)
        # Приоритет: Гигантский миньон -> Ведьма -> Дровосек
        for cid in ["giant_minion", "witch_evo", "lumberjack", "princess"]:
            if cid in affordable:
                c = self.deck_by_id[cid]
                side = random.choice(["left", "right"])
                return Decision(cid, c.name_ru, f"{side}_mid",
                                f"Агрессия: {c.name_ru} за мостом", 0.85)
        return self._wait(state, elixir, "Агрессия ждёт эликсир")

    def _defensive(self, state, elixir, threats) -> Decision:
        if threats:
            d = self._defense_decision(threats, elixir, state)
            if d:
                return d
        if elixir >= 9:
            # Контратака выжившими
            affordable = self._affordable(elixir, state)
            for cid in ["giant_minion", "witch_evo"]:
                if cid in affordable:
                    c = self.deck_by_id[cid]
                    return Decision(cid, c.name_ru, "left_mid",
                                    "Контратака после обороны", 0.8)
        return self._wait(state, elixir, "Оборонительная тактика: ждём атаку врага")

    def _cycle(self, state, elixir) -> Decision:
        # Дешёвые карты + чип Принцессой
        affordable = self._affordable(elixir, state)
        # Принцесса для чип-урона по башне
        if "princess" in affordable and elixir >= 6:
            return Decision("princess", "Принцесса", "center",
                            "Цикл: чип-урон Принцессой", 0.75)
        for cid in ["skeletons_evo", "bats", "log", "lumberjack"]:
            if cid in affordable:
                c = self.deck_by_id[cid]
                return Decision(cid, c.name_ru, "center",
                                f"Цикл: {c.name_ru}", 0.7)
        return self._wait(state, elixir, "Цикл ждёт эликсир")

    def _counter(self, state, elixir, threats) -> Decision:
        if threats:
            d = self._defense_decision(threats, elixir, state)
            if d:
                return d
        if elixir >= 8:
            affordable = self._affordable(elixir, state)
            for cid in ["witch_evo", "giant_minion"]:
                if cid in affordable:
                    c = self.deck_by_id[cid]
                    return Decision(cid, c.name_ru, "left_mid",
                                    "Контр-пуш: поддержка за выжившими", 0.85)
        return self._wait(state, elixir, "Контр-пуш ждёт атаку врага")

    def _balanced(self, state, elixir, threats) -> Decision:
        if threats:
            d = self._defense_decision(threats, elixir, state)
            if d:
                return d
        if elixir >= 8:
            affordable = self._affordable(elixir, state)
            for cid in ["giant_minion", "witch_evo", "princess"]:
                if cid in affordable:
                    c = self.deck_by_id[cid]
                    side = random.choice(["left", "right"])
                    return Decision(cid, c.name_ru, f"{side}_mid",
                                    f"Сбалансированная атака: {c.name_ru}", 0.8)
        if elixir >= 5:
            affordable = self._affordable(elixir, state)
            for cid in ["lumberjack", "bats", "skeletons_evo"]:
                if cid in affordable:
                    c = self.deck_by_id[cid]
                    return Decision(cid, c.name_ru, "center",
                                    f"Поддержка: {c.name_ru}", 0.7)
        return self._wait(state, elixir, "Сбалансированная: копим эликсир")

    def _wait(self, state, elixir, reason) -> Decision:
        return Decision(None, "—", "center", f"{reason} (эликсир {elixir:.1f})", 0.5)

    def explain_tactic(self) -> str:
        cfg = TACTICS.get(self.tactic, {})
        return f"{cfg.get('name','')} — {cfg.get('desc','')}"
