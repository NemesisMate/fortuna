package com.grunka.random.fortuna;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Pool {
    private final MessageDigest poolDigest = createDigest();
    private long size = 0;

    private MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new Error("Could not initialize digest", e);
        }
    }

    long size() {
        return size;
    }

    public void add(int source, byte[] event) {
        if (source < 0 || source > 255) {
            throw new IllegalArgumentException("Source needs to be in the range 0 to 255, it was " + source);
        }
        if (event.length < 1 || event.length > 32) {
            throw new IllegalArgumentException("The length of the event need to be in the range 1 to 32, it was " + event.length);
        }
        size += event.length + 2;
        poolDigest.update(new byte[]{(byte) source, (byte) event.length});
        poolDigest.update(event);
    }

    byte[] getAndClear() {
        size = 0;
        return poolDigest.digest();
    }
}
