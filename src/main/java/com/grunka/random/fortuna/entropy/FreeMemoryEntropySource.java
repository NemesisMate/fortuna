package com.grunka.random.fortuna.entropy;

import com.grunka.random.fortuna.Util;
import com.grunka.random.fortuna.accumulator.EventAdder;

import java.time.Duration;

public final class FreeMemoryEntropySource extends AbstractEntropySource {

    public FreeMemoryEntropySource() {
        this(Duration.ofMillis(100));
    }

    public FreeMemoryEntropySource(Duration refreshRate) {
        super(refreshRate);
    }

    @Override
    public void event(EventAdder adder) {
        long freeMemory = Runtime.getRuntime().freeMemory();
        adder.add(Util.twoLeastSignificantBytes(freeMemory));
    }
}
