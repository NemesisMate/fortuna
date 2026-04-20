package com.grunka.random.fortuna.accumulator;

import com.grunka.random.fortuna.Pool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LocalAccumulator implements Accumulator {
    private final Refresher[] entropyRefreshers;
    private final Pool[] pools;

    private LocalAccumulator(Pool[] pools, Refresher[] entropyRefreshers) {
        this.pools = pools;
        this.entropyRefreshers = entropyRefreshers;
    }

    @Override
    public Pool[] getPools() {
        return pools;
    }

    public void refreshEntropySources() {
        for (Refresher entropyRefresher : entropyRefreshers) {
            entropyRefresher.refresh();
        }
    }

    public void refreshEntropySources(long currentMillis) {
        for (Refresher entropyRefresher : entropyRefreshers) {
            entropyRefresher.refresh(currentMillis);
        }
    }

    private static final class Refresher {
        private final EntropySource source;
        private final EventAdder eventAdder;
        private final long refreshRateMillis;
        private long nextRefreshMillis;

        public Refresher(EntropySource source, EventAdder eventAdder) {
            this.source = source;
            this.eventAdder = eventAdder;
            this.refreshRateMillis = source.getRefreshRate().toMillis();
        }

        public void refresh(long currentMillis) {
            if (currentMillis >= nextRefreshMillis) {
                refresh();
                nextRefreshMillis = currentMillis + refreshRateMillis;
            }
        }

        public void refresh() {
            source.event(eventAdder);
        }

        @Override
        public String toString() {
            return "Refresher{" +
                    "refreshRateMillis=" + refreshRateMillis +
                    ", source=" + source +
                    '}';
        }
    }

    public static Builder builder(Pool[] pools) {
        return new Builder(pools);
    }

    public static final class Builder {
        private final Pool[] pools;
        private final List<Refresher> entropyRefreshers = new ArrayList<>();

        public Builder(Pool[] pools) {
            this.pools = pools;
        }

        public Builder addSource(EntropySource entropySource) {
            int sourceId = entropyRefreshers.size();
            EventAdder eventAdder = new EventAdderImpl(sourceId, pools);
            entropyRefreshers.add(new Refresher(entropySource, eventAdder));
            return this;
        }

        public LocalAccumulator build() {
            return new LocalAccumulator(pools, entropyRefreshers.toArray(Refresher[]::new));
        }
    }

    @Override
    public String toString() {
        return "LocalAccumulator{" +
                "entropyRefreshers=" + Arrays.toString(entropyRefreshers) +
                '}';
    }
}
