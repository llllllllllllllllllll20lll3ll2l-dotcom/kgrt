"""Распознавание карт в руке по иконкам-шаблонам.

Шаблоны лежат в data/templates/<card_id>.png — иконки карт, снятые с экрана телефона.

Метод: много-масштабное сопоставление шаблонов (cv2.matchTemplate TM_CCOEFF_NORMED).
На экране карта в руке примерно в 2.4 раза крупнее иконки-шаблона, поэтому
масштаб перебирается. Проверено на реальном кадре 720x1604: 4 из 4 слотов,
отрыв лучшего совпадения от второго 0.13-0.41.
"""
import os
import glob
from typing import Dict, List, Optional, Tuple

import cv2
import numpy as np

TEMPLATES_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "data", "templates"
)

# Перебираемые масштабы шаблона (иконка -> размер карты на экране)
SCALES = tuple(round(x, 2) for x in np.arange(1.9, 2.85, 0.05))
# Минимальное совпадение, чтобы считать карту распознанной
MIN_SCORE = 0.28
# Минимальный отрыв от второй по схожести карты
MIN_MARGIN = 0.05


class CardRecognizer:
    """Определяет, какая карта нарисована в слоте руки."""

    def __init__(self, templates_dir: str = TEMPLATES_DIR):
        self.templates_dir = templates_dir
        self.cards: Dict[str, np.ndarray] = {}
        self.load()

    def load(self) -> int:
        self.cards = {}
        if not os.path.isdir(self.templates_dir):
            return 0
        for path in sorted(glob.glob(os.path.join(self.templates_dir, "*.png"))):
            card_id = os.path.splitext(os.path.basename(path))[0]
            img = cv2.imread(path, cv2.IMREAD_COLOR)
            if img is None or img.size == 0:
                continue
            self.cards[card_id] = img
        return len(self.cards)

    def available(self) -> List[str]:
        return sorted(self.cards.keys())

    def _score(self, region: np.ndarray, template: np.ndarray) -> Tuple[float, float]:
        """Лучшее совпадение шаблона с областью и его масштаб."""
        best_score, best_scale = -1.0, 0.0
        rh, rw = region.shape[:2]
        th0, tw0 = template.shape[:2]
        for scale in SCALES:
            th, tw = int(th0 * scale), int(tw0 * scale)
            if th >= rh or tw >= rw or th < 8 or tw < 8:
                continue
            resized = cv2.resize(template, (tw, th), interpolation=cv2.INTER_AREA)
            result = cv2.matchTemplate(region, resized, cv2.TM_CCOEFF_NORMED)
            score = float(result.max())
            if score > best_score:
                best_score, best_scale = score, scale
        return best_score, best_scale

    def recognize(self, slot_img: np.ndarray) -> Tuple[Optional[str], float, float]:
        """Возвращает (card_id | None, score, margin)."""
        if slot_img is None or slot_img.size == 0 or not self.cards:
            return None, 0.0, 0.0
        if min(slot_img.shape[:2]) < 24:
            return None, 0.0, 0.0
        scores = []
        for card_id, template in self.cards.items():
            score, scale = self._score(slot_img, template)
            scores.append((score, card_id, scale))
        scores.sort(reverse=True)
        best_score, best_id, _ = scores[0]
        second = scores[1][0] if len(scores) > 1 else 0.0
        margin = best_score - second
        if best_score < MIN_SCORE or margin < MIN_MARGIN:
            return None, round(best_score, 3), round(margin, 3)
        return best_id, round(best_score, 3), round(margin, 3)

    def annotate(self, img: np.ndarray, slots) -> np.ndarray:
        """Рисует на кадре слоты и распознанные карты (для калибровки)."""
        out = img.copy()
        for item in slots:
            x1, y1, x2, y2 = item["bbox"]
            card_id = item.get("card_id")
            score = item.get("score", 0.0)
            color = (0, 220, 0) if card_id else (0, 0, 255)
            cv2.rectangle(out, (x1, y1), (x2, y2), color, 2)
            label = f"{item['slot']}: {card_id or '?'} {score:.2f}"
            cv2.putText(out, label, (x1, max(14, y1 - 8)),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 1, cv2.LINE_AA)
        return out
