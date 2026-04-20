package com.grunka.random.fortuna.entropy;

import com.grunka.random.fortuna.accumulator.EventAdder;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;

public final class URandomEntropySource extends AbstractEntropySource {
    private final byte[] bytes = new byte[32];

    public URandomEntropySource() {
        this(Duration.ofMillis(100));
    }

    public URandomEntropySource(Duration refreshRate) {
        super(refreshRate);
    }

    @Override
    public void event(EventAdder adder) {
        try {
            try (FileInputStream inputStream = new FileInputStream("/dev/urandom")) {
                int bytesRead = inputStream.read(bytes);
                assert bytesRead == bytes.length;
                adder.add(bytes);
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException("Could not open /dev/urandom", e);
        }
    }
}
