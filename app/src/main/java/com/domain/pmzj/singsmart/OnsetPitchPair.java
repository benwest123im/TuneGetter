package com.domain.pmzj.singsmart;

/**
 * Created by Martin on 14. 05. 2016.
 */
public class OnsetPitchPair {
    public long onsetTimeMs;
    public double pitch;

    public OnsetPitchPair(long onsetTimeMs, double pitch) {
        this.onsetTimeMs = onsetTimeMs;
        this.pitch = pitch;
    }
}
