package com.grunka.random.fortuna.entropy;

import com.grunka.random.fortuna.Util;
import com.grunka.random.fortuna.accumulator.EventAdder;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Duration;

public final class ThreadTimeEntropySource extends AbstractEntropySource {

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    public ThreadTimeEntropySource() {
        this(Duration.ofMillis(100));
    }

    public ThreadTimeEntropySource(Duration refreshRate) {
        super(refreshRate);
    }

    @Override
    public void event(EventAdder adder) {
        long threadTime = threadMXBean.getCurrentThreadCpuTime() + threadMXBean.getCurrentThreadUserTime();
        adder.add(Util.twoLeastSignificantBytes(threadTime));
    }
}
