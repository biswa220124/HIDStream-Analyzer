package com.behavioralfingerprint.pcap;

import java.util.ArrayList;
import java.util.List;

public class UsbPacketFilter {
    public static final int LINKTYPE_USB_LINUX = 220;
    public static final int LINKTYPE_USB_LINUX_MMAPPED = 189;

    public boolean isUsbLinkType(int linkType) {
        return linkType == LINKTYPE_USB_LINUX || linkType == LINKTYPE_USB_LINUX_MMAPPED;
    }

    public List<RawUsbPacket> filter(List<RawUsbPacket> packets) {
        return new ArrayList<>(packets);
    }
}
