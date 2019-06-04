package xyz.phanta.aquinasmc.client.model;

import javax.annotation.Nullable;

class DXModelSeq {

    private final String name;
    private final int startFrame, endFrame;
    private final long frameDuration;
    @Nullable
    private final String nextSequence;

    DXModelSeq(String name, int startFrame, int frames, double rate, @Nullable String nextSequence) {
        this.name = name;
        this.startFrame = startFrame;
        this.endFrame = startFrame + frames;
        this.frameDuration = (long)Math.floor(1000D / rate);
        this.nextSequence = nextSequence;
    }

    String getName() {
        return name;
    }

    int getStartFrame() {
        return startFrame;
    }

    int getEndFrame() {
        return endFrame;
    }

    long getFrameDuration() {
        return frameDuration;
    }

    @Nullable
    String getNextSequence() {
        return nextSequence;
    }

}
