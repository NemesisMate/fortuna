package com.grunka.random.fortuna.entropy;

import com.grunka.random.fortuna.accumulator.EntropySource;

import java.time.Duration;

public abstract class AbstractEntropySource implements EntropySource {
    private final Duration refreshRate;

    public AbstractEntropySource(Duration refreshRate) {
        this.refreshRate = refreshRate;
    }

    @Override
    public Duration getRefreshRate() {
        return refreshRate;
    }
}
