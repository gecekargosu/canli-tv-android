"""markDead içindeki metin satırlarını doğru Java kaçışlarıyla yazar."""
import io

BS = chr(92)
p = 'src/com/cengiz/canlitv/PlayerActivity.java'
raw = io.open(p, encoding='utf-8', newline='').read()
crlf = '\r\n' in raw
lines = raw.replace('\r\n', '\n').split('\n')

idx = [k for k, l in enumerate(lines) if 'errorView.setText("Bu kanal şu an açılamadı.' in l]
assert len(idx) >= 1, idx
# markDead içindeki olan: sonraki satırı "Dokun → tekrar dene" içeren
target = None
for k in idx:
    if 'neden' in lines[k] or (k + 1 < len(lines) and 'Dokun' in lines[k + 1]):
        target = k
assert target is not None, idx

ind = ' ' * 12
lines[target] = (ind + 'errorView.setText("Bu kanal şu an açılamadı.' + BS + 'n' + BS + 'n" + neden')
lines[target + 1] = (ind + '        + " · Dokun → tekrar dene' + BS + 'nveya ⏭ ile sonraki kanal");')

io.open(p, 'w', encoding='utf-8', newline='').write(
    ('\r\n' if crlf else '\n').join(lines))
print('markDead metni yazildi')
