"""Обучение бота: симуляция боёв + подбор весов (hill climbing).
Полное обучение на правилах: бот играет тысячи симулированных боёв
против случайных колод и улучшает решения.
"""
import random
import json
import os
import time
from dataclasses import dataclass
from typing import Dict, List
from .ai_brain import AIBrain, TACTICS
from .vision import GameState
from .deck import USER_DECK

WEIGHTS_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), "data", "trained_weights.json")

@dataclass
class BattleResult:
    win: bool
    crowns: int
    enemy_crowns: int
    decisions: int
    elixir_spent: float

# Пулы вражеских колод для симуляции
ENEMY_DECKS = [
    ["giant", "musketeer", "bomber", "skeletons", "arrows", "mini_pekka", "cannon", "fireball"],
    ["hog_rider", "musketeer", "skeletons", "ice_spirit", "cannon", "fireball", "ice_golem", "zap"],
    ["pekka", "wizard", "baby_dragon", "minions", "arrows", "mini_pekka", "tombstone", "fireball"],
    ["golem", "night_witch", "baby_dragon", "minion_horde", "poison", "mini_pekka", "tombstone", "zap"],
    ["balloon", "baby_dragon", "minions", "skeletons", "arrows", "musketeer", "tesla", "fireball"],
    ["sparky", "mini_pekka", "skeletons", "ice_spirit", "cannon", "fireball", "electro_wizard", "zap"],
    ["x_bow", "archers", "skeletons", "ice_spirit", "tesla", "fireball", "log", "ice_golem"],
    ["mortar", "musketeer", "minions", "skeletons", "arrows", "mini_pekka", "cannon", "fireball"],
    ["mega_knight", "wizard", "minions", "skeletons", "arrows", "mini_pekka", "tesla", "fireball"],
    ["electro_giant", "electro_wizard", "baby_dragon", "minions", "zap", "mini_pekka", "tombstone", "fireball"],
]

THREAT_POOL = [
    {"card_id": "giant", "name": "Гигант", "danger": 5, "side": "left"},
    {"card_id": "golem", "name": "Голем", "danger": 8, "side": "right"},
    {"card_id": "pekka", "name": "ПЕККА", "danger": 7, "side": "left"},
    {"card_id": "hog_rider", "name": "Всадник на кабане", "danger": 6, "side": "right"},
    {"card_id": "balloon", "name": "Воздушный шар", "danger": 7, "side": "left"},
    {"card_id": "baby_dragon", "name": "Дракончик", "danger": 5, "side": "right"},
    {"card_id": "musketeer", "name": "Мушкетёр", "danger": 4, "side": "left"},
    {"card_id": "wizard", "name": "Волшебник", "danger": 5, "side": "right"},
    {"card_id": "witch", "name": "Ведьма", "danger": 5, "side": "left"},
    {"card_id": "valkyrie", "name": "Валькирия", "danger": 5, "side": "right"},
    {"card_id": "skeleton_army", "name": "Скелетная армия", "danger": 6, "side": "left"},
    {"card_id": "minion_horde", "name": "Орда миньонов", "danger": 6, "side": "right"},
    {"card_id": "sparky", "name": "Спарки", "danger": 8, "side": "left"},
    {"card_id": "mega_knight", "name": "Мегарыцарь", "danger": 7, "side": "right"},
    {"card_id": "electro_giant", "name": "Электрогигант", "danger": 7, "side": "left"},
    {"card_id": "royal_giant", "name": "Королевский гигант", "danger": 6, "side": "right"},
    {"card_id": "x_bow", "name": "X-лук", "danger": 6, "side": "left"},
    {"card_id": "mortar", "name": "Мортира", "danger": 6, "side": "right"},
    {"card_id": "inferno_tower", "name": "Адская башня", "danger": 6, "side": "left"},
    {"card_id": "princess", "name": "Принцесса", "danger": 3, "side": "right"},
]

