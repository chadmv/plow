package com.breakersoft.plow;

public final class FrameRange {

    final public FrameSet frameSet;
    final public int chunkSize;
    final public int numFrames;

    public FrameRange(String range, int chunkSize) {
        this(new FrameSet(range), chunkSize);
    }

    public FrameRange(FrameSet frameSet, int chunkSize) {
        this.frameSet = frameSet;

        // Only place we need to handle chunk size < 0.
        if (chunkSize <=0) {
            this.chunkSize = frameSet.size();
        }
        else {
            this.chunkSize = chunkSize;
        }

        this.numFrames = this.frameSet.size() / this.chunkSize;
    }
}
