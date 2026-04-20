package com.grunka.random.fortuna.entropy;

import com.grunka.random.fortuna.Util;
import com.grunka.random.fortuna.accumulator.EventAdder;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.List;

public final class BufferPoolEntropySource extends AbstractEntropySource {

    public BufferPoolEntropySource() {
        this(Duration.ofSeconds(5));
    }

    public BufferPoolEntropySource(Duration refreshRate) {
        super(refreshRate);
    }

    @Override
    public void event(EventAdder adder) {
        long sum = 0;
        List<BufferPoolMXBean> bufferPoolMXBeans = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        for (BufferPoolMXBean bufferPoolMXBean : bufferPoolMXBeans) {
            sum += bufferPoolMXBean.getMemoryUsed();
        }
        adder.add(Util.twoLeastSignificantBytes(sum));
    }
}
