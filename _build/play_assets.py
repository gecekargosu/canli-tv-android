# -*- coding: utf-8 -*-
"""Play Store gorsellerini uretir: 512 ikon, 1024x500 one cikan gorsel, 9:16 ekran goruntusu."""
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

PLAY = os.path.join(os.path.dirname(__file__), "..", "docs", "play")
PLAY = os.path.abspath(PLAY)
FONTB = "C:/Windows/Fonts/segoeuib.ttf"
FONTR = "C:/Windows/Fonts/segoeui.ttf"

BG = (17, 21, 28)
PINK = (194, 24, 91)
ORANGE = (255, 122, 26)


def rounded(draw, box, r, fill):
    draw.rounded_rectangle(box, radius=r, fill=fill)


def draw_mark(size):
    """Uygulama ikonundaki vektorun ayni oranlarla cizimi (108 birim -> size)."""
    S = 8  # supersample
    W = size * S
    s = W / 108.0
    img = Image.new("RGBA", (W, W), BG + (255,))
    d = ImageDraw.Draw(img)
    # ekran (beyaz govde)
    rounded(d, (26 * s, 38 * s, 82 * s, 74 * s), 4 * s, (255, 255, 255, 255))
    # pembe oynat ucgeni
    d.polygon([(47 * s, 46 * s), (63 * s, 56 * s), (47 * s, 66 * s)], fill=PINK + (255,))
    # anten
    d.polygon([(38 * s, 26 * s), (54 * s, 32 * s), (70 * s, 26 * s),
               (70 * s, 31 * s), (54 * s, 37 * s), (38 * s, 31 * s)], fill=(255, 255, 255, 255))
    return img.resize((size, size), Image.LANCZOS)


def icon():
    out = os.path.join(PLAY, "ikon-512.png")
    draw_mark(512).convert("RGB").save(out, "PNG")
    return out


def feature():
    W, H = 1024, 500
    img = Image.new("RGB", (W, H), BG)
    d = ImageDraw.Draw(img)
    for y in range(H):
        t = y / H
        d.line([(0, y), (W, y)], fill=(int(13 + 12 * t), int(17 + 18 * t), int(23 + 24 * t)))
    # sol tarafta sicak isik (maskeli yumusak parlama)
    mask = Image.new("L", (W, H), 0)
    ImageDraw.Draw(mask).ellipse([-200, -140, 520, 600], fill=120)
    mask = mask.filter(ImageFilter.GaussianBlur(130))
    img = Image.composite(Image.new("RGB", (W, H), (58, 32, 10)), img, mask)
    d = ImageDraw.Draw(img)

    mark = draw_mark(150)
    img.paste(mark, (72, 84), mark)

    fb = ImageFont.truetype(FONTB, 74)
    fr = ImageFont.truetype(FONTR, 30)
    fs = ImageFont.truetype(FONTB, 21)

    d.text((72, 252), "UMAY Multi TV", font=fb, fill=(255, 255, 255))
    d.text((76, 350), "305 canlı kanal · 49 radyo · 3'lü izleme · kayıt",
           font=fr, fill=(168, 179, 196))

    # sag alt rozetler
    pills = ["360p veri tasarrufu", "Çoklu ekran", "Kayıt + PiP"]
    y = 330
    for p in pills:
        w = d.textlength(p, font=fs)
        x1 = W - 72
        d.rounded_rectangle([x1 - w - 30, y, x1, y + 40], radius=20,
                            fill=(26, 33, 44), outline=(255, 122, 26), width=2)
        d.text((x1 - w - 15, y + 9), p, font=fs, fill=(255, 186, 140))
        y += 52

    # turuncu vurgu cizgisi
    d.rounded_rectangle([72, 440, 300, 448], radius=4, fill=ORANGE)
    out = os.path.join(PLAY, "one-cikan-1024x500.png")
    img.save(out, "PNG")
    return out


def screenshot(src, dst, w=1080, h=1920):
    im = Image.open(src).convert("RGB")
    scale = min(w / im.width, h / im.height)
    im = im.resize((int(im.width * scale), int(im.height * scale)), Image.LANCZOS)
    canvas = Image.new("RGB", (w, h), (11, 15, 20))
    canvas.paste(im, ((w - im.width) // 2, (h - im.height) // 2))
    canvas.save(dst, "PNG")
    return dst


if __name__ == "__main__":
    print(icon())
    print(feature())
    print(screenshot(os.path.join(PLAY, "01-ana-ekran.png"), os.path.join(PLAY, "ekran-1.png")))
