"""ADB-контроллер: видит экран телефона и кликает по нему.
Работает на Windows через adb.exe (Android SDK Platform Tools).
"""
import subprocess
import os
import time
import shutil
from typing import Optional, List

class ADBController:
    def __init__(self, adb_path: str = "adb", device: Optional[str] = None):
        self.adb = adb_path
        self.device = device
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

    def tap(self, x: int, y: int):
        try:
            self._run(["shell", "input", "tap", str(x), str(y)], timeout=5)
        except Exception:
            pass

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
        try:
            r = self._run(["shell", "wm", "size"], timeout=5)
            out = r.stdout.decode(errors="ignore")
            # Override size: 1080x2400
            for line in out.splitlines():
                if "Override" in line:
                    size = line.split(":")[-1].strip()
                    w, h = size.split("x")
                    return int(w), int(h)
            for line in out.splitlines():
                if "Physical" in line:
                    size = line.split(":")[-1].strip()
                    w, h = size.split("x")
                    return int(w), int(h)
        except Exception:
            pass
        return 1080, 2400

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
