package com.cengiz.canlitv;

import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSpec;

/**
 * Yayın verisini gerçekten sayan DataSource sarmalayıcı.
 *
 * Neden: eskiden tüketim TrafficStats ile ölçülüyordu; bu yöntem cihazda 0 dönebiliyor,
 * reklam trafiğini de karıştırıyor ve oynatma sürerken ekrandaki sayaç güncellenmiyordu.
 * Artık oynatıcının ağdan okuduğu her bayt burada sayılıyor: ekran, ayarlar ve limit
 * aynı sayıyı gösteriyor.
 */
public final class CountingDataSource implements DataSource {

    public interface Sink {
        void add(long bytes);
    }

    private final DataSource upstream;
    private final Sink sink;

    public CountingDataSource(DataSource upstream, Sink sink) {
        this.upstream = upstream;
        this.sink = sink;
    }

    @Override
    public void addTransferListener(androidx.media3.datasource.TransferListener listener) {
        upstream.addTransferListener(listener);
    }

    @Override
    public long open(DataSpec dataSpec) throws java.io.IOException {
        return upstream.open(dataSpec);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws java.io.IOException {
        int n = upstream.read(buffer, offset, length);
        if (n > 0 && sink != null) sink.add(n);
        return n;
    }

    @Override
    public android.net.Uri getUri() {
        return upstream.getUri();
    }

    @Override
    public java.util.Map<String, java.util.List<String>> getResponseHeaders() {
        return upstream.getResponseHeaders();
    }

    @Override
    public void close() throws java.io.IOException {
        upstream.close();
    }

    /** Sayaçlı fabrika: her yeni kaynak aynı sayaca yazar. */
    public static final class Factory implements DataSource.Factory {
        private final DataSource.Factory upstream;
        private final Sink sink;

        public Factory(DataSource.Factory upstream, Sink sink) {
            this.upstream = upstream;
            this.sink = sink;
        }

        @Override
        public DataSource createDataSource() {
            return new CountingDataSource(upstream.createDataSource(), sink);
        }
    }
}
