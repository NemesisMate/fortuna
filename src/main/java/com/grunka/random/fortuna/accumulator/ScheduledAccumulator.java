package com.grunka.random.fortuna.accumulator;

import com.grunka.random.fortuna.Pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ScheduledAccumulator implements Accumulator {
    private final AtomicInteger sourceCount = new AtomicInteger(0);
    private final List<ScheduledFuture<?>> entropyFutures = new ArrayList<>();
    private final Pool[] pools;
    private final ScheduledExecutorService scheduler;

    public ScheduledAccumulator(Pool[] pools, ScheduledExecutorService scheduler) {
        this.pools = pools;
        this.scheduler = scheduler;
    }

    @Override
    public Pool[] getPools() {
        return pools;
    }

    public void addSource(EntropySource entropySource) {
        int sourceId = sourceCount.getAndIncrement();
        EventAdder eventAdder = new EventAdderImpl(sourceId, pools);

        var refreshRate = entropySource.getRefreshRate();
        entropyFutures.add(scheduler.scheduleWithFixedDelay(() -> entropySource.event(eventAdder), 0, refreshRate.toMillis(), TimeUnit.MILLISECONDS));
    }

    public void shutdownSources() {
        entropyFutures.forEach(f -> f.cancel(false));
        entropyFutures.clear();
    }
}
