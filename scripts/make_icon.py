"""One-off script to generate app icons from the RunwayIQ brand mark.
Run with: python scripts/make_icon.py
Produces src/main/resources/icon.ico and icon.icns.
"""
from PIL import Image, ImageDraw
import os

SIZE = 1024
BG = (34, 211, 238, 255)     # #22D3EE - app primary cyan
FG = (255, 255, 255, 255)    # white glyph

def make_base_image() -> Image.Image:
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Circular badge background
    draw.ellipse((0, 0, SIZE, SIZE), fill=BG)

    # Simple "trending up" glyph: a rising polyline with an arrowhead,
    # echoing the sidebar logo badge (Icons.Default.TrendingUp).
    pad = SIZE * 0.26
    points = [
        (pad, SIZE - pad),
        (SIZE * 0.42, SIZE * 0.58),
        (SIZE * 0.58, SIZE * 0.70),
        (SIZE - pad, pad),
    ]
    stroke_w = int(SIZE * 0.065)
    draw.line(points, fill=FG, width=stroke_w, joint="curve")
    for x, y in points:
        r = stroke_w / 2
        draw.ellipse((x - r, y - r, x + r, y + r), fill=FG)

    # Arrowhead at the top-right end of the line
    tip = points[-1]
    arrow_len = SIZE * 0.16
    arrow = [
        tip,
        (tip[0] - arrow_len, tip[1]),
        (tip[0], tip[1] + arrow_len),
    ]
    draw.polygon(arrow, fill=FG)

    return img

def main():
    out_dir = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
    os.makedirs(out_dir, exist_ok=True)

    base = make_base_image()

    ico_path = os.path.join(out_dir, "icon.ico")
    base.save(ico_path, format="ICO", sizes=[(16, 16), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)])
    print(f"Wrote {ico_path}")

    icns_path = os.path.join(out_dir, "icon.icns")
    try:
        base.save(icns_path, format="ICNS")
        print(f"Wrote {icns_path}")
    except Exception as e:
        print(f"ICNS save failed: {e}")

if __name__ == "__main__":
    main()
