package edu.cmu.cs.vbc.prog.zipme;

class Deflater_deflate2 {

    protected Deflater _this;
    protected byte[] output;
    protected int offset;
    protected int length;
    protected int origLength;
    protected int header;
    protected int level_flags;
    protected int chksum;
    protected int count;
    protected int neededbits;
    protected int adler;

    Deflater_deflate2(Deflater _this, byte[] output, int offset, int length) {
        this._this = _this;
        this.output = output;
        this.offset = offset;
        this.length = length;
    }

    int execute() {
        origLength = length;
        if (_this.state == Deflater.CLOSED_STATE)
            throw new IllegalStateException("Deflater closed");
        if (_this.state < Deflater.BUSY_STATE) {
            header = (Deflater.DEFLATED + ((DeflaterConstants.MAX_WBITS - 8) << 4)) << 8;
            level_flags = (_this.level - 1) >> 1;
            if (level_flags < 0 || level_flags > 3)
                level_flags = 3;
            header |= level_flags << 6;
            if ((_this.state & Deflater.IS_SETDICT) != 0)
                header |= DeflaterConstants.PRESET_DICT;
            header += 31 - (header % 31);
            _this.pending.writeShortMSB(header);
            if ((_this.state & Deflater.IS_SETDICT) != 0) {
                this.hook22();
            }
            _this.state = Deflater.BUSY_STATE | (_this.state & (Deflater.IS_FLUSHING | Deflater.IS_FINISHING));
        }
        for (; ; ) {
            count = _this.pending.flush(output, offset, length);
            offset += count;
            _this.totalOut += count;
            length -= count;
            if (length == 0 || _this.state == Deflater.FINISHED_STATE)
                break;
            if (!_this.engine.deflate((_this.state & Deflater.IS_FLUSHING) != 0, (_this.state & Deflater.IS_FINISHING) != 0)) {
                if (_this.state == Deflater.BUSY_STATE) {
                    return origLength - length;
                } else if (_this.state == Deflater.FLUSHING_STATE) {
                    if (_this.level != Deflater.NO_COMPRESSION) {
                        neededbits = 8 + ((-_this.pending.getBitCount()) & 7);
                        while (neededbits > 0) {
                            _this.pending.writeBits(2, 10);
                            neededbits -= 10;
                        }
                    }
                    _this.state = Deflater.BUSY_STATE;
                } else if (_this.state == Deflater.FINISHING_STATE) {
                    _this.pending.alignToByte();
                    if (!_this.noHeader) {
                        this.hook23();
                    }
                    _this.state = Deflater.FINISHED_STATE;
                }
            }
        }
        return origLength - length;
    }

    protected void hook22__before__DerivativeCompressAdler32Checksum() {
    }

    protected void hook22__role__DerivativeCompressAdler32Checksum() {
        chksum = _this.engine.getAdler();
        _this.engine.resetAdler();
        _this.pending.writeShortMSB(chksum >> 16);
        _this.pending.writeShortMSB(chksum & 0xffff);
        hook22__before__DerivativeCompressAdler32Checksum();
    }

    protected void hook22() {
        if (FeatureSwitches.__SELECTED_FEATURE_DerivativeCompressAdler32Checksum) {
            hook22__role__DerivativeCompressAdler32Checksum();
        } else {
            hook22__before__DerivativeCompressAdler32Checksum();
        }
    }

    protected void hook23__before__DerivativeCompressAdler32Checksum() {
    }

    protected void hook23__role__DerivativeCompressAdler32Checksum() {
        adler = _this.engine.getAdler();
        _this.pending.writeShortMSB(adler >> 16);
        _this.pending.writeShortMSB(adler & 0xffff);
        hook23__before__DerivativeCompressAdler32Checksum();
    }

    protected void hook23() {
        if (FeatureSwitches.__SELECTED_FEATURE_DerivativeCompressAdler32Checksum) {
            hook23__role__DerivativeCompressAdler32Checksum();
        } else {
            hook23__before__DerivativeCompressAdler32Checksum();
        }
    }

}
