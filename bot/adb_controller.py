"""ADB-контроллер: видит экран телефона и кликает по нему.
Работает на Windows через adb.exe (Android SDK Platform Tools).
"""
import subprocess
import os
import struct
import time
import shutil
import threading
from typing import Optional, List, Tuple


class _FrameStream:
    """Непрерывный поток кадров от одного процесса `adb exec-out`.

    Запускает на телефоне цикл `while true; do screencap; done` и читает кадры
    из stdout. Формат screencap самоописывающийся: заголовок (w, h, fmt) и,
    начиная с Android 9, ещё colorspace; дальше сырые RGBA-пиксели.
    Экономит ~100 мс на кадр: не нужно запускать adb.exe заново.
    """

    MAX_SIDE = 10000
    MAX_HEADER = 16

    def __init__(self, proc):
        self.proc = proc
        self.header_size = None      # 12 или 16, определяется по первому кадру
        self.width = 0
        self.height = 0
        self.error = None
        self.frames = 0
        self._buf = bytearray()

    # ---------- чтение сырых байт ----------
    def _fill(self, n: int) -> bool:
        """Добирает в буфер так, чтобы в нём было минимум n байт."""
        while len(self._buf) < n:
            chunk = self.proc.stdout.read(max(4096, n - len(self._buf)))
            if not chunk:
                return False
            self._buf.extend(chunk)
        return True

    @classmethod
    def _dims(cls, buf, offset=0):
        if len(buf) < offset + 12:
            return None
        w, h, fmt = struct.unpack("<III", bytes(buf[offset:offset + 12]))
        if not (0 < w < cls.MAX_SIDE and 0 < h < cls.MAX_SIDE):
            return None
        return w, h, fmt

    def _detect_header_size(self):
        """Определяет 12 или 16 байт заголовка, проверяя следующий кадр."""
        dims = self._dims(self._buf)
        if dims is None:
            return None
        w, h, _ = dims
        body = w * h * 4
        # Пробуем оба варианта; верный тот, после которого идёт заголовок с теми же w,h
        for hs in (16, 12):
            if not self._fill(hs + body + 12):
                continue
            nxt = self._dims(self._buf, hs + body)
            if nxt is None or nxt[0] != w or nxt[1] != h:
                continue
            if hs == 16:
                colorspace = struct.unpack("<I", bytes(self._buf[12:16]))[0]
                if colorspace > 8:
                    continue
            self.header_size, self.width, self.height = hs, w, h
            return hs
        return None

    def read_frame(self):
        """Читает один кадр. Возвращает BGR-массив или None при ошибке/конце потока."""
        import numpy as np
        import cv2

        if self.header_size is None:
            if not self._fill(12):
                self.error = "поток закрыт"
                return None
            if self._detect_header_size() is None:
                # Не смогли определить заголовок по следующему кадру.
                if not self._fill(12):
                    self.error = "поток закрыт"
                    return None
                dims = self._dims(self._buf)
                if dims is None:
                    self.error = "неверный заголовок кадра"
                    return None
                self.header_size = 12
                self.width, self.height = dims[0], dims[1]

        w, h = self.width, self.height
        body = w * h * 4
        need = self.header_size + body
        if not self._fill(need):
            self.error = "поток закрыт"
            return None

        dims = self._dims(self._buf)
        if dims is None or dims[0] != w or dims[1] != h:
            # Потеря синхронизации: пробуем найти следующий осмысленный заголовок
            self._resync()
            return None

        pixels = bytes(self._buf[self.header_size:need])
        del self._buf[:need]
        self.frames += 1
        arr = np.frombuffer(pixels, dtype=np.uint8).reshape(h, w, 4)
        return cv2.cvtColor(arr, cv2.COLOR_RGBA2BGR)

    def _resync(self):
        """Ищет начало следующего кадра, сдвигая буфер по одному байту."""
        for shift in range(1, min(len(self._buf), 1 << 16)):
            dims = self._dims(self._buf, shift)
            if dims is None:
                continue
            w, h, _ = dims
            if w == self.width and h == self.height:
                del self._buf[:shift]
                return
        self._buf.clear()
        self.error = "потеря синхронизации"


