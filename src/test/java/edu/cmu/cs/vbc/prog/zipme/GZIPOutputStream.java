// DerivativeCompressGZIPCRC

package edu.cmu.cs.vbc.prog.zipme;

import java.io.IOException;
import java.io.OutputStream;

public class GZIPOutputStream extends DeflaterOutputStream {

    /**
     * CRC-32 value for uncompressed data
     */
    protected CRC32 crc;

    /**
     * Creates a GZIPOutputStream with the default buffer size
     *
     * @param out The stream to read data (to be compressed) from
     */
    public GZIPOutputStream(OutputStream out) throws IOException {
        this(out, 4096);
    }

    /**
     * Creates a GZIPOutputStream with the specified buffer size
     *
     * @param out  The stream to read compressed data from
     * @param size Size of the buffer to use
     */
    public GZIPOutputStream(OutputStream out, int size) throws IOException {
        super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true), size);
        hook();
        int mod_time = (int) (System.currentTimeMillis() / 1000L);
        byte[] gzipHeader = {(byte) GZIPInputStream.GZIP_MAGIC, (byte) (GZIPInputStream.GZIP_MAGIC >> 8), (byte) Deflater.DEFLATED, 0, (byte) mod_time, (byte) (mod_time >> 8),
                (byte) (mod_time >> 16), (byte) (mod_time >> 24), 0, (byte) 255};
        out.write(gzipHeader);
    }

    public void hook__before__DerivativeCompressGZIPCRC() {
    }

    public void hook__role__DerivativeCompressGZIPCRC() {
        crc = new CRC32();
        hook__before__DerivativeCompressGZIPCRC();
    }

    public void hook() {
        if (FeatureSwitches.__SELECTED_FEATURE_DerivativeCompressGZIPCRC) {
            hook__role__DerivativeCompressGZIPCRC();
        } else {
            hook__before__DerivativeCompressGZIPCRC();
        }
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        super.write(buf, off, len);
        this.hook31(buf, off, len);
    }

    /**
     * Writes remaining compressed output data to the output stream
     * and closes it.
     */
    public void close() throws IOException {
        finish();
        out.close();
    }

    public void finish() throws IOException {
        super.finish();
        int totalin = def.getTotalIn();
        byte[] gzipFooter = new byte[8];
        hook2(gzipFooter);
        gzipFooter[4] = (byte) totalin;
        gzipFooter[5] = (byte) (totalin >> 8);
        gzipFooter[6] = (byte) (totalin >> 16);
        gzipFooter[7] = (byte) (totalin >> 24);
        out.write(gzipFooter);
    }

    private void hook2(byte[] gzipFooter) {
        new GZIPOutputStream_hook22(this, gzipFooter).execute();
    }

    protected void hook31__before__DerivativeCompressGZIPCRC(byte[] buf, int off, int len) throws IOException {
    }

    protected void hook31__role__DerivativeCompressGZIPCRC(byte[] buf, int off, int len) throws IOException {
        crc.update(buf, off, len);
        hook31__before__DerivativeCompressGZIPCRC(buf, off, len);
    }

    protected void hook31(byte[] buf, int off, int len) throws IOException {
        if (FeatureSwitches.__SELECTED_FEATURE_DerivativeCompressGZIPCRC) {
            hook31__role__DerivativeCompressGZIPCRC(buf, off, len);
        } else {
            hook31__before__DerivativeCompressGZIPCRC(buf, off, len);
        }
    }

}
