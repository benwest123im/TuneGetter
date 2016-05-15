package com.domain.pmzj.singsmart;

/**
 * Created by Martin on 14. 05. 2016.
 */
public class OnsetPitchPair {
    public long onsetTimeMs;
    public double pitchInHz;

    public OnsetPitchPair(long onsetTimeMs, double pitchInHz) {
        this.onsetTimeMs = onsetTimeMs;
        this.pitchInHz = pitchInHz;
    }

    public String toString() @Override {
        return onsetTimeMs + ": " + pitchInHz;
    }
}