class ADBController:
    def __init__(self, adb_path: str = "adb", device: Optional[str] = None):
        self.adb = adb_path
        self.device = device
        self._screen_size: Optional[Tuple[int, int]] = None
        self._shell = None
        self._shell_lock = threading.Lock()
        self._raw_ok = True
        self._stream = None
        self._stream_fail = 0
        self.last_capture_ms = 0.0
        self._check_adb()

    def _check_adb(self):
        # Ищем adb в PATH или рядом
        if shutil.which(self.adb) is None and not os.path.exists(self.adb):
            # Попробуем стандартные пути Windows
            candidates = [
                r"C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe",
                r"C:\platform-tools\adb.exe",
                "./adb.exe",
                "./platform-tools/adb.exe",
            ]
            for c in candidates:
                c = os.path.expandvars(c)
                if os.path.exists(c):
                    self.adb = c
                    break

    def _run(self, args: List[str], timeout: int = 10) -> subprocess.CompletedProcess:
        cmd = [self.adb]
        if self.device:
            cmd += ["-s", self.device]
        cmd += args
        return subprocess.run(cmd, capture_output=True, timeout=timeout)

    def list_devices(self) -> List[str]:
        try:
            r = self._run(["devices"], timeout=5)
            out = r.stdout.decode(errors="ignore")
            devices = []
            for line in out.splitlines()[1:]:
                line = line.strip()
                if line and "\tdevice" in line:
                    devices.append(line.split("\t")[0])
            return devices
        except Exception:
            return []

    def connect(self, device: str):
        self.device = device

    def is_connected(self) -> bool:
        return len(self.list_devices()) > 0

    def screencap(self) -> Optional[bytes]:
        """Возвращает PNG кадра экрана телефона."""
        try:
            r = self._run(["exec-out", "screencap", "-p"], timeout=8)
            if r.returncode == 0 and r.stdout:
                return r.stdout
        except Exception:
            pass
        return None

    def screencap_raw(self) -> Optional[bytes]:
        """Быстрый захват без PNG: `exec-out screencap` отдаёт сырые пиксели
        (заголовок + RGBA). На 720x1604 это в разы быстрее, чем PNG."""
        try:
            r = self._run(["exec-out", "screencap"], timeout=8)
            if r.returncode != 0 or len(r.stdout) < 16:
                return None
            return r.stdout
        except Exception:
            return None

    @staticmethod
    def decode_raw(raw: bytes):
        """Разбирает сырой screencap в массив BGR (h, w, 3). None если формат не тот."""
        if not raw or len(raw) < 16:
            return None
        try:
            w, h, fmt = struct.unpack("<III", raw[:12])
        except Exception:
            return None
        if w <= 0 or h <= 0 or w > 10000 or h > 10000:
            return None
        need = w * h * 4
        # Заголовок: 12 байт (w,h,fmt) или 16 байт (w,h,fmt,colorspace).
        # Выбираем тот, при котором размер сходится точно — иначе кадр съедет.
        header = None
        if len(raw) - 12 == need:
            header = 12
        elif len(raw) - 16 == need:
            header = 16
        elif len(raw) >= 12 + need:
            header = 12
        elif len(raw) >= 16 + need:
            header = 16
        if header is None:
            return None
        body = raw[header:header + need]
        try:
            import numpy as np
            import cv2
            arr = np.frombuffer(body, dtype=np.uint8).reshape(h, w, 4)
            # screencap отдаёт RGBA
            return cv2.cvtColor(arr, cv2.COLOR_RGBA2BGR)
        except Exception:
            return None

    def start_stream(self) -> bool:
        """Запускает непрерывный захват кадров (один процесс на весь бой)."""
        if self._stream is not None:
            return True
        cmd = [self.adb]
        if self.device:
            cmd += ["-s", self.device]
        cmd += ["exec-out", "sh", "-c", "while true; do screencap; done"]
        try:
            proc = subprocess.Popen(cmd, stdout=subprocess.PIPE,
                                    stderr=subprocess.DEVNULL, bufsize=0)
        except Exception:
            return False
        self._stream = _FrameStream(proc)
        return True

    def stop_stream(self):
        stream, self._stream = self._stream, None
        if stream is not None:
            try:
                stream.proc.kill()
            except Exception:
                pass

    def stream_frame(self):
        """Следующий кадр из потока. None — поток недоступен."""
        if self._stream is None:
            return None
        t0 = time.perf_counter()
        frame = self._stream.read_frame()
        if frame is None:
            self._stream_fail += 1
            if self._stream_fail >= 3:
                self.stop_stream()
                self._stream_fail = 0
            return None
        self._stream_fail = 0
        self.last_capture_ms = (time.perf_counter() - t0) * 1000
        return frame

    def screencap_frame(self):
        """Кадр сразу как BGR-массив. Сначала пробует быстрый raw-режим,
        при проблеме откатывается на PNG (и запоминает, что raw не работает)."""
        # 1. Непрерывный поток — самый быстрый путь
        if self._stream is not None:
            frame = self.stream_frame()
            if frame is not None:
                return frame

        t0 = time.perf_counter()
        if self._raw_ok:
            frame = self.decode_raw(self.screencap_raw())
            if frame is not None:
                self.last_capture_ms = (time.perf_counter() - t0) * 1000
                return frame
            self._raw_ok = False
        import cv2
        import numpy as np
        png = self.screencap()
        if not png:
            return None
        frame = cv2.imdecode(np.frombuffer(png, dtype=np.uint8), cv2.IMREAD_COLOR)
        self.last_capture_ms = (time.perf_counter() - t0) * 1000
        return frame

    def _ensure_shell(self):
        """Один постоянный `adb shell` для кликов: не тратим ~150-300 мс
        на запуск adb.exe при каждом тапе."""
        if self._shell is not None and self._shell.poll() is None:
            return self._shell
        cmd = [self.adb]
        if self.device:
            cmd += ["-s", self.device]
        cmd += ["shell"]
        try:
            self._shell = subprocess.Popen(
                cmd, stdin=subprocess.PIPE, stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL, bufsize=0,
            )
        except Exception:
            self._shell = None
        return self._shell

    def close(self):
        self.stop_stream()
        with self._shell_lock:
            if self._shell is not None:
                try:
                    self._shell.stdin.close()
                    self._shell.terminate()
                except Exception:
                    pass
                self._shell = None

    def tap(self, x: int, y: int):
        """Быстрый клик через постоянный shell, с откатом на обычный вызов."""
        line = f"input tap {int(x)} {int(y)}\n".encode()
        with self._shell_lock:
            sh = self._ensure_shell()
            if sh is not None:
                try:
                    sh.stdin.write(line)
                    sh.stdin.flush()
                    return
                except Exception:
                    self._shell = None
        try:
            self._run(["shell", "input", "tap", str(x), str(y)], timeout=5)
        except Exception:
            pass

    def tap_many(self, points, gap_s: float = 0.0):
        """Несколько кликов подряд через один shell (без перезапуска adb)."""
        with self._shell_lock:
            sh = self._ensure_shell()
            if sh is None:
                for x, y in points:
                    self.tap(x, y)
                return
            try:
                for x, y in points:
                    sh.stdin.write(f"input tap {int(x)} {int(y)}\n".encode())
                    sh.stdin.flush()
                    if gap_s:
                        time.sleep(gap_s)
                return
            except Exception:
                self._shell = None
        for x, y in points:
            self.tap(x, y)

    def swipe(self, x1: int, y1: int, x2: int, y2: int, duration_ms: int = 300):
        try:
            self._run(["shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration_ms)], timeout=5)
        except Exception:
            pass

    def key(self, code: int):
        try:
            self._run(["shell", "input", "keyevent", str(code)], timeout=5)
        except Exception:
            pass

    def get_screen_size(self):
        if self._screen_size:
            return self._screen_size
        try:
            r = self._run(["shell", "wm", "size"], timeout=5)
            out = r.stdout.decode(errors="ignore")
            # Override size: 1080x2400
            for line in out.splitlines():
                if "Override" in line:
                    size = line.split(":")[-1].strip()
                    w, h = size.split("x")
                    self._screen_size = (int(w), int(h))
                    return self._screen_size
            for line in out.splitlines():
                if "Physical" in line:
                    size = line.split(":")[-1].strip()
                    w, h = size.split("x")
                    self._screen_size = (int(w), int(h))
                    return self._screen_size
        except Exception:
            pass
        self._screen_size = (1080, 2400)
        return self._screen_size

    def start_scrcpy(self, extra_args: str = ""):
        """Запуск scrcpy для зеркалирования (если установлен)."""
        scrcpy = shutil.which("scrcpy")
        if not scrcpy:
            return False, "scrcpy не найден. Установите с https://github.com/Genymobile/scrcpy"
        try:
            args = [scrcpy, "--window-title=Clash Royale Bot"]
            if self.device:
                args += ["-s", self.device]
            if extra_args:
                args += extra_args.split()
            subprocess.Popen(args)
            return True, "scrcpy запущен"
        except Exception as e:
            return False, str(e)