class Trainer:
    """Полное обучение: симуляция + hill climbing по весам."""

    def __init__(self, tactic: str = "balanced"):
        self.tactic = tactic
        self.brain = AIBrain(tactic=tactic)
        self.history: List[Dict] = []
        self.best_weights = dict(self.brain.weights)
        self.best_score = 0.0

    def simulate_battle(self, brain: AIBrain = None) -> BattleResult:
        brain = brain or self.brain
        elixir = 5.0
        decisions = 0
        elixir_spent = 0.0
        my_crowns = 0
        enemy_crowns = 0
        # Бой 3 минуты, шаг 2 сек = 90 шагов
        for step in range(90):
            elixir = min(10.0, elixir + 0.056)  # ~2.8/мин
            # Случайная угроза от врага
            threats = []
            if random.random() < 0.35:
                threats.append(random.choice(THREAT_POOL))
            state = GameState(elixir=elixir, threats=threats)
            d = brain.decide(state, threats)
            if d.card_id:
                decisions += 1
                cost = next((c.elixir for c in USER_DECK if c.id == d.card_id), 3)
                elixir -= cost
                elixir_spent += cost
                # Оценка: защита от сильной угрозы = +шанс короны, атака = +шанс
                if "Защита" in d.reason:
                    if threats and threats[0]["danger"] >= 6:
                        my_crowns += 0.3
                    else:
                        my_crowns += 0.1
                elif "Агрессия" in d.reason or "атака" in d.reason.lower():
                    my_crowns += random.uniform(0.1, 0.5)
                elif "Цикл" in d.reason:
                    my_crowns += random.uniform(0.05, 0.2)
            # Враг тоже давит (умеренно, чтобы бой был сбалансированным)
            if random.random() < 0.12:
                enemy_crowns += random.uniform(0.02, 0.12)
            # Если не защитились от сильной угрозы — враг берёт корону
            if threats and threats[0]["danger"] >= 7 and random.random() < 0.25:
                enemy_crowns += 0.25
            # Кап корон (макс 3 в реальном бое)
            my_crowns = min(3.0, my_crowns)
            enemy_crowns = min(3.0, enemy_crowns)
        win = my_crowns > enemy_crowns
        return BattleResult(
            win=win,
            crowns=int(round(my_crowns)),
            enemy_crowns=int(round(enemy_crowns)),
            decisions=decisions,
            elixir_spent=round(elixir_spent, 1),
        )

    def evaluate(self, brain: AIBrain, n: int = 200) -> float:
        """Средний скор за n боёв."""
        wins = 0
        total_crowns = 0
        for _ in range(n):
            r = self.simulate_battle(brain)
            if r.win:
                wins += 1
            total_crowns += r.crowns - r.enemy_crowns
        return wins / n * 100 + total_crowns * 5

    def train(self, iterations: int = 30, battles_per_iter: int = 200, progress_cb=None) -> Dict:
        """Hill climbing: пробуем менять веса, оставляем лучшие."""
        print(f"Обучение: тактика={self.tactic}, итераций={iterations}")
        current = AIBrain(tactic=self.tactic, weights=dict(self.best_weights))
        self.best_score = self.evaluate(current, battles_per_iter)
        print(f"  Стартовая оценка: {self.best_score:.1f}")
        for it in range(iterations):
            candidate_weights = dict(self.best_weights)
            # Мутация: меняем 1-2 веса
            keys = list(candidate_weights.keys())
            for _ in range(random.randint(1, 2)):
                k = random.choice(keys)
                candidate_weights[k] = round(max(0.1, min(2.0, candidate_weights[k] + random.uniform(-0.3, 0.3))), 2)
            candidate = AIBrain(tactic=self.tactic, weights=candidate_weights)
            score = self.evaluate(candidate, battles_per_iter)
            improved = score > self.best_score
            if improved:
                self.best_score = score
                self.best_weights = candidate_weights
                self.brain.weights = candidate_weights
            rec = {"iter": it+1, "score": round(score, 1), "best": round(self.best_score, 1),
                   "improved": improved, "weights": candidate_weights}
            self.history.append(rec)
            print(f"  Итерация {it+1}/{iterations}: скор={score:.1f} лучший={self.best_score:.1f} {'✓' if improved else ''}")
            if progress_cb:
                progress_cb(it+1, iterations, score, self.best_score)
        self.save()
        return {"best_score": self.best_score, "weights": self.best_weights, "history": self.history}

    def train_all_tactics(self, iterations: int = 15, battles_per_iter: int = 150, progress_cb=None) -> Dict:
        """Обучить все тактики и выбрать лучшую."""
        results = {}
        for tactic in TACTICS:
            print(f"\n=== Обучение тактики: {TACTICS[tactic]['name']} ===")
            t = Trainer(tactic=tactic)
            res = t.train(iterations=iterations, battles_per_iter=battles_per_iter, progress_cb=progress_cb)
            results[tactic] = res
        best = max(results, key=lambda k: results[k]["best_score"])
        print(f"\nЛучшая тактика: {TACTICS[best]['name']} ({results[best]['best_score']:.1f})")
        # Сохраняем лучшую
        self.tactic = best
        self.best_weights = results[best]["weights"]
        self.best_score = results[best]["best_score"]
        self.save()
        return {"results": results, "best_tactic": best}

    def save(self):
        data = {"tactic": self.tactic, "weights": self.best_weights, "best_score": self.best_score,
                "history": self.history[-20:]}
        with open(WEIGHTS_PATH, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)

    def load(self):
        if os.path.exists(WEIGHTS_PATH):
            with open(WEIGHTS_PATH, "r", encoding="utf-8") as f:
                data = json.load(f)
            self.tactic = data.get("tactic", self.tactic)
            self.best_weights = data.get("weights", self.best_weights)
            self.best_score = data.get("best_score", 0)
            self.brain = AIBrain(tactic=self.tactic, weights=self.best_weights)
            return True
        return False
