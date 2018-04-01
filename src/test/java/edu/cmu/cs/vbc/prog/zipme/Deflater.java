//

package edu.cmu.cs.vbc.prog.zipme;

public class Deflater {

    /**
     * The best and slowest compression level. This tries to find very
     * long and distant string repetitions.
     */
    public static final int BEST_COMPRESSION = 9;

    /**
     * The worst but fastest compression level.
     */
    public static final int BEST_SPEED = 1;

    /**
     * The default compression level.
     */
    public static final int DEFAULT_COMPRESSION = -1;

    /**
     * This level won't compress at all but output uncompressed blocks.
     */
    public static final int NO_COMPRESSION = 0;

    /**
     * The default strategy.
     */
    public static final int DEFAULT_STRATEGY = 0;

    /**
     * This strategy will only allow longer string repetitions. It is
     * useful for random data with a small character set.
     */
    public static final int FILTERED = 1;

    /**
     * This strategy will not look for string repetitions at all. It
     * only encodes with Huffman trees (which means, that more common
     * characters get a smaller encoding.
     */
    public static final int HUFFMAN_ONLY = 2;

    /**
     * The compression method. This is the only method supported so far.
     * There is no need to use this constant at all.
     */
    public static final int DEFLATED = 8;

    public static final int IS_SETDICT = 0x01;

    public static final int IS_FLUSHING = 0x04;

    public static final int IS_FINISHING = 0x08;
    public static final int BUSY_STATE = 0x10;
    public static final int FLUSHING_STATE = 0x14;
    public static final int FINISHING_STATE = 0x1c;
    public static final int FINISHED_STATE = 0x1e;
    public static final int CLOSED_STATE = 0x7f;
    private static final int INIT_STATE = 0x00;
    private static final int SETDICT_STATE = 0x01;
    private static final int INIT_FINISHING_STATE = 0x08;
    private static final int SETDICT_FINISHING_STATE = 0x09;
    /**
     * Compression level.
     */
    public int level;

    /**
     * should we include a header.
     */
    public boolean noHeader;

    /**
     * The current state.
     */
    public int state;

    /**
     * The total bytes of output written.
     */
    public long totalOut;

    /**
     * The pending output.
     */
    public DeflaterPending pending;

    /**
     * The deflater engine.
     */
    public DeflaterEngine engine;

    /**
     * Creates a new deflater with default compression level.
     */
    public Deflater() {
        this(DEFAULT_COMPRESSION, false);
    }

    /**
     * Creates a new deflater with given compression level.
     *
     * @param lvl the compression level, a value between NO_COMPRESSION
     *            and BEST_COMPRESSION, or DEFAULT_COMPRESSION.
     * @throws IllegalArgumentException if lvl is out of range.
     */
    public Deflater(int lvl) {
        this(lvl, false);
    }

    /**
     * Creates a new deflater with given compression level.
     *
     * @param lvl    the compression level, a value between NO_COMPRESSION
     *               and BEST_COMPRESSION.
     * @param nowrap true, iff we should suppress the deflate header at the
     *               beginning and the adler checksum at the end of the output. This is
     *               useful for the GZIP format.
     * @throws IllegalArgumentException if lvl is out of range.
     */
    public Deflater(int lvl, boolean nowrap) {
        if (lvl == DEFAULT_COMPRESSION)
            lvl = 6;
        else if (lvl < NO_COMPRESSION || lvl > BEST_COMPRESSION)
            throw new IllegalArgumentException();
        pending = new DeflaterPending();
        this.hook25();
        this.noHeader = nowrap;
        this.hook24(lvl);
        reset();
    }

    /**
     * Resets the deflater. The deflater acts afterwards as if it was
     * just created with the same compression level and strategy as it
     * had before.
     */
    public void reset__before__Compress() {
        state = (noHeader ? BUSY_STATE : INIT_STATE);
        totalOut = 0;
        pending.reset();
    }

    /**
     * Resets the deflater. The deflater acts afterwards as if it was
     * just created with the same compression level and strategy as it
     * had before.
     */
    public void reset__role__Compress() {
        reset__before__Compress();
        engine.reset();
    }

