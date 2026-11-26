"""Convierte el MP4 del prólogo a recursos reproducibles por el juego con Java 21."""

import argparse
from pathlib import Path
import shutil
import subprocess
from tempfile import TemporaryDirectory
from zipfile import ZipFile


FPS = 18
WIDTH = 640
HEIGHT = 360
ROOT = Path(__file__).resolve().parents[2]


def ffmpeg(*args):
    subprocess.run(["ffmpeg", "-nostdin", "-hide_banner", "-loglevel", "error", *args], check=True)


def preparar(video: Path):
    cliente = ROOT / "src/main/resources/cliente"
    destino = cliente / "mision1/prologo.zip"
    audio = cliente / "audio/cinematica/prologo-sofi.wav"
    with TemporaryDirectory() as directorio:
        base = Path(directorio)
        ajuste = (f"scale={WIDTH}:{HEIGHT}:force_original_aspect_ratio=decrease:force_divisible_by=2:flags=area,"
                  f"pad={WIDTH}:{HEIGHT}:(ow-iw)/2:(oh-ih)/2")
        ffmpeg("-i", str(video), "-map", "0:v:0", "-vf", f"fps={FPS},{ajuste}",
               "-q:v", "5", str(base / "%05d.jpg"))
        ffmpeg("-i", str(video), "-map", "0:a:0", "-ar", "22050", "-ac", "2",
               "-c:a", "pcm_s16le", str(base / "prologo-sofi.wav"))
        fotogramas = sorted(base.glob("*.jpg"))
        if not fotogramas:
            raise ValueError("El video no produjo fotogramas")
        with ZipFile(base / destino.name, "w") as archivo:
            archivo.writestr("reproduccion.properties", f"fps={FPS}\nfotogramas={len(fotogramas)}\n")
            for imagen in fotogramas:
                archivo.write(imagen, imagen.name)
        destino.parent.mkdir(parents=True, exist_ok=True)
        audio.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(base / destino.name, destino)
        shutil.copyfile(base / audio.name, audio)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("video", type=Path)
    preparar(parser.parse_args().video)
