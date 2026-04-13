package com.behavioralfingerprint.pcap;

public class RawUsbPacket {
    public final long timestampMicros;
    public final byte[] data;

    public RawUsbPacket(long timestampMicros, byte[] data) {
        this.timestampMicros = timestampMicros;
        this.data = data;
    }
}