    /**
     * Resets the deflater. The deflater acts afterwards as if it was
     * just created with the same compression level and strategy as it
     * had before.
     */
    public void reset() {
        if (FeatureSwitches.__SELECTED_FEATURE_Compress) {
            reset__role__Compress();
        } else {
            reset__before__Compress();
        }
    }

    /**
     * Frees all objects allocated by the compressor. There's no
     * reason to call this, since you can just rely on garbage
     * collection. Exists only for compatibility against Sun's JDK,
     * where the compressor allocates native memory.
     * If you call any method (even reset) afterwards the behaviour is
     * <i>undefined</i>.
     */
    public void end__before__Compress() {
        pending = null;
        state = CLOSED_STATE;
    }

    /**
     * Frees all objects allocated by the compressor. There's no
     * reason to call this, since you can just rely on garbage
     * collection. Exists only for compatibility against Sun's JDK,
     * where the compressor allocates native memory.
     * If you call any method (even reset) afterwards the behaviour is
     * <i>undefined</i>.
     */
    public void end__role__Compress() {
        engine = null;
        end__before__Compress();
    }

    /**
     * Frees all objects allocated by the compressor. There's no
     * reason to call this, since you can just rely on garbage
     * collection. Exists only for compatibility against Sun's JDK,
     * where the compressor allocates native memory.
     * If you call any method (even reset) afterwards the behaviour is
     * <i>undefined</i>.
     */
    public void end() {
        if (FeatureSwitches.__SELECTED_FEATURE_Compress) {
            end__role__Compress();
        } else {
            end__before__Compress();
        }
    }

    /**
     * Gets the number of output bytes so far.
     */
    public int getTotalOut() {
        return (int) totalOut;
    }

    protected void hook24__before__Compress(int lvl) {
    }

    protected void hook24__role__Compress(int lvl) {
        setStrategy(DEFAULT_STRATEGY);
        setLevel(lvl);
        hook24__before__Compress(lvl);
    }

    protected void hook24(int lvl) {
        if (FeatureSwitches.__SELECTED_FEATURE_Compress) {
            hook24__role__Compress(lvl);
        } else {
            hook24__before__Compress(lvl);
        }
    }

    protected void hook25__before__Compress() {
    }

    protected void hook25__role__Compress() {
        engine = new DeflaterEngine(pending);
        hook25__before__Compress();
    }

    protected void hook25() {
        if (FeatureSwitches.__SELECTED_FEATURE_Compress) {
            hook25__role__Compress();
        } else {
            hook25__before__Compress();
        }
    }

    /**
     * Gets the number of input bytes processed so far.
     */
    public int getTotalIn() {
        return (int) engine.getTotalIn();
    }

    /**
     * Gets the number of input bytes processed so far.
     *
     * @since 1.5
     */
    public long getBytesRead() {
        return engine.getTotalIn();
    }

    /**
     * Gets the number of output bytes so far.
     *
     * @since 1.5
     */
    public long getBytesWritten() {
        return totalOut;
    }

    /**
     * Flushes the current input block. Further calls to deflate() will
     * produce enough output to inflate everything in the current input
     * block. This is not part of Sun's JDK so I have made it package
     * private. It is used by DeflaterOutputStream to implement
     * flush().
     */
    void flush() {
        state |= IS_FLUSHING;
    }

    /**
     * Finishes the deflater with the current input block. It is an error
     * to give more input after this method was called. This method must
     * be called to force all bytes to be flushed.
     */
    public void finish() {
        state |= IS_FLUSHING | IS_FINISHING;
    }

    /**
     * Returns true iff the stream was finished and no more output bytes
     * are available.
     */
    public boolean finished() {
        return state == FINISHED_STATE && pending.isFlushed();
    }

    /**
     * Returns true, if the input buffer is empty.
     * You should then call setInput(). <br>
     * <em>NOTE</em>: This method can also return true when the stream
     * was finished.
     */
    public boolean needsInput() {
        return engine.needsInput();
    }

    /**
     * Sets the data which should be compressed next. This should be only
     * called when needsInput indicates that more input is needed.
     * If you call setInput when needsInput() returns false, the
     * previous input that is still pending will be thrown away.
     * The given byte array should not be changed, before needsInput() returns
     * true again.
     * This call is equivalent to <code>setInput(input, 0, input.length)</code>.
     *
     * @param input the buffer containing the input data.
     * @throws IllegalStateException if the buffer was finished() or ended().
     */
    public void setInput(byte[] input) {
        setInput(input, 0, input.length);
    }

