package com.grunka.random.fortuna.tests;

import com.grunka.random.fortuna.LocalFortuna;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.StandardOpenOption;
import java.util.stream.IntStream;

public class LocalDump {

    static void main(String[] args) throws Exception {
        if (args.length != 3) {
            usage();
            System.exit(args.length == 0 ? 0 : 1);
        }
        int bs;
        try {
            bs = Integer.parseInt(args[0]) * 1024 * 1024;
        } catch (NumberFormatException e) {
            usage();
            System.err.println("BS Megabytes was not a number: " + args[0]);
            System.exit(2);
            return;
        }

        int count;
        try {
            count = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            usage();
            System.err.println("Count was not a number: " + args[0]);
            System.exit(3);
            return;
        }

        if (bs < 1 || count < 1) {
            usage();
            System.err.println("No data will be generated for bs * count = " + (bs * count));
            System.exit(4);
        }

        File file;
        try {
            file = new File(args[2]);
            if (file.exists()) {
                throw new FileAlreadyExistsException(file.getPath());
            }
        } catch (Exception e) {
            usage();
            System.err.println("File must have a valid path and not exist: " + args[2]);
            System.exit(5);
            return;
        }

        var localFortuna = ThreadLocal.withInitial(() -> {
            System.err.println("Creating RNG instance");
            return LocalFortuna.createInstance();
        });

        System.err.println("Generating data...");
        long start = System.currentTimeMillis();

        try (FileChannel channel = FileChannel.open(
                file.toPath(),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE)) {

            long totalSize = (long) count * bs;
            channel.truncate(totalSize);

            IntStream.range(0, count)
                    .parallel()
                    .forEach(i -> {
                        byte[] block = new byte[bs];
                        localFortuna.get().nextBytes(block);

                        ByteBuffer buffer = ByteBuffer.wrap(block);
                        long position = (long) i * bs;

                        try {
                            channel.write(buffer, position);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }

        System.err.println("Done in " + ((System.currentTimeMillis() - start) / 1000) + " seconds");
    }


    private static void usage() {
        System.err.println("Usage: " + LocalDump.class.getName() + " <bs_in_bytes> <count> [<file>]");
        System.err.println("Will generate <bs_in_bytes> * <count> of data and output them either to <file> or stdout if <file> is not specified");
    }
}
