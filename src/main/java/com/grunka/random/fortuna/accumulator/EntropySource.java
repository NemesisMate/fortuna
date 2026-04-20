package com.grunka.random.fortuna.accumulator;

import java.time.Duration;

public interface EntropySource {
    Duration getRefreshRate();

    void event(EventAdder adder);
}
