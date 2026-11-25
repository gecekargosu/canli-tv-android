"""9.71: PlayerActivity M-99 ölü kanal düzeltmeleri (AdManager ayrı uygulandı)."""
import io

BS = chr(92)
p = 'src/com/cengiz/canlitv/PlayerActivity.java'
raw = io.open(p, encoding='utf-8', newline='').read()
crlf = '\r\n' in raw
s = raw.replace('\r\n', '\n')


def rep(old, new, count=1):
    global s
    assert s.count(old) == count, (s.count(old), old[:70])
    s = s.replace(old, new)


rep('''    private int sameRetries = 0;''',
    '''    private int sameRetries = 0;
    /** Ağ olayı yüzünden kaç kez yeniden bağlanıldı (oynak bağlantı koruması). */
    private int netReconnects = 0;
    /** Yayın hiç hazır oldu mu (ölü kanal zaman aşımı). */
    private boolean everPlayed = false;
    /** Bu kanal için ilk oynatma denemesinin başladığı an. */
    private long tryStartedAt = 0;''')

rep('''        ready = true;
        progress.setVisibility(View.GONE);''',
    '''        ready = true;
        everPlayed = true;   // M-99: ilk kare geldi, bu kanal ölü sayılmayacak
        progress.setVisibility(View.GONE);''')

rep('''        if (sameRetries > 0) showNotice("✅ Bağlantı iyi · yayın açıldı");
        sameRetries = 0;''',
    '''        if (sameRetries > 0) showNotice("✅ Bağlantı iyi · yayın açıldı");
        sameRetries = 0;
        netReconnects = 0;''')

rep('''        sameRetries = 0;
        tracking = true;
        openedAt = System.currentTimeMillis();''',
    '''        sameRetries = 0;
        // M-99: sayaçlar her denemede sıfırlandığı için "kaç kez denendi" mantığı sonsuz
        // döngüye açıktı; ölü kanal kararı artık zamana bağlı.
        tryStartedAt = System.currentTimeMillis();
        everPlayed = false;
        tracking = true;
        openedAt = System.currentTimeMillis();''')

rep('''                                if (ok) return;
                                sameRetries = 0;
                                errorView.setVisibility(View.GONE);''',
    '''                                if (ok) return;
                                // M-99: ağ her duyurulduğunda retry sayacı sıfırlanıyordu;
                                // oynak bağlantıda "yeniden bağlanıyorum" sonsuza dönüyordu.
                                if (++netReconnects > 3) {
                                    markDead("Bağlantı kurulamadı");
                                    return;
                                }
                                errorView.setVisibility(View.GONE);''')

rep('''                if (!player.isPlaying()) {
                    // Zayıf internet: kanalı "ölü" saymadan önce aynı yayına 3 kez daha bağlan.''',
    '''                if (!player.isPlaying()) {
                    if (giveUpIfDead()) return;
                    // Zayıf internet: kanalı "ölü" saymadan önce aynı yayına 2 kez daha bağlan.''')

rep('''                    if (sameRetries < 3) {
                        sameRetries++;
                        final String cur = list.get(index).url;''',
    '''                    if (sameRetries < 2) {
                        sameRetries++;
                        final String cur = list.get(index).url;''')

# watchdog'un vazgeçme dalı: artık tek yerden markDead
start = [i for i, l in enumerate(s.split('\n')) if 'Yayın açılmadı (bağlantı zayıf olabilir)' in l]
assert len(start) == 1, start
lines = s.split('\n')
i = start[0]
ind = ' ' * 20
lines[i:i + 4] = [
    ind + '// M-99: buradan vazgeçen kanal hiç "başarısız" işaretlenmiyordu.',
    ind + 'markDead("Bağlantı kurulamadı");',
    ind + 'return;',
]
s = '\n'.join(lines)

rep('''    private void checkStall() {
        try {''',
    '''    /**
     * M-99: kanalı ölü say — hata işaretle, hata ekranını göster, gerekiyorsa sonrakine geç.
     * Tek yerden yapılıyor ki dört ayrı "yeniden bağlanıyorum" yolu farklı davranmasın.
     */
    private void markDead(String neden) {
        try { Prefs.addFail(this, list.get(index).url); } catch (Exception ignored) { }
        try {
            progress.setVisibility(View.GONE);
            errorView.setText("Bu kanal şu an açılamadı." + BS + "n" + BS + "n" + neden
                    + " · Dokun → tekrar dene" + BS + "nveya ⏭ ile sonraki kanal");
            errorView.setVisibility(View.VISIBLE);
        } catch (Exception ignored) { }
        autoNext();
    }

    /**
     * M-99: 45 sn içinde hiç hazır olmadıysa kanalı ölü say. Sunucusu tamamen erişilemez
     * kanallar onPlayerError üretmediği için eskiden sonsuz "yeniden bağlanıyorum"
     * çemberinde kalıyor, hiç başarısız işaretlenmiyor ve "ölü kanalları onar" hiç
     * devreye giremiyordu.
     */
    private boolean giveUpIfDead() {
        if (everPlayed || tryStartedAt <= 0) return false;
        if (System.currentTimeMillis() - tryStartedAt < 45000) return false;
        markDead("Zaman aşımı");
        return true;
    }

    private void checkStall() {
        try {
            // Erken dönüşlerden (oynatıcı IDLE/BUFFERING) önce bakılmalı.
            if (giveUpIfDead()) return;''')

io.open(p, 'w', encoding='utf-8', newline='').write(
    s.replace('\n', '\r\n') if crlf else s)
print('PlayerActivity yamalandi')
