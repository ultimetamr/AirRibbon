"""Build crisp Android and PICO launcher icon assets from generated masters."""

from pathlib import Path
import shutil

from PIL import Image, ImageFilter


ROOT = Path(__file__).resolve().parents[1]
OPAQUE_SOURCE = Path(
    r"C:\Users\Administrator\.codex\generated_images\019ff555-b741-79c2-9bed-77780139cbfe"
    r"\exec-f636b1c2-92c8-4c1b-b7a6-8464c43daf66.png"
)
FOREGROUND_SOURCE = Path(
    r"C:\Users\Administrator\.codex\generated_images\019ff555-b741-79c2-9bed-77780139cbfe"
    r"\exec-beabd320-d71b-48b8-ae58-0a65753560b2.png"
)


def resized(source: Path, size: int, sharpen: bool = True) -> Image.Image:
    image = Image.open(source).convert("RGBA")
    image = image.resize((size, size), Image.Resampling.LANCZOS)
    if sharpen:
        rgb = image.convert("RGB").filter(
            ImageFilter.UnsharpMask(radius=0.8, percent=110, threshold=3)
        )
        rgb.putalpha(image.getchannel("A"))
        image = rgb
    return image


def main() -> None:
    assets = ROOT / "design" / "assets"
    drawable = ROOT / "app" / "src" / "main" / "res" / "drawable"
    mipmap = ROOT / "app" / "src" / "main" / "res" / "mipmap-anydpi"
    assets.mkdir(parents=True, exist_ok=True)

    shutil.copy2(OPAQUE_SOURCE, assets / "airribbon-app-icon-crisp.png")
    shutil.copy2(FOREGROUND_SOURCE, assets / "airribbon-icon-foreground-crisp.png")

    resized(OPAQUE_SOURCE, 1024).convert("RGB").save(
        mipmap / "ic_spatial_launcher.png", optimize=True
    )
    resized(OPAQUE_SOURCE, 1424).save(
        drawable / "ic_launcher_foreground.png", optimize=True
    )

    foreground = resized(FOREGROUND_SOURCE, 1024)
    foreground.save(drawable / "icon_3d_layer_1.png", optimize=True)

    alpha = foreground.getchannel("A")
    sdf_foreground = Image.new("RGBA", foreground.size, "white")
    sdf_foreground.putalpha(alpha)
    sdf_foreground.save(drawable / "icon_3d_sdf_1.png", optimize=True)

    background = Image.new("RGBA", (1024, 1024), (4, 8, 31, 255))
    background.save(drawable / "icon_3d_layer_0.png", optimize=True)
    Image.new("RGBA", (1024, 1024), "white").save(
        drawable / "icon_3d_sdf_0.png", optimize=True
    )


if __name__ == "__main__":
    main()