    /**
     * Sets the data which should be compressed next. This should be
     * only called when needsInput indicates that more input is needed.
     * The given byte array should not be changed, before needsInput() returns
     * true again.
     *
     * @param input the buffer containing the input data.
     * @param off   the start of the data.
     * @param len   the length of the data.
     * @throws IllegalStateException if the buffer was finished() or ended()
     *                               or if previous input is still pending.
     */
    public void setInput(byte[] input, int off, int len) {
        if ((state & IS_FINISHING) != 0)
            throw new IllegalStateException("finish()/end() already called");
        engine.setInput(input, off, len);
    }

    /**
     * Sets the compression level. There is no guarantee of the exact
     * position of the change, but if you call this when needsInput is
     * true the change of compression level will occur somewhere near
     * before the end of the so far given input.
     *
     * @param lvl the new compression level.
     */
    public void setLevel(int lvl) {
        if (lvl == DEFAULT_COMPRESSION)
            lvl = 6;
        else if (lvl < NO_COMPRESSION || lvl > BEST_COMPRESSION)
            throw new IllegalArgumentException();
        if (level != lvl) {
            level = lvl;
            engine.setLevel(lvl);
        }
    }

    /**
     * Sets the compression strategy. Strategy is one of
     * DEFAULT_STRATEGY, HUFFMAN_ONLY and FILTERED. For the exact
     * position where the strategy is changed, the same as for
     * setLevel() applies.
     *
     * @param stgy the new compression strategy.
     */
    public void setStrategy(int stgy) {
        if (stgy != DEFAULT_STRATEGY && stgy != FILTERED && stgy != HUFFMAN_ONLY)
            throw new IllegalArgumentException();
        engine.setStrategy(stgy);
    }

    /**
     * Deflates the current input block to the given array. It returns
     * the number of bytes compressed, or 0 if either
     * needsInput() or finished() returns true or length is zero.
     *
     * @param output the buffer where to write the compressed data.
     */
    public int deflate(byte[] output) {
        return deflate(output, 0, output.length);
    }

    /**
     * Deflates the current input block to the given array. It returns
     * the number of bytes compressed, or 0 if either
     * needsInput() or finished() returns true or length is zero.
     *
     * @param output the buffer where to write the compressed data.
     * @param offset the offset into the output array.
     * @param length the maximum number of bytes that may be written.
     * @throws IllegalStateException     if end() was called.
     * @throws IndexOutOfBoundsException if offset and/or length
     *                                   don't match the array length.
     */
    public int deflate(byte[] output, int offset, int length) {
        int x = new Deflater_deflate2(this, output, offset, length).execute();
        return x;
    }

    /**
     * Sets the dictionary which should be used in the deflate process.
     * This call is equivalent to <code>setDictionary(dict, 0,
     * dict.length)</code>.
     *
     * @param dict the dictionary.
     * @throws IllegalStateException if setInput () or deflate ()
     *                               were already called or another dictionary was already set.
     */
    public void setDictionary(byte[] dict) {
        setDictionary(dict, 0, dict.length);
    }

    /**
     * Sets the dictionary which should be used in the deflate process.
     * The dictionary should be a byte array containing strings that are
     * likely to occur in the data which should be compressed. The
     * dictionary is not stored in the compressed output, only a
     * checksum. To decompress the output you need to supply the same
     * dictionary again.
     *
     * @param dict   the dictionary.
     * @param offset an offset into the dictionary.
     * @param length the length of the dictionary.
     * @throws IllegalStateException if setInput () or deflate () were
     *                               already called or another dictionary was already set.
     */
    public void setDictionary(byte[] dict, int offset, int length) {
        if (state != INIT_STATE)
            throw new IllegalStateException();
        state = SETDICT_STATE;
        engine.setDictionary(dict, offset, length);
    }

    /**
     * Gets the current adler checksum of the data that was processed so
     * far.
     */
    public int getAdler() {
        return engine.getAdler();
    }

}
