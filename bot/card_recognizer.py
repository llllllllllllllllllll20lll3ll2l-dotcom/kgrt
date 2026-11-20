"""Распознавание карт в руке по иконкам-шаблонам.

Шаблоны лежат в data/templates/<card_id>.png — иконки карт, снятые с экрана телефона.

Метод — двухэтапный, ради скорости (важна каждая миллисекунда в цикле бота):
1. Быстрый проход: все шаблоны в одном «типовом» масштабе, по серому изображению.
2. Уточнение: только 3 лучших карты перебираются по масштабам.

Результат на реальном кадре 720x1604: 4 из 4 слотов верно, отрыв 0.16-0.34,
время ~57 мс на всю руку (было ~1290 мс при полном переборе в цвете).
"""
import os
import glob
from typing import Dict, List, Optional, Tuple

import cv2
import numpy as np

TEMPLATES_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "data", "templates"
)

# Масштаб, в котором иконка карты совпадает с картой на экране (замерено ~2.4)
COARSE_SCALE = 2.4
# Масштабы для уточнения лучших кандидатов
FINE_SCALES = tuple(round(x, 2) for x in np.arange(2.05, 2.81, 0.1))
# Сколько лучших кандидатов уточнять
REFINE_TOP = 3
# Минимальное совпадение, чтобы считать карту распознанной
MIN_SCORE = 0.28
# Минимальный отрыв от второй по схожести карты
MIN_MARGIN = 0.04


def _to_gray(img: np.ndarray) -> np.ndarray:
    if img.ndim == 3:
        return cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    return img


class CardRecognizer:
    """Определяет, какая карта нарисована в слоте руки."""

    def __init__(self, templates_dir: str = TEMPLATES_DIR):
        self.templates_dir = templates_dir
        self.cards: Dict[str, np.ndarray] = {}
        self.coarse: Dict[str, np.ndarray] = {}
        self.fine: Dict[str, List[np.ndarray]] = {}
        self.load()

    def load(self) -> int:
        self.cards = {}
        self.coarse = {}
        self.fine = {}
        if not os.path.isdir(self.templates_dir):
            return 0
        for path in sorted(glob.glob(os.path.join(self.templates_dir, "*.png"))):
            card_id = os.path.splitext(os.path.basename(path))[0]
            img = cv2.imread(path, cv2.IMREAD_COLOR)
            if img is None or img.size == 0:
                continue
            gray = _to_gray(img)
            self.cards[card_id] = img
            self.coarse[card_id] = self._resize(gray, COARSE_SCALE)
            self.fine[card_id] = [self._resize(gray, s) for s in FINE_SCALES]
        return len(self.cards)

    @staticmethod
    def _resize(template: np.ndarray, scale: float) -> np.ndarray:
        th = max(8, int(template.shape[0] * scale))
        tw = max(8, int(template.shape[1] * scale))
        return cv2.resize(template, (tw, th), interpolation=cv2.INTER_AREA)

    @staticmethod
    def _match(region: np.ndarray, template: np.ndarray) -> float:
        if template.shape[0] >= region.shape[0] or template.shape[1] >= region.shape[1]:
            return -1.0
        try:
            return float(cv2.matchTemplate(region, template, cv2.TM_CCOEFF_NORMED).max())
        except cv2.error:
            return -1.0

    def available(self) -> List[str]:
        return sorted(self.cards.keys())

    def recognize(self, slot_img: np.ndarray) -> Tuple[Optional[str], float, float]:
        """Возвращает (card_id | None, score, margin)."""
        if slot_img is None or slot_img.size == 0 or not self.cards:
            return None, 0.0, 0.0
        if min(slot_img.shape[:2]) < 24:
            return None, 0.0, 0.0

        region = _to_gray(slot_img)

        # Этап 1: быстрый проход в одном масштабе
        rough = []
        for card_id, tpl in self.coarse.items():
            rough.append((self._match(region, tpl), card_id))
        rough.sort(reverse=True)

        # Этап 2: уточняем только лучших
        refined = []
        for score, card_id in rough[:REFINE_TOP]:
            best = score
            for tpl in self.fine[card_id]:
                best = max(best, self._match(region, tpl))
            refined.append((best, card_id))
        # Остальные оставляем как были — отрыв считается честно
        refined.extend(rough[REFINE_TOP:])
        refined.sort(reverse=True)

        best_score, best_id = refined[0]
        second = refined[1][0] if len(refined) > 1 else 0.0
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
