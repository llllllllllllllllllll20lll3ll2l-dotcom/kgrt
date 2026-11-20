"""Зрение бота: анализирует кадр экрана Clash Royale.
Определяет: эликсир, таймер, руку карт, башни, войска (упрощённо).
"""
import cv2
import numpy as np
from dataclasses import dataclass, field
from typing import List, Optional, Dict, Tuple

from . import screen_layout as L
from .card_recognizer import CardRecognizer

@dataclass
class GameState:
    elixir: float = 0.0          # 0..10
    elixir_max: float = 10.0
    time_left: int = 180         # секунды
    overtime: bool = False
    hand: List[str] = field(default_factory=list)   # id карт в руке (до 4)
    hand_scores: List[float] = field(default_factory=list)  # уверенность распознавания
    next_card: Optional[str] = None
    enemy_towers: Dict[str, int] = field(default_factory=dict)  # left, right, king
    my_towers: Dict[str, int] = field(default_factory=dict)
    threats: List[Dict] = field(default_factory=list)  # войска врага
    frame_ok: bool = False
    raw_frame: Optional[np.ndarray] = None

class Vision:
    """Анализатор экрана. Координаты — относительные (0..1) от размера экрана."""

    # Зоны экрана (x1, y1, x2, y2) в относительных координатах
    # Под 720x1604 (OPPO) и большинство телефонов 19:9 / 20:9
    ZONES = L.ZONES

    def __init__(self):
        self.last_state = GameState()
        self.recognizer = CardRecognizer()
        self._last_hand_scores: List[float] = []

    def analyze(self, png_bytes: Optional[bytes]) -> GameState:
        if not png_bytes:
            return GameState(frame_ok=False)
        arr = np.frombuffer(png_bytes, dtype=np.uint8)
        img = cv2.imdecode(arr, cv2.IMREAD_COLOR)
        if img is None:
            return GameState(frame_ok=False)
        h, w = img.shape[:2]
        state = GameState(frame_ok=True, raw_frame=img)

        state.elixir = self._detect_elixir(img, w, h)
        state.time_left, state.overtime = self._detect_timer(img, w, h)
        state.hand = self._detect_hand(img, w, h)
        state.hand_scores = list(getattr(self, "_last_hand_scores", []))
        state.enemy_towers, state.my_towers = self._detect_towers(img, w, h)
        state.threats = self._detect_threats(img, w, h)
        self.last_state = state
        return state

    def _crop(self, img, zone):
        x1, y1, x2, y2 = zone
        h, w = img.shape[:2]
        return img[int(h*y1):int(h*y2), int(w*x1):int(w*x2)]

    def _detect_elixir(self, img, w, h) -> float:
        """Эликсир — розово-фиолетовая полоса внизу, заполняется слева направо.
        Метод: по столбцам считаем, где есть эликсир, и меряем ширину заполнения.
        Устойчив к разным оттенкам (OPPO 720x1604 и др.)."""
        roi = self._crop(img, self.ZONES["elixir_bar"])
        if roi.size == 0:
            return 0.0
        hsv = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
        # Широкий диапазон: фиолетовый + розовый эликсир
        lower1 = np.array([120, 40, 40])
        upper1 = np.array([175, 255, 255])
        mask = cv2.inRange(hsv, lower1, upper1)
        # Закрываем мелкие дыры
        kernel = np.ones((3, 3), np.uint8)
        mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel, iterations=2)
        # По столбцам: заполненный столбец = много фиолетовых пикселей
        col_h = mask.shape[0]
        col_counts = np.sum(mask > 0, axis=0)  # (width,)
        filled_cols = col_counts > (col_h * 0.35)
        if not np.any(filled_cols):
            return 0.0
        # Находим левую и правую границы заполнения
        idx = np.where(filled_cols)[0]
        left, right = int(idx[0]), int(idx[-1])
        bar_width = max(1, mask.shape[1])
        # Доля заполненных столбцов от всей ширины бара = доля эликсира.
        # На реальном кадре 720x1604 полный бар даёт frac ~0.874 -> 10 эликсира.
        frac = float(np.mean(filled_cols))
        return round(min(10.0, max(0.0, frac * 10 / 0.874)), 1)

    def _detect_timer(self, img, w, h) -> tuple:
        """Таймер сверху. OCR если доступен, иначе эвристика."""
        roi = self._crop(img, self.ZONES["timer"])
        try:
            import pytesseract
            gray = cv2.cvtColor(roi, cv2.COLOR_BGR2GRAY)
            # Увеличиваем и бинаризуем для лучшего OCR
            gray = cv2.resize(gray, None, fx=3, fy=3, interpolation=cv2.INTER_CUBIC)
            _, gray = cv2.threshold(gray, 150, 255, cv2.THRESH_BINARY)
            text = pytesseract.image_to_string(gray, config="--psm 7 -c tessedit_char_whitelist=0123456789:")
            text = "".join(ch for ch in text if ch.isdigit())
            if len(text) >= 2:
                # Формат M:SS или SS
                if len(text) == 3:
                    m, s = int(text[0]), int(text[1:])
                    return m*60+s, False
                elif len(text) == 4:
                    m, s = int(text[:2]), int(text[2:])
                    return m*60+s, False
                elif len(text) <= 2:
                    return int(text), False
        except Exception:
            pass
        # Fallback: если таймер не распознан, возвращаем 180
        return 180, False

    def _detect_hand(self, img, w, h) -> List[str]:
        """Рука карт: 4 слота внизу. Каждый слот сравнивается с иконками-шаблонами
        из data/templates. Возвращает список из 4 card_id (None -> "unknown")."""
        hand: List[str] = []
        scores: List[float] = []
        for i in range(4):
            x1, y1, x2, y2 = L.hand_slot_box(i, w, h)
            slot_img = img[max(0, y1):min(h, y2), max(0, x1):min(w, x2)]
            card_id, score, _margin = self.recognizer.recognize(slot_img)
            hand.append(card_id or "unknown")
            scores.append(score)
        self._last_hand_scores = scores
        return hand

    def hand_slots_detail(self, img) -> List[Dict]:
        """Подробно по каждому слоту: bbox, точка клика, карта, уверенность.
        Используется для калибровки и отладки (tools/calibrate.py)."""
        h, w = img.shape[:2]
        out = []
        for i in range(4):
            x1, y1, x2, y2 = L.hand_slot_box(i, w, h)
            slot_img = img[max(0, y1):min(h, y2), max(0, x1):min(w, x2)]
            card_id, score, margin = self.recognizer.recognize(slot_img)
            out.append({
                "slot": i,
                "bbox": (x1, y1, x2, y2),
                "tap": L.hand_slot_tap(i, w, h),
                "card_id": card_id,
                "score": score,
                "margin": margin,
            })
        return out

    def _detect_towers(self, img, w, h):
        """Башни: ищем HP-полосы. Упрощённо — возвращаем значения по умолчанию,
        для точности нужен OCR HP (см. калибровку)."""
        # Заглушка: в реальном бое сюда подключается OCR чисел HP башен
        enemy = {"left": 2534, "right": 2534, "king": 4008}
        mine = {"left": 2534, "right": 2534, "king": 4008}
        return enemy, mine

    def _detect_threats(self, img, w, h) -> List[Dict]:
        """Угрозы: войска врага на арене. Упрощённо — цветовые кластеры.
        Для продакшена: шаблонное сопоставление спрайтов карт."""
        # Заглушка: в симуляции угрозы приходят из AI-модуля
        return []

    def card_slot_xy(self, slot: int, screen_w: int, screen_h: int):
        """Координаты центра слота карты в руке (для тапа).
        Замерено на 720x1604: центры карт x = 0.310 / 0.485 / 0.665 / 0.865, y = 0.872.
        Клик по (0.15 + slot*0.22) попадал в зону «След.» — карта не выбиралась."""
        return L.hand_slot_tap(slot, screen_w, screen_h)

    def arena_xy(self, side: str, screen_w: int, screen_h: int):
        """Координаты точки установки карты на арене (доли экрана из screen_layout)."""
        return L.arena_point(side, screen_w, screen_h)

    def slot_for_card(self, state: GameState, card_id: str) -> Optional[int]:
        """В каком слоте руки лежит нужная карта. None — карты нет в руке."""
        if not card_id or not state.hand:
            return None
        for i, cid in enumerate(state.hand):
            if cid == card_id:
                return i
        return None
