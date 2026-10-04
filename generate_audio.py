"""Generate five seamless synthetic sirens for Siren Controller.

Method: deterministic additive synthesis at 48 kHz stereo, with instantaneous
frequency sweeps, harmonics, mild amplitude modulation, soft saturation and a
very short equal-power seam crossfade. FFmpeg encodes the finished PCM to Vorbis.

Requirements when run manually: Python 3, numpy, ffmpeg.
The generated OGG files are committed to the mod, so GitHub Actions does not
need Python/numpy/ffmpeg to build the JAR.
"""
from __future__ import annotations

import math
import shutil
import subprocess
import tempfile
from pathlib import Path

import numpy as np

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/main/resources/assets/siren_controller/sounds/siren"
SR = 48_000
DURATION = 8.0
N = int(SR * DURATION)
T = np.arange(N, dtype=np.float64) / SR
rng = np.random.default_rng(260326)


def smooth_cycle(phase: np.ndarray) -> np.ndarray:
    # -1..1, sinusoidal sweep with smooth turning points.
    return np.sin(2.0 * np.pi * phase)


def integrate_frequency(freq: np.ndarray) -> np.ndarray:
    return 2.0 * np.pi * np.cumsum(freq) / SR


def additive(phase: np.ndarray, amps: list[tuple[int, float]]) -> np.ndarray:
    out = np.zeros_like(phase)
    for harmonic, amp in amps:
        out += amp * np.sin(phase * harmonic)
    return out


def soft_limit(x: np.ndarray) -> np.ndarray:
    # Smooth nonlinearity, no hard clipping.
    return np.tanh(1.08 * x) / np.tanh(1.08)


def stereoize(mono: np.ndarray, width: float = 0.08) -> np.ndarray:
    # Very small deterministic side difference. Minecraft still supplies spatial panning.
    delay = max(1, int(round(0.0015 * SR)))
    delayed = np.concatenate((mono[:1], mono[:-1]))
    left = mono * (1.0 + width) + delayed * width * 0.12
    right = mono * (1.0 - width) + delayed * width * 0.12
    return np.stack((left, right), axis=1)


def seam_fade(stereo: np.ndarray, ms: float = 18.0) -> np.ndarray:
    # Equal-power blend around the wrap point so the first and last samples agree.
    m = max(2, int(SR * ms / 1000.0))
    a = np.linspace(0.0, 1.0, m, dtype=np.float64)
    left = stereo[:m].copy()
    right = stereo[-m:].copy()
    blend = right * np.cos(a[:, None] * np.pi / 2.0) + left * np.sin(a[:, None] * np.pi / 2.0)
    stereo[:m] = blend
    stereo[-m:] = blend[::-1]
    return stereo


def finalize(mono: np.ndarray, width: float = 0.08) -> np.ndarray:
    mono = soft_limit(mono)
    stereo = stereoize(mono, width)
    stereo = seam_fade(stereo)
    peak = float(np.max(np.abs(stereo)))
    if peak > 0:
        stereo *= 0.92 / peak
    return stereo


def air_raid() -> np.ndarray:
    f = 500.0 + 185.0 * smooth_cycle(T / 4.0) + 10.0 * np.sin(2 * np.pi * T / 1.0)
    ph = integrate_frequency(f)
    mono = additive(ph, [(1, 0.78), (2, 0.28), (3, 0.14), (5, 0.07), (7, 0.035)])
    mono *= 0.94 + 0.06 * np.sin(2 * np.pi * T / 4.0 + 0.7)
    return finalize(mono, 0.06)


def police() -> np.ndarray:
    f = 635.0 + 255.0 * np.sin(2 * np.pi * T / 2.0) + 24.0 * np.sin(2 * np.pi * T / 0.5)
    ph = integrate_frequency(f)
    mono = additive(ph, [(1, 0.66), (2, 0.24), (3, 0.15), (4, 0.09), (6, 0.035)])
    # Alternating two-tone emphasis gives a familiar emergency-vehicle pattern.
    gate = 0.90 + 0.10 * np.sin(2 * np.pi * T / 2.0 + np.pi / 2.0)
    mono *= gate
    return finalize(mono, 0.09)


def fire() -> np.ndarray:
    period = 2.0
    # Smooth two-tone cycle rather than a square switch.
    phase = (T % period) / period
    f_low, f_high = 510.0, 760.0
    mix = 0.5 - 0.5 * np.cos(2 * np.pi * phase)
    f = f_low * (1.0 - mix) + f_high * mix
    ph = integrate_frequency(f)
    mono = additive(ph, [(1, 0.70), (2, 0.26), (3, 0.12), (5, 0.05)])
    mono += 0.08 * np.sin(integrate_frequency(f * 0.5))
    return finalize(mono, 0.07)


def nuclear() -> np.ndarray:
    f = 260.0 + 185.0 * smooth_cycle(T / 4.0)
    ph = integrate_frequency(f)
    mono = additive(ph, [(1, 0.86), (2, 0.31), (3, 0.13), (4, 0.08), (6, 0.03)])
    sub = np.sin(integrate_frequency(f * 0.5))
    mono += 0.20 * sub
    mono *= 0.92 + 0.08 * np.sin(2 * np.pi * T / 4.0)
    return finalize(mono, 0.05)


def industrial() -> np.ndarray:
    f = 365.0 + 125.0 * smooth_cycle(T / 4.0) + 18.0 * np.sin(2 * np.pi * T / 1.0)
    ph = integrate_frequency(f)
    mono = additive(ph, [(1, 0.72), (2, 0.34), (3, 0.20), (4, 0.10), (6, 0.05), (8, 0.025)])
    motor = 0.08 * np.sin(2 * np.pi * 115.0 * T) + 0.04 * np.sin(2 * np.pi * 230.0 * T)
    mono += motor
    mono *= 0.93 + 0.07 * np.sin(2 * np.pi * T / 0.8)
    return finalize(mono, 0.11)


def write_wav_and_ogg(name: str, stereo: np.ndarray) -> None:
    import wave

    OUT.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        wav = Path(tmp) / f"{name}.wav"
        pcm = np.clip(stereo, -1.0, 1.0)
        pcm16 = np.round(pcm * 32767.0).astype(np.int16)
        with wave.open(str(wav), "wb") as wf:
            wf.setnchannels(2)
            wf.setsampwidth(2)
            wf.setframerate(SR)
            wf.writeframes(pcm16.tobytes())

        if not shutil.which("ffmpeg"):
            raise RuntimeError("ffmpeg is required for OGG/Vorbis encoding")
        ogg = OUT / f"{name}.ogg"
        subprocess.run(
            ["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", str(wav),
             "-c:a", "libvorbis", "-q:a", "6", str(ogg)],
            check=True,
        )


SIRENS = {
    "air_raid": air_raid,
    "police": police,
    "fire": fire,
    "nuclear": nuclear,
    "industrial": industrial,
}

for name, fn in SIRENS.items():
    print("Generating", name)
    write_wav_and_ogg(name, fn())

print("Generated:")
for p in sorted(OUT.glob("*.ogg")):
    print(p.name, p.stat().st_size, "bytes")
