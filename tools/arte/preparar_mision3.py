"""Prepara los recursos originales del rediseño de la misión 3."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "tools/arte/referencias-mision3"
DEST = ROOT / "src/main/resources/cliente/mision3/redisenio"
DEST.mkdir(parents=True, exist_ok=True)


def alpha_crop(image: Image.Image) -> Image.Image:
    image = image.convert("RGBA")
    mask = image.getchannel("A").point(lambda value: 255 if value > 40 else 0)
    bounds = mask.getbbox()
    if bounds is None:
        raise ValueError("Imagen sin personaje visible")
    return image.crop(bounds)


for name in ("atrio", "pixel", "pasillo", "nucleo"):
    image = Image.open(SOURCE / f"{name}.png").convert("RGB")
    image.resize((1280, 720), Image.Resampling.LANCZOS).save(DEST / f"fondo-{name}.png")

for filename in ("troll", "spambot", "clickbait", "deepfake", "comment", "botswarm", "viral_core"):
    alpha_crop(Image.open(SOURCE / f"{filename}.png")).save(DEST / f"{filename}.png")


def walking_frames(source: Path, stem: str) -> None:
    """Extrae 4 direcciones × 4 poses con pivote estable en los pies."""
    sheet = Image.open(source).convert("RGBA")
    for row, direction in enumerate(("front", "back", "left", "right")):
        poses = []
        for column in range(4):
            x0 = round(column * sheet.width / 4)
            x1 = round((column + 1) * sheet.width / 4)
            y0 = round(row * sheet.height / 4)
            y1 = round((row + 1) * sheet.height / 4)
            poses.append(alpha_crop(sheet.crop((x0, y0, x1, y1))))
        width = max(pose.width for pose in poses) + 12
        height = max(pose.height for pose in poses) + 12
        for index, pose in enumerate(poses, 1):
            frame = Image.new("RGBA", (width, height))
            frame.alpha_composite(pose, ((width - pose.width) // 2, height - pose.height - 6))
            frame.save(DEST / f"{stem}_{direction}_{index}.png")


walking_frames(SOURCE / "influencer-caminar.png", "player")
for direction in ("front", "back", "left", "right"):
    # Reutilizar la primera pose de su misma dirección evita que cambie el
    # rostro o se invierta el perfil al detenerse.
    idle = Image.open(DEST / f"player_{direction}_1.png")
    idle.save(DEST / f"player_{direction}_idle.png")
    idle.save(DEST / f"player_{direction}_live.png")
for name in ("troll", "spambot", "clickbait", "deepfake", "comment", "botswarm"):
    walking_frames(SOURCE / f"{name}-caminar.png", f"enemy_{name}")
walking_frames(SOURCE / "viral-core-movimiento.png", "viral_core_move")

# Mr. Palomo conserva exactamente su atlas de la misión 1. La fila superior
# contiene direcciones y la inferior, los cuatro cuadros reales de vuelo.
palomo = Image.open(ROOT / "src/main/resources/cliente/arte/mision1/palomo.png").convert("RGBA")
cw, ch = palomo.width // 4, palomo.height // 2
for frame in range(4):
    idle = alpha_crop(palomo.crop((frame * cw, 0, (frame + 1) * cw, ch)))
    flight = alpha_crop(palomo.crop((frame * cw, ch, (frame + 1) * cw, palomo.height)))
    for pose in ("idle", "walk", "talk"):
        idle.save(DEST / f"palomo_{pose}_{frame + 1}.png")
    flight.save(DEST / f"palomo_fly_{frame + 1}.png")
