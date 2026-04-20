package com.grunka.random.fortuna.entropy;

import com.grunka.random.fortuna.Util;
import com.grunka.random.fortuna.accumulator.EventAdder;

import java.time.Duration;

public final class SchedulingEntropySource extends AbstractEntropySource {
    private long lastTime = 0;

    public SchedulingEntropySource() {
        this(Duration.ofMillis(10));
    }

    public SchedulingEntropySource(Duration refreshRate) {
        super(refreshRate);
    }

    @Override
    public void event(EventAdder adder) {
        long now = System.nanoTime();
        long elapsed = now - lastTime;
        lastTime = now;
        adder.add(Util.twoLeastSignificantBytes(elapsed));
    }
}
