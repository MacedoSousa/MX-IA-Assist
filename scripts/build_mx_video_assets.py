from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs' / 'portfolio' / 'video-assets'
OUT.mkdir(parents=True, exist_ok=True)
W, H = 1280, 720
BG = '#07111d'
PANEL = '#0d1b2a'
CYAN = '#45d9ff'
BLUE = '#1687d4'
WHITE = '#f3f8ff'
MUTED = '#9bb0c5'

FONT = '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'
FONT_BOLD = '/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'

def f(size, bold=False):
    return ImageFont.truetype(FONT_BOLD if bold else FONT, size)

def base():
    im = Image.new('RGB', (W, H), BG)
    d = ImageDraw.Draw(im)
    for x in range(0, W, 80):
        d.line((x, 0, x, H), fill='#0b1b2b', width=1)
    for y in range(0, H, 80):
        d.line((0, y, W, y), fill='#0b1b2b', width=1)
    return im

def text(draw, xy, value, size, fill=WHITE, bold=False, anchor=None):
    draw.text(xy, value, font=f(size, bold), fill=fill, anchor=anchor)

def pill(draw, xy, value, width, fill=PANEL):
    x, y = xy
    draw.rounded_rectangle((x, y, x+width, y+42), radius=12, fill=fill, outline=CYAN, width=1)
    text(draw, (x+width//2, y+21), value, 18, CYAN, True, 'mm')

def save(im, n):
    im.save(OUT / f'{n}.png', optimize=True)

# Cover
im = Image.open(ROOT / 'docs' / 'portfolio' / 'mx-video-opening.png').convert('RGB').resize((W, H))
d = ImageDraw.Draw(im)
d.rectangle((0, 0, W, H), fill=(7, 17, 29, 80))
text(d, (760, 238), 'MX', 76, CYAN, True, 'mm')
text(d, (760, 316), 'Universo local de', 32, WHITE, True, 'mm')
text(d, (760, 356), 'inteligência, cuidado e produtividade', 27, WHITE, False, 'mm')
text(d, (760, 420), 'Local universe of intelligence, care and productivity', 17, MUTED, False, 'mm')
text(d, (760, 520), 'MacedoSousa / MX-IA-Assist', 19, CYAN, True, 'mm')
save(im, '01-cover')

# Architecture
im = base(); d = ImageDraw.Draw(im)
text(d, (70, 62), 'Um único ponto de comunicação', 38, WHITE, True)
text(d, (70, 108), 'One communication point', 22, CYAN)
text(d, (70, 168), 'O usuário fala com o MX Core.', 24, WHITE, True)
text(d, (70, 205), 'Skills especialistas trabalham internamente.', 20, MUTED)
arch = Image.open(ROOT / 'docs' / 'architecture' / 'mx-universe.png').convert('RGB')
arch.thumbnail((790, 430))
card = Image.new('RGB', (810, 450), '#0a1624')
card.paste(arch, ((810-arch.width)//2, (450-arch.height)//2))
im.paste(card, (430, 180))
pill(d, (70, 330), 'WEB', 100)
pill(d, (185, 330), 'ANDROID', 150)
pill(d, (350, 330), 'IOS', 100)
text(d, (70, 458), 'Centralized core. Internal expertise.', 18, MUTED)
save(im, '02-architecture')

# Technology
im = base(); d = ImageDraw.Draw(im)
text(d, (70, 70), 'Arquitetura para evoluir', 42, WHITE, True)
text(d, (70, 120), 'Architecture built to evolve', 23, CYAN)
text(d, (70, 190), 'Java 21', 30, CYAN, True); text(d, (300, 190), 'Spring Boot 4', 30, WHITE, True)
text(d, (70, 250), 'Clean Architecture', 30, WHITE, True); text(d, (420, 250), 'TDD + SOLID', 30, CYAN, True)
text(d, (70, 310), 'Ollama local', 30, CYAN, True); text(d, (350, 310), 'qwen3:8b', 30, WHITE, True)
for i, (label, sub) in enumerate([('GeneralSkill','conversation'),('DevelopmentSkill','tools'),('QualitySkill','software quality')]):
    x = 70 + i*365
    d.rounded_rectangle((x, 420, x+320, 570), radius=18, fill=PANEL, outline=BLUE, width=2)
    text(d, (x+25, 462), label, 22, CYAN, True)
    text(d, (x+25, 505), sub, 18, MUTED)
text(d, (70, 650), 'Local model execution keeps the core on the user machine.', 18, MUTED)
save(im, '03-technology')

# Security
im = base(); d = ImageDraw.Draw(im)
text(d, (70, 70), 'Inteligência com cuidado', 42, WHITE, True)
text(d, (70, 120), 'Intelligence with care', 23, CYAN)
# shield
cx, cy = 220, 360
pts = [(cx,cy-145),(cx+110,cy-90),(cx+90,cy+75),(cx,cy+145),(cx-90,cy+75),(cx-110,cy-90)]
d.polygon(pts, fill='#102c42', outline=CYAN)
d.line((cx-45, cy, cx-8, cy+38, cx+60, cy-48), fill=CYAN, width=12, joint='curve')
for i, (key, value) in enumerate([('AWAITING_APPROVAL','sensitive actions'),('Allowlist','authorized tools'),('Sandbox','workspace boundary'),('JWT + nonce','revocable sessions')]):
    y = 210 + i*100
    d.rounded_rectangle((470, y, 1120, y+70), radius=15, fill=PANEL, outline=BLUE, width=1)
    text(d, (500, y+21), key, 22, CYAN, True)
    text(d, (500, y+49), value, 17, MUTED)
text(d, (70, 610), 'No silent execution. No model-generated authorization.', 20, WHITE, True)
save(im, '04-security')

# Evidence and closing
im = base(); d = ImageDraw.Draw(im)
text(d, (70, 64), 'Provas que podem ser reproduzidas', 39, WHITE, True)
text(d, (70, 112), 'Evidence that can be reproduced', 22, CYAN)
metrics = [('79', 'testes PASS'), ('4', 'healthchecks HTTP 200'), ('14.56 GB', 'limpeza segura'), ('8082', 'web local / LAN')]
for i, (n, label) in enumerate(metrics):
    x = 70 + (i%2)*570; y = 190 + (i//2)*180
    d.rounded_rectangle((x, y, x+500, y+130), radius=18, fill=PANEL, outline=BLUE, width=2)
    text(d, (x+28, y+30), n, 40, CYAN, True)
    text(d, (x+30, y+86), label, 19, MUTED)
text(d, (70, 595), 'Inteligência. Cuidado. Produtividade.', 29, WHITE, True)
text(d, (70, 645), 'MX — built locally, designed responsibly.', 18, CYAN)
save(im, '05-evidence')

print(OUT)
