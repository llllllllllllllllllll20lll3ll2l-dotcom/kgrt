"""Зрение бота: анализирует кадр экрана Clash Royale.
Определяет: эликсир, таймер, руку карт, башни, войска (упрощённо).
"""
import cv2
import numpy as np
from dataclasses import dataclass, field
from typing import List, Optional, Dict

@dataclass
class GameState:
    elixir: float = 0.0          # 0..10
    elixir_max: float = 10.0
    time_left: int = 180         # секунды
    overtime: bool = False
    hand: List[str] = field(default_factory=list)   # id карт в руке (до 4)
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
    ZONES = {
        "elixir_bar": (0.08, 0.925, 0.92, 0.965),
        "timer": (0.40, 0.015, 0.60, 0.075),
        "hand": (0.05, 0.80, 0.95, 0.925),
        "enemy_towers": (0.05, 0.05, 0.95, 0.35),
        "my_towers": (0.05, 0.55, 0.95, 0.80),
        "arena": (0.05, 0.10, 0.95, 0.80),
    }

    def __init__(self):
        self.last_state = GameState()

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
        # Эликсир пропорционален ширине заполнения от левого края бара
        # Левый край бара ~ 5% ширины ROI, правый ~ 95%
        fill_ratio = (right - left) / bar_width
        # Калибровка: при полном баре fill_ratio ~0.85 -> 10 эликсира
        elixir = fill_ratio * 10 / 0.85
        # Альтернативная оценка по доле заполненных столбцов
        frac = float(np.mean(filled_cols))
        elixir2 = frac * 10
        # Берём среднее двух оценок для стабильности
        elixir = (elixir + elixir2) / 2
        return round(min(10.0, max(0.0, elixir)), 1)

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
        """Рука карт: 4 слота внизу. Пока возвращаем заглушку — для точного
        распознавания нужны шаблоны карт (см. tools/calibrate.py).
        Здесь: определяем, сколько слотов подсвечено (доступно по эликсиру)."""
        roi = self._crop(img, self.ZONES["hand"])
        # Эвристика: тёмные слоты = недоступны, светлые = доступны
        # Делим на 4 части
        if roi.size == 0:
            return []
        hh, ww = roi.shape[:2]
        hand = []
        for i in range(4):
            part = roi[:, int(ww*i/4):int(ww*(i+1)/4)]
            gray = cv2.cvtColor(part, cv2.COLOR_BGR2GRAY)
            bright = float(np.mean(gray))
            # Яркий слот = карта доступна
            hand.append(f"slot_{i}_{'ok' if bright > 90 else 'dim'}")
        return hand

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
        """Координаты центра слота карты (для тапа)."""
        # Рука: 4 слота по ширине
        x = int(screen_w * (0.15 + slot * 0.22))
        y = int(screen_h * 0.86)
        return x, y

    def arena_xy(self, side: str, screen_w: int, screen_h: int):
        """Координаты точки на арене: left/right/center, defense/attack."""
        positions = {
            "left_def": (0.30, 0.62), "right_def": (0.70, 0.62),
            "left_mid": (0.30, 0.50), "right_mid": (0.70, 0.50),
            "left_atk": (0.30, 0.38), "right_atk": (0.70, 0.38),
            "center": (0.50, 0.50), "king": (0.50, 0.30),
        }
        x, y = positions.get(side, (0.5, 0.5))
        return int(screen_w * x), int(screen_h * y)
