package com.behavioralfingerprint.pcap;

import java.util.List;

public class PcapReadResult {
    public final int linkType;
    public final List<RawUsbPacket> packets;

    public PcapReadResult(int linkType, List<RawUsbPacket> packets) {
        this.linkType = linkType;
        this.packets = packets;
    }
}
