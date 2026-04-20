package com.grunka.random.fortuna;

import com.grunka.random.fortuna.accumulator.LocalAccumulator;
import com.grunka.random.fortuna.entropy.BufferPoolEntropySource;
import com.grunka.random.fortuna.entropy.FreeMemoryEntropySource;
import com.grunka.random.fortuna.entropy.GarbageCollectorEntropySource;
import com.grunka.random.fortuna.entropy.LoadAverageEntropySource;
import com.grunka.random.fortuna.entropy.MemoryPoolEntropySource;
import com.grunka.random.fortuna.entropy.SchedulingEntropySource;
import com.grunka.random.fortuna.entropy.ThreadTimeEntropySource;
import com.grunka.random.fortuna.entropy.URandomEntropySource;
import com.grunka.random.fortuna.entropy.UptimeEntropySource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import static com.grunka.random.fortuna.Fortuna.MIN_POOL_SIZE;
import static com.grunka.random.fortuna.Fortuna.POWERS_OF_TWO;
import static com.grunka.random.fortuna.Fortuna.RANDOM_DATA_CHUNK_SIZE;

public final class LocalFortuna extends Random {
    private long lastReseedTime = 0;
    private long reseedCount = 0;
    private final RandomDataBuffer randomDataBuffer;
    private final Generator generator;
    private final LocalAccumulator accumulator;

    public static LocalFortuna createInstance() {
        return new LocalFortuna(createAccumulator());
    }

    public static LocalFortuna createInstance(LocalAccumulator accumulator) {
        return new LocalFortuna(accumulator);
    }

    private LocalFortuna(LocalAccumulator accumulator) {
        this.generator = new Generator();
        this.randomDataBuffer = new RandomDataBuffer();
        this.accumulator = accumulator;
    }

    private static LocalAccumulator createAccumulator() {
        var pools = new Pool[32];
        for (int pool = 0; pool < pools.length; pool++) {
            pools[pool] = new Pool();
        }
        var accumulatorBuilder = LocalAccumulator.builder(pools)
                .addSource(new SchedulingEntropySource())
                .addSource(new GarbageCollectorEntropySource())
                .addSource(new LoadAverageEntropySource())
                .addSource(new FreeMemoryEntropySource())
                .addSource(new ThreadTimeEntropySource())
                .addSource(new UptimeEntropySource())
                .addSource(new BufferPoolEntropySource())
                .addSource(new MemoryPoolEntropySource());

        if (Files.exists(Paths.get("/dev/urandom"))) {
            accumulatorBuilder.addSource(new URandomEntropySource());
        }

        var accumulator = accumulatorBuilder.build();
        while (pools[0].size() < MIN_POOL_SIZE) {
            accumulator.refreshEntropySources();
        }
        return accumulator;
    }

    private byte[] randomData() {
        long now = System.currentTimeMillis();
        Pool[] pools = accumulator.getPools();

        if (pools[0].size() >= MIN_POOL_SIZE) {
            if (now - lastReseedTime > 100) {
                lastReseedTime = now;
                reseedCount++;
                byte[] seed = new byte[pools.length * 32]; // Maximum potential length
                int seedLength = 0;
                for (int pool = 0; pool < pools.length; pool++) {
                    if (reseedCount % POWERS_OF_TWO[pool] == 0) {
                        System.arraycopy(pools[pool].getAndClear(), 0, seed, seedLength, 32);
                        seedLength += 32;
                    }
                }
                generator.reseed(Arrays.copyOf(seed, seedLength));
            }
        } else {
            accumulator.refreshEntropySources(now);
        }
        return generator.pseudoRandomData(RANDOM_DATA_CHUNK_SIZE);
    }

    @Override
    protected int next(int bits) {
        return randomDataBuffer.next(bits, this::randomData);
    }

    @Override
    public synchronized void setSeed(long seed) {
        // Does not do anything
    }

    @Override
    public String toString() {
        return "LocalFortuna{" +
                "accumulator=" + accumulator +
                '}';
    }

    //    @Override
//    public String toString() {
//
//        return getClass().getName() + "[" +  + "]";
//    }
}
