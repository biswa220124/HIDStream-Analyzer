package com.behavioralfingerprint.pcap;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class PcapReader {
    private static final int GLOBAL_HEADER_LEN = 24;
    private static final int PACKET_HEADER_LEN = 16;

    public PcapReadResult read(String path) throws IOException {
        try (FileInputStream in = new FileInputStream(path)) {
            byte[] globalHeader = readFully(in, GLOBAL_HEADER_LEN);
            if (globalHeader == null) {
                throw new IOException("PCAP file too short: " + path);
            }

            ByteOrder order = detectByteOrder(globalHeader);
            boolean nanoPrecision = isNanoPrecision(globalHeader, order);

            ByteBuffer gh = ByteBuffer.wrap(globalHeader).order(order);
            int magic = gh.getInt(0);
            if (!isKnownMagic(magic)) {
                throw new IOException("Unsupported PCAP magic: 0x" + Integer.toHexString(magic));
            }
            int linkType = gh.getInt(20);

            List<RawUsbPacket> packets = new ArrayList<>();
            while (true) {
                byte[] packetHeader = readFully(in, PACKET_HEADER_LEN);
                if (packetHeader == null) {
                    break;
                }

                ByteBuffer ph = ByteBuffer.wrap(packetHeader).order(order);
                long tsSec = toUnsignedInt(ph.getInt(0));
                long tsSub = toUnsignedInt(ph.getInt(4));
                long inclLen = toUnsignedInt(ph.getInt(8));

                if (inclLen <= 0 || inclLen > Integer.MAX_VALUE) {
                    // Skip invalid packet lengths.
                    skipFully(in, (int) Math.max(0, inclLen));
                    continue;
                }

                byte[] data = readFully(in, (int) inclLen);
                if (data == null) {
                    break;
                }

                long timestampMicros = tsSec * 1_000_000L + (nanoPrecision ? (tsSub / 1000L) : tsSub);
                packets.add(new RawUsbPacket(timestampMicros, data));
            }

            return new PcapReadResult(linkType, packets);
        }
    }

    private static ByteOrder detectByteOrder(byte[] globalHeader) {
        int magicBE = ByteBuffer.wrap(globalHeader).order(ByteOrder.BIG_ENDIAN).getInt(0);
        if (magicBE == 0xa1b2c3d4 || magicBE == 0xa1b23c4d) {
            return ByteOrder.BIG_ENDIAN;
        }
        int magicLE = ByteBuffer.wrap(globalHeader).order(ByteOrder.LITTLE_ENDIAN).getInt(0);
        if (magicLE == 0xa1b2c3d4 || magicLE == 0xa1b23c4d || magicLE == 0xd4c3b2a1 || magicLE == 0x4d3cb2a1) {
            return ByteOrder.LITTLE_ENDIAN;
        }
        return ByteOrder.BIG_ENDIAN;
    }

    private static boolean isNanoPrecision(byte[] globalHeader, ByteOrder order) {
        int magic = ByteBuffer.wrap(globalHeader).order(order).getInt(0);
        return magic == 0xa1b23c4d || magic == 0x4d3cb2a1;
    }

    private static boolean isKnownMagic(int magic) {
        return magic == 0xa1b2c3d4 || magic == 0xd4c3b2a1 || magic == 0xa1b23c4d || magic == 0x4d3cb2a1;
    }

    private static long toUnsignedInt(int value) {
        return value & 0xffffffffL;
    }

    private static byte[] readFully(FileInputStream in, int len) throws IOException {
        byte[] buf = new byte[len];
        int off = 0;
        while (off < len) {
            int read = in.read(buf, off, len - off);
            if (read == -1) {
                return null;
            }
            off += read;
        }
        return buf;
    }

    private static void skipFully(FileInputStream in, int len) throws IOException {
        long remaining = len;
        while (remaining > 0) {
            long skipped = in.skip(remaining);
            if (skipped <= 0) {
                break;
            }
            remaining -= skipped;
        }
    }
}
