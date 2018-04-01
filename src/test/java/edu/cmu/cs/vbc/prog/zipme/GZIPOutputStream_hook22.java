// DerivativeCompressGZIPCRC

package edu.cmu.cs.vbc.prog.zipme;


class GZIPOutputStream_hook22 {

    protected GZIPOutputStream _this;
    protected byte[] gzipFooter;
    protected int crcval;

    GZIPOutputStream_hook22(GZIPOutputStream _this, byte[] gzipFooter) {
        this._this = _this;
        this.gzipFooter = gzipFooter;
    }

    void execute__before__DerivativeCompressGZIPCRC() {
    }

    void execute__role__DerivativeCompressGZIPCRC() {
        crcval = (int) (_this.crc.getValue() & 0xffffffff);
        gzipFooter[0] = (byte) crcval;
        gzipFooter[1] = (byte) (crcval >> 8);
        gzipFooter[2] = (byte) (crcval >> 16);
        gzipFooter[3] = (byte) (crcval >> 24);
        execute__before__DerivativeCompressGZIPCRC();
    }

    void execute() {
        if (FeatureSwitches.__SELECTED_FEATURE_DerivativeCompressGZIPCRC) {
            execute__role__DerivativeCompressGZIPCRC();
        } else {
            execute__before__DerivativeCompressGZIPCRC();
        }
    }

}
