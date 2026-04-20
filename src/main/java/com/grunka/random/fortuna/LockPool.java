package com.grunka.random.fortuna;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class LockPool extends Pool {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    @Override
    long size() {
        readLock.lock();
        try {
            return super.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void add(int source, byte[] event) {
        writeLock.lock();
        try {
            super.add(source, event);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    byte[] getAndClear() {
        writeLock.lock();
        try {
            return super.getAndClear();
        } finally {
            writeLock.unlock();
        }
    }
}
