package com.grunka.random.fortuna.entropy;

import com.grunka.random.fortuna.Util;
import com.grunka.random.fortuna.accumulator.EventAdder;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.util.List;

public final class MemoryPoolEntropySource extends AbstractEntropySource {

    public MemoryPoolEntropySource() {
        this(Duration.ofSeconds(5));
    }

    public MemoryPoolEntropySource(Duration refreshRate) {
        super(refreshRate);
    }

    @Override
    public void event(EventAdder adder) {
        long sum = 0;
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            if (memoryPoolMXBean.isValid()) {
                MemoryUsage usage = memoryPoolMXBean.getUsage();
                if (usage != null) {
                    sum += usage.getUsed();
                }
                MemoryUsage collectionUsage = memoryPoolMXBean.getCollectionUsage();
                if (collectionUsage != null) {
                    sum += collectionUsage.getUsed();
                }
            }
        }
        adder.add(Util.twoLeastSignificantBytes(sum));
    }
}
