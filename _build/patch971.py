"""9.71 yamaları: reklam sayacı (gösterim anında say, test modu, günlük) + M-99 ölü kanal."""
import io

BS = chr(92)


def load(p):
    raw = io.open(p, encoding='utf-8', newline='').read()
    return raw, ('\r\n' in raw)


def save(p, s, crlf):
    io.open(p, 'w', encoding='utf-8', newline='').write(
        s.replace('\n', '\r\n') if crlf else s)


def rep(s, old, new, count=1):
    assert s.count(old) == count, (s.count(old), old[:70])
    return s.replace(old, new)


# ---------------- AdManager: gösterim anında say ----------------
p = 'src/com/cengiz/canlitv/AdManager.java'
raw, crlf = load(p)
s = raw.replace('\r\n', '\n')

# 1) geçiş reklamı: show() anında değil, gerçek gösterimde say
s = rep(s, '''            AdStats.note(activity, "gecis");
''', '', 2)

s = rep(s, '''        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                showingAd = false;
                loadInterstitial(activity);
                then.run();
            }''',
        '''        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            // Sayaç yalnız gerçekten gösterildiğinde artar: eskiden ad.show() çağrısında
            // sayılıyordu, doluluk yokken/uygulama öndeyken de "reklam gösterildi" yazıyordu.
            @Override public void onAdImpression() { AdStats.note(activity, "gecis"); }

            @Override public void onAdDismissedFullScreenContent() {
                showingAd = false;
                loadInterstitial(activity);
                then.run();
            }''')

s = rep(s, '''        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                showingAd = false;
                loadInterstitial(activity);
            }''',
        '''        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdImpression() { AdStats.note(activity, "gecis"); }

            @Override public void onAdDismissedFullScreenContent() {
                showingAd = false;
                loadInterstitial(activity);
            }''')

# 2) ödüllü: ödül gerçekten kazanılınca say (erken kapatınca para yok)
s = rep(s, '''        try {
            AdStats.note(activity, "odullu");
            ad.show(activity, new com.google.android.gms.ads.OnUserEarnedRewardListener() {
                @Override public void onUserEarnedReward(
                        com.google.android.gms.ads.rewarded.RewardItem item) {
                    long until = System.currentTimeMillis() + Ads.REWARD_MINUTES * 60000L;''',
        '''        try {
            ad.show(activity, new com.google.android.gms.ads.OnUserEarnedRewardListener() {
                @Override public void onUserEarnedReward(
                        com.google.android.gms.ads.rewarded.RewardItem item) {
                    // Ödül gerçekten kazanıldı: erken kapatılan ödüllü reklam gelir yazmaz.
                    AdStats.note(activity, "odullu");
                    long until = System.currentTimeMillis() + Ads.REWARD_MINUTES * 60000L;''')
save(p, s, crlf)
print('AdManager yamalandi')

# ---------------- PlayerActivity: M-99 ölü kanal ----------------
p = 'src/com/cengiz/canlitv/PlayerActivity.java'
raw, crlf = load(p)
s = raw.replace('\r\n', '\n')

s = rep(s, '''    private int sameRetries = 0;''',
        '''    private int sameRetries = 0;
    /** Ağ olayı yüzünden kaç kez yeniden bağlanıldı (oynak bağlantı koruması). */
    private int netReconnects = 0;
    /** Yayın hiç hazır oldu mu (ölü kanal zaman aşımı). */
    private boolean everPlayed = false;
    /** Bu kanal için ilk oynatma denemesinin başladığı an. */
    private long tryStartedAt = 0;''')

s = rep(s, '''        ready = true;
        progress.setVisibility(View.GONE);''',
        '''        ready = true;
        everPlayed = true;   // M-99: ilk kare geldi, bu kanal ölü sayılmayacak
        progress.setVisibility(View.GONE);''')

s = rep(s, '''        sameRetries = 0;
        netReconnects = 0;
        stallCount = 0;''',
        '''        sameRetries = 0;
        netReconnects = 0;
        stallCount = 0;''')

s = rep(s, '''        sameRetries = 0;
        tracking = true;
        openedAt = System.currentTimeMillis();''',
        '''        sameRetries = 0;
        // M-99: sayaçlar her denemede sıfırlandığı için "kaç kez denendi" mantığı sonsuz
        // döngüye açıktı; ölü kanal kararı artık zamana bağlı.
        tryStartedAt = System.currentTimeMillis();
        everPlayed = false;
        tracking = true;
        openedAt = System.currentTimeMillis();''')

# onReady içinde sayaç sıfırlama
s = rep(s, '''        if (sameRetries > 0) showNotice("✅ Bağlantı iyi · yayın açıldı");
        sameRetries = 0;''',
        '''        if (sameRetries > 0) showNotice("✅ Bağlantı iyi · yayın açıldı");
        sameRetries = 0;
        netReconnects = 0;''')

# ağ geri çağrısı: sayacı sıfırlama, sınırla
s = rep(s, '''                                if (ok) return;
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

# watchdog: ölü kontrolü + vazgeçince addFail
s = rep(s, '''                if (!player.isPlaying()) {
                    // Zayıf internet: kanalı "ölü" saymadan önce aynı yayına 3 kez daha bağlan.''',
        '''                if (!player.isPlaying()) {
                    if (giveUpIfDead()) return;
                    // Zayıf internet: kanalı "ölü" saymadan önce aynı yayına 2 kez daha bağlan.''')

s = rep(s, '''                    if (sameRetries < 3) {
                        sameRetries++;
                        final String cur = list.get(index).url;''',
        '''                    if (sameRetries < 2) {
                        sameRetries++;
                        final String cur = list.get(index).url;''')

s = rep(s, '''                    progress.setVisibility(View.GONE);
                    errorView.setText("Yayın açılmadı (bağlantı zayıf olabilir).''' + BS + '''n''' + BS + '''nDokun → tekrar dene''' + BS + '''nveya ⏭ ile sonraki kanal");
                    errorView.setVisibility(View.VISIBLE);
                    autoNext();''',
        '''                    // M-99: buradan vazgeçen kanal hiç "başarısız" işaretlenmiyordu.
                    markDead("Bağlantı kurulamadı");
                    return;''')

# checkStall: en basta ölü kontrolü (eski yerde erken dönüşler vardı)
s = rep(s, '''    private void checkStall() {
        try {''',
        '''    /**
     * M-99: kanalı ölü say — hata işaretle, hata ekranını göster, gerekiyorsa sonrakine geç.
     * Tek yerden yapılıyor ki dört farklı "yeniden bağlanıyorum" yolu ayrı davranmasın.
     */
    private void markDead(String neden) {
        try { Prefs.addFail(this, list.get(index).url); } catch (Exception ignored) { }
        try {
            progress.setVisibility(View.GONE);
            errorView.setText("Bu kanal şu an açılamadı." + ''' + BS + '''n''' + BS + '''n" + neden
                    + " · Dokun → tekrar dene" + ''' + BS + '''nveya ⏭ ile sonraki kanal");
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
            if (giveUpIfDead()) return;''')
save(p, s, crlf)
print('PlayerActivity yamalandi')
