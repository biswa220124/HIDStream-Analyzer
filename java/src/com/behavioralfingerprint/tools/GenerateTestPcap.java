package com.behavioralfingerprint.tools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GenerateTestPcap {
    private static final int USBMON_HEADER_LEN = 40;
    private static final int LINKTYPE_USB_LINUX = 220;

    private static final Map<Character, Integer> KEYCODES = new HashMap<>();
    static {
        KEYCODES.put('h', 0x0b);
        KEYCODES.put('e', 0x08);
        KEYCODES.put('l', 0x0f);
        KEYCODES.put('o', 0x12);
    }

    public static void main(String[] args) throws IOException {
        String mode = "human";
        String out = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--mode".equals(arg)) {
                mode = nextArg(args, ++i, "--mode requires a value");
            } else if ("--out".equals(arg)) {
                out = nextArg(args, ++i, "--out requires a value");
            } else if ("--help".equals(arg)) {
                printUsage();
                return;
            } else {
                System.err.println("Unknown arg: " + arg);
                printUsage();
                return;
            }
        }

        if (out == null) {
            System.err.println("Missing --out");
            printUsage();
            return;
        }

        writePcap(out, mode);
        System.out.println("Wrote " + out);
    }

    private static void printUsage() {
        System.out.println("Usage: java -cp <out> com.behavioralfingerprint.tools.GenerateTestPcap --mode human|bot --out <path>");
    }

    private static void writePcap(String path, String mode) throws IOException {
        List<Event> events = buildTypingSequence(mode);
        long lastTime = events.isEmpty() ? 0 : events.get(events.size() - 1).timestampMicros + 500_000L;
        events.addAll(buildMouseSequence(mode, lastTime));
        events.sort((a, b) -> Long.compare(a.timestampMicros, b.timestampMicros));

        try (FileOutputStream out = new FileOutputStream(path)) {
            out.write(pcapGlobalHeader());
            long baseSec = 1_700_000_000L;

            for (Event event : events) {
                long tsSec = baseSec + (event.timestampMicros / 1_000_000L);
                long tsUsec = event.timestampMicros % 1_000_000L;
                byte[] data = new byte[USBMON_HEADER_LEN + event.report.length];
                System.arraycopy(event.report, 0, data, USBMON_HEADER_LEN, event.report.length);

                out.write(pcapPacketHeader((int) tsSec, (int) tsUsec, data.length));
                out.write(data);
            }
        }
    }

    private static List<Event> buildTypingSequence(String mode) {
        List<Event> events = new ArrayList<>();
        Random rng = new Random(42);
        long tMs = 0;
        for (char ch : "hello".toCharArray()) {
            int key = KEYCODES.getOrDefault(ch, 0);
            int downDelay;
            int upDelay;
            int gap;

            if ("bot".equals(mode)) {
                downDelay = 0;
                upDelay = 1;
                gap = 1;
            } else {
                downDelay = randBetween(rng, 40, 120);
                upDelay = randBetween(rng, 60, 180);
                gap = randBetween(rng, 50, 180);
            }

            tMs += downDelay;
            events.add(new Event(tMs * 1000L, hidKeyboardReport(0x00, key)));
            tMs += upDelay;
            events.add(new Event(tMs * 1000L, hidKeyboardReport(0x00, 0x00)));
            tMs += gap;
        }
        return events;
    }

    private static List<Event> buildMouseSequence(String mode, long startMicros) {
        List<Event> events = new ArrayList<>();
        Random rng = new Random(99);
        long t = startMicros;
        for (int i = 0; i < 20; i++) {
            int dx;
            int dy;
            int dt;
            if ("bot".equals(mode)) {
                dx = 5;
                dy = 5;
                dt = 5_000;
            } else {
                dx = randBetween(rng, 1, 6);
                dy = randBetween(rng, 1, 6);
                dt = randBetween(rng, 8_000, 40_000);
            }
            t += dt;
            events.add(new Event(t, hidMouseReport(0, dx, dy, 0)));
        }
        return events;
    }

    private static int randBetween(Random rng, int min, int max) {
        return min + rng.nextInt(max - min + 1);
    }

    private static byte[] pcapGlobalHeader() {
        ByteBuffer buf = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(0xa1b2c3d4);
        buf.putShort((short) 2);
        buf.putShort((short) 4);
        buf.putInt(0);
        buf.putInt(0);
        buf.putInt(65535);
        buf.putInt(LINKTYPE_USB_LINUX);
        return buf.array();
    }

    private static byte[] pcapPacketHeader(int tsSec, int tsUsec, int length) {
        ByteBuffer buf = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(tsSec);
        buf.putInt(tsUsec);
        buf.putInt(length);
        buf.putInt(length);
        return buf.array();
    }

    private static byte[] hidKeyboardReport(int modifier, int keycode) {
        return new byte[] {
            (byte) modifier,
            0x00,
            (byte) keycode,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00
        };
    }

    private static byte[] hidMouseReport(int buttons, int dx, int dy, int scroll) {
        return new byte[] {
            (byte) buttons,
            (byte) dx,
            (byte) dy,
            (byte) scroll
        };
    }

    private static String nextArg(String[] args, int index, String error) {
        if (index >= args.length) {
            throw new IllegalArgumentException(error);
        }
        return args[index];
    }

    private static class Event {
        final long timestampMicros;
        final byte[] report;

        Event(long timestampMicros, byte[] report) {
            this.timestampMicros = timestampMicros;
            this.report = report;
        }
    }
}
