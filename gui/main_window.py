"""Главное окно бота (Tkinter, русский интерфейс).
Вкладки: Бот | Карты | Обучение | Настройки
"""
import tkinter as tk
from tkinter import ttk, messagebox, filedialog
import threading
import time
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

from bot.cards_db import db
from bot.deck import USER_DECK, AVG_ELIXIR
from bot.ai_brain import AIBrain, TACTICS
from bot.trainer import Trainer
from bot.adb_controller import ADBController
from bot.vision import Vision

class BotApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Clash Royale Bot — KGRT (Windows + ADB)")
        self.geometry("1100x720")
        self.minsize(900, 600)
        self.adb = ADBController()
        self.vision = Vision()
        self.brain = AIBrain(tactic="balanced")
        self.trainer = Trainer(tactic="balanced")
        self.trainer.load()
        self.brain = self.trainer.brain
        self.running = False
        self._build_ui()

    def _build_ui(self):
        # Шапка
        header = ttk.Frame(self, padding=8)
        header.pack(fill=tk.X)
        ttk.Label(header, text="⚔️ Clash Royale Bot — KGRT", font=("Segoe UI", 16, "bold")).pack(side=tk.LEFT)
        ttk.Label(header, text=f"Колода: {len(USER_DECK)} карт • Ср. эликсир {AVG_ELIXIR:.1f} • Карт в базе: {db.count()}",
                  foreground="gray").pack(side=tk.RIGHT)

        self.notebook = ttk.Notebook(self)
        self.notebook.pack(fill=tk.BOTH, expand=True, padx=8, pady=4)

        self.tab_bot = ttk.Frame(self.notebook)
        self.tab_cards = ttk.Frame(self.notebook)
        self.tab_train = ttk.Frame(self.notebook)
        self.tab_settings = ttk.Frame(self.notebook)
        self.notebook.add(self.tab_bot, text="🤖 Бот")
        self.notebook.add(self.tab_cards, text="🃏 Все карты")
        self.notebook.add(self.tab_train, text="🎓 Обучение")
        self.notebook.add(self.tab_settings, text="⚙️ Настройки")

        self._build_bot_tab()
        self._build_cards_tab()
        self._build_train_tab()
        self._build_settings_tab()

    # ---------- Вкладка БОТ ----------
    def _build_bot_tab(self):
        left = ttk.Frame(self.tab_bot, padding=8)
        left.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        right = ttk.Frame(self.tab_bot, padding=8, width=340)
        right.pack(side=tk.RIGHT, fill=tk.Y)
        right.pack_propagate(False)

        ttk.Label(left, text="Экран телефона (ADB screencap)", font=("Segoe UI", 11, "bold")).pack(anchor=tk.W)
        self.preview = tk.Canvas(left, bg="#1a1a2e", height=420)
        self.preview.pack(fill=tk.BOTH, expand=True, pady=4)
        self.preview.create_text(200, 200, text="Нажмите «Подключить»\nи запустите бой в Clash Royale",
                                 fill="white", font=("Segoe UI", 12), justify=tk.CENTER)

        # Статус
        self.status_var = tk.StringVar(value="Не подключено")
        ttk.Label(left, textvariable=self.status_var, foreground="blue").pack(anchor=tk.W)

        # Панель управления
        ctrl = ttk.Frame(right)
        ctrl.pack(fill=tk.X)
        self.btn_connect = ttk.Button(ctrl, text="🔌 Подключить ADB", command=self.connect_adb)
        self.btn_connect.pack(fill=tk.X, pady=2)
        self.btn_start = ttk.Button(ctrl, text="▶️ Запустить бота", command=self.toggle_bot, state=tk.DISABLED)
        self.btn_start.pack(fill=tk.X, pady=2)
        self.btn_scrcpy = ttk.Button(ctrl, text="🖥️ Открыть scrcpy", command=self.open_scrcpy)
        self.btn_scrcpy.pack(fill=tk.X, pady=2)

        # Тактика
        ttk.Label(right, text="Тактика боя:", font=("Segoe UI", 11, "bold")).pack(anchor=tk.W, pady=(10, 2))
        self.tactic_var = tk.StringVar(value=self.brain.tactic)
        for key, cfg in TACTICS.items():
            ttk.Radiobutton(right, text=f"{cfg['name']} — {cfg['desc'][:40]}...",
                            variable=self.tactic_var, value=key,
                            command=self.on_tactic_change).pack(anchor=tk.W)
        self.tactic_info = ttk.Label(right, text=self.brain.explain_tactic(), wraplength=300, foreground="gray")
        self.tactic_info.pack(anchor=tk.W, pady=4)

        # Колода
        ttk.Label(right, text="Ваша колода:", font=("Segoe UI", 11, "bold")).pack(anchor=tk.W, pady=(10, 2))
        for c in USER_DECK:
            ttk.Label(right, text=f"  {c.elixir}💜 {c.name_ru}").pack(anchor=tk.W)

        # Лог
        ttk.Label(right, text="Лог бота:", font=("Segoe UI", 11, "bold")).pack(anchor=tk.W, pady=(10, 2))
        self.log_text = tk.Text(right, height=10, wrap=tk.WORD, font=("Consolas", 9))
        self.log_text.pack(fill=tk.BOTH, expand=True)

    def log(self, msg: str):
        self.log_text.insert(tk.END, f"[{time.strftime('%H:%M:%S')}] {msg}\n")
        self.log_text.see(tk.END)

    def connect_adb(self):
        devices = self.adb.list_devices()
        if not devices:
            messagebox.showwarning("ADB", "Устройства не найдены.\nПроверьте: USB-отладка включена, телефон подключён, adb в PATH.")
            self.log("ADB: устройства не найдены")
            return
        self.adb.connect(devices[0])
        w, h = self.adb.get_screen_size()
        self.status_var.set(f"Подключено: {devices[0]} • {w}x{h}")
        self.btn_start.config(state=tk.NORMAL)
        self.log(f"ADB подключён: {devices[0]} ({w}x{h})")

    def open_scrcpy(self):
        ok, msg = self.adb.start_scrcpy()
        self.log(msg)
        if not ok:
            messagebox.showinfo("scrcpy", msg)

    def on_tactic_change(self):
        self.brain.set_tactic(self.tactic_var.get())
        self.tactic_info.config(text=self.brain.explain_tactic())
        self.log(f"Тактика изменена: {TACTICS[self.tactic_var.get()]['name']}")

    def toggle_bot(self):
        if self.running:
            self.running = False
            self.btn_start.config(text="▶️ Запустить бота")
            self.log("Бот остановлен")
        else:
            self.running = True
            self.btn_start.config(text="⏹️ Остановить бота")
            self.log(f"Бот запущен. Тактика: {TACTICS[self.brain.tactic]['name']}")
            threading.Thread(target=self.bot_loop, daemon=True).start()

    def bot_loop(self):
        while self.running:
            try:
                png = self.adb.screencap()
                state = self.vision.analyze(png)
                if state.frame_ok:
                    # Показываем кадр (упрощённо — текст)
                    self.after(0, lambda: self.status_var.set(
                        f"Эликсир {state.elixir:.1f} • Время {state.time_left}с • Рука: {state.hand}"))
                    # AI-решение
                    d = self.brain.decide(state)
                    if d.card_id:
                        self.log(f"Решение: {d.card_name} → {d.position} ({d.reason})")
                        # Клик по слоту + арене
                        w, h = self.adb.get_screen_size()
                        # Находим слот (упрощённо — первый доступный)
                        slot = 0
                        x1, y1 = self.vision.card_slot_xy(slot, w, h)
                        self.adb.tap(x1, y1)
                        time.sleep(0.3)
                        x2, y2 = self.vision.arena_xy(d.position, w, h)
                        self.adb.tap(x2, y2)
                        self.log(f"  Клик: слот {slot} ({x1},{y1}) → арена ({x2},{y2})")
                    else:
                        self.log(f"Ждём: {d.reason}")
                else:
                    self.log("Кадр не получен (проверьте ADB)")
            except Exception as e:
                self.log(f"Ошибка цикла: {e}")
            time.sleep(1.5)

    # ---------- Вкладка КАРТЫ ----------
    def _build_cards_tab(self):
        top = ttk.Frame(self.tab_cards, padding=8)
        top.pack(fill=tk.X)
        ttk.Label(top, text="Поиск:").pack(side=tk.LEFT)
        self.card_search = ttk.Entry(top, width=30)
        self.card_search.pack(side=tk.LEFT, padx=4)
        self.card_search.bind("<Return>", lambda e: self.refresh_cards())
        ttk.Button(top, text="Найти", command=self.refresh_cards).pack(side=tk.LEFT)
        ttk.Label(top, text="Редкость:").pack(side=tk.LEFT, padx=(12, 2))
        self.rarity_var = tk.StringVar(value="Все")
        ttk.Combobox(top, textvariable=self.rarity_var, values=["Все"] + db.rarities(), width=14,
                     state="readonly").pack(side=tk.LEFT)
        ttk.Button(top, text="Фильтр", command=self.refresh_cards).pack(side=tk.LEFT, padx=4)
        ttk.Label(top, text=f"Всего карт: {db.count()}", foreground="gray").pack(side=tk.RIGHT)

        paned = ttk.PanedWindow(self.tab_cards, orient=tk.HORIZONTAL)
        paned.pack(fill=tk.BOTH, expand=True, padx=8, pady=4)

        # Список
        left = ttk.Frame(paned)
        self.card_list = tk.Listbox(left, font=("Segoe UI", 10), height=30)
        self.card_list.pack(fill=tk.BOTH, expand=True)
        self.card_list.bind("<<ListboxSelect>>", self.on_card_select)
        paned.add(left, weight=1)

        # Детали
        right = ttk.Frame(paned, padding=8)
        self.card_detail = tk.Text(right, wrap=tk.WORD, font=("Segoe UI", 11), height=30, width=55)
        self.card_detail.pack(fill=tk.BOTH, expand=True)
        paned.add(right, weight=1)

        self._all_cards = db.cards
        self.refresh_cards()

    def refresh_cards(self):
        q = self.card_search.get().strip() if hasattr(self, "card_search") else ""
        rarity = self.rarity_var.get() if hasattr(self, "rarity_var") else "Все"
        cards = db.search(q) if q else db.cards
        if rarity != "Все":
            cards = [c for c in cards if c["rarity"] == rarity]
        self._all_cards = cards
        self.card_list.delete(0, tk.END)
        for c in cards:
            self.card_list.insert(tk.END, f"{c['emoji']} {c['name_ru']}  •  {c['elixir']}💜  •  {c['rarity']}")

    def on_card_select(self, event):
        sel = self.card_list.curselection()
        if not sel:
            return
        c = self._all_cards[sel[0]]
        self.card_detail.delete("1.0", tk.END)
        text = f"""{c['emoji']} {c['name_ru']} ({c['name_en']})
{'='*50}
Эликсир: {c['elixir']} 💜   Редкость: {c['rarity']}   Тип: {c['type']}
Цели: {c['targets']}   Скорость: {c['speed']}   Дальность: {c['range']}   Кол-во: {c['count']}
HP: {c['hp']}   Урон: {c['dmg']}

📖 Описание:
{c['desc']}

🎮 Как играет:
{c['how_plays']}

✅ Контрит (чем бить):
{', '.join(c['counters'])}

❌ Слаб против:
{', '.join(c['weak_vs'])}
"""
        self.card_detail.insert(tk.END, text)

    # ---------- Вкладка ОБУЧЕНИЕ ----------
    def _build_train_tab(self):
        frame = ttk.Frame(self.tab_train, padding=12)
        frame.pack(fill=tk.BOTH, expand=True)
        ttk.Label(frame, text="🎓 Полное обучение бота (симуляция + hill climbing)",
                  font=("Segoe UI", 13, "bold")).pack(anchor=tk.W)
        ttk.Label(frame, text="Бот играет тысячи симулированных боёв против случайных колод\nи подбирает лучшие веса решений. Это и есть «полное обучение» на правилах.",
                  foreground="gray").pack(anchor=tk.W, pady=4)

        # Настройки
        settings = ttk.Frame(frame)
        settings.pack(fill=tk.X, pady=8)
        ttk.Label(settings, text="Тактика для обучения:").pack(side=tk.LEFT)
        self.train_tactic_var = tk.StringVar(value="balanced")
        ttk.Combobox(settings, textvariable=self.train_tactic_var,
                     values=list(TACTICS.keys()), width=16, state="readonly").pack(side=tk.LEFT, padx=4)
        ttk.Label(settings, text="Итераций:").pack(side=tk.LEFT, padx=(12, 2))
        self.train_iters = ttk.Spinbox(settings, from_=5, to=100, width=6)
        self.train_iters.set(30)
        self.train_iters.pack(side=tk.LEFT)
        ttk.Label(settings, text="Боёв/итерация:").pack(side=tk.LEFT, padx=(12, 2))
        self.train_battles = ttk.Spinbox(settings, from_=50, to=1000, width=6)
        self.train_battles.set(200)
        self.train_battles.pack(side=tk.LEFT)

        btns = ttk.Frame(frame)
        btns.pack(fill=tk.X, pady=4)
        self.btn_train = ttk.Button(btns, text="🚀 Запустить обучение", command=self.start_training)
        self.btn_train.pack(side=tk.LEFT, padx=4)
        ttk.Button(btns, text="🏆 Обучить все тактики", command=self.start_training_all).pack(side=tk.LEFT, padx=4)
        self.train_status = tk.StringVar(value="Готово к обучению")
        ttk.Label(btns, textvariable=self.train_status, foreground="blue").pack(side=tk.LEFT, padx=12)

        self.train_progress = ttk.Progressbar(frame, mode="determinate")
        self.train_progress.pack(fill=tk.X, pady=8)

        self.train_log = tk.Text(frame, height=18, wrap=tk.WORD, font=("Consolas", 9))
        self.train_log.pack(fill=tk.BOTH, expand=True)

    def tlog(self, msg):
        self.train_log.insert(tk.END, msg + "\n")
        self.train_log.see(tk.END)

    def start_training(self):
        tactic = self.train_tactic_var.get()
        iters = int(self.train_iters.get())
        battles = int(self.train_battles.get())
        self.btn_train.config(state=tk.DISABLED)
        self.train_status.set(f"Обучение: {TACTICS[tactic]['name']}...")
        self.train_log.delete("1.0", tk.END)
        self.tlog(f"Запуск обучения: {TACTICS[tactic]['name']}, {iters} итераций x {battles} боёв")

        def run():
            trainer = Trainer(tactic=tactic)
            def cb(done, total, score, best):
                self.after(0, lambda: self.train_progress.config(value=done/total*100))
                self.after(0, lambda: self.tlog(f"  Итерация {done}/{total}: скор={score:.1f} лучший={best:.1f}"))
            res = trainer.train(iterations=iters, battles_per_iter=battles, progress_cb=cb)
            self.after(0, lambda: self.train_status.set(f"Готово! Лучший скор: {res['best_score']:.1f}"))
            self.after(0, lambda: self.tlog(f"\n✅ Обучение завершено. Веса сохранены в data/trained_weights.json"))
            self.after(0, lambda: self.btn_train.config(state=tk.NORMAL))
            # Применяем обученного
            self.brain = trainer.brain
            self.trainer = trainer

        threading.Thread(target=run, daemon=True).start()

    def start_training_all(self):
        self.btn_train.config(state=tk.DISABLED)
        self.train_log.delete("1.0", tk.END)
        self.tlog("Обучение всех тактик...")

        def run():
            trainer = Trainer()
            res = trainer.train_all_tactics(iterations=15, battles_per_iter=150)
            best = res["best_tactic"]
            self.after(0, lambda: self.train_status.set(f"Лучшая тактика: {TACTICS[best]['name']}"))
            self.after(0, lambda: self.tlog(f"\n🏆 Лучшая тактика: {TACTICS[best]['name']}"))
            self.after(0, lambda: self.btn_train.config(state=tk.NORMAL))
            self.brain = trainer.brain
            self.trainer = trainer
            self.tactic_var.set(best)

        threading.Thread(target=run, daemon=True).start()

    # ---------- Вкладка НАСТРОЙКИ ----------
    def _build_settings_tab(self):
        frame = ttk.Frame(self.tab_settings, padding=12)
        frame.pack(fill=tk.BOTH, expand=True)
        ttk.Label(frame, text="⚙️ Настройки и помощь", font=("Segoe UI", 13, "bold")).pack(anchor=tk.W)
        help_text = """Как подключить телефон:
1. Включите «Отладку по USB» на телефоне (Настройки → О телефоне → 7 раз тап по номеру сборки → Для разработчиков).
2. Установите Android SDK Platform Tools на Windows (https://developer.android.com/tools/releases/platform-tools).
3. Подключите телефон USB, разрешите отладку.
4. Нажмите «Подключить ADB» во вкладке «Бот».

Как играть:
1. Откройте Clash Royale на телефоне.
2. Нажмите «Запустить бота» — бот будет видеть экран через ADB и кликать сам.
3. Тактику можно менять прямо во время боя (радиокнопки справа).

⚠️ Предупреждение: автоматизация может нарушать правила Supercell.
Используйте на свой риск — возможен бан аккаунта. Рекомендуется тестировать на твике.

Файлы:
• data/cards.json — база всех карт (156)
• data/trained_weights.json — обученные веса (создаётся после обучения)
• tools/calibrate.py — калибровка координат экрана
"""
        tk.Text(frame, wrap=tk.WORD, font=("Segoe UI", 11), height=20).pack(fill=tk.BOTH, expand=True)
        frame.children[list(frame.children.keys())[-1]].insert("1.0", help_text)

def run():
    app = BotApp()
    app.mainloop()

if __name__ == "__main__":
    run()
