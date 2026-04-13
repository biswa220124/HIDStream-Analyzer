package com.behavioralfingerprint.hid;

import com.behavioralfingerprint.pcap.RawUsbPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HIDParser {
    private static final int USBMON_HEADER_LEN = 40;
    private static final int USBMON_HEADER_LEN_MMAPPED = 48;

    private final KeyboardHIDParser keyboardParser = new KeyboardHIDParser();
    private final MouseHIDParser mouseParser = new MouseHIDParser();

    public List<HIDEvent> parse(List<RawUsbPacket> packets) {
        List<HIDEvent> events = new ArrayList<>();
        for (RawUsbPacket packet : packets) {
            byte[] report = extractHidReport(packet.data);
            if (report == null) {
                continue;
            }

            if (keyboardParser.looksLikeKeyboard(report)) {
                events.addAll(keyboardParser.parseReport(packet.timestampMicros, report));
            } else if (mouseParser.looksLikeMouse(report)) {
                events.addAll(mouseParser.parseReport(packet.timestampMicros, report));
            }
        }
        return events;
    }

    private byte[] extractHidReport(byte[] data) {
        if (data == null || data.length < 4) {
            return null;
        }

        // Common usbmon headers
        if (data.length >= USBMON_HEADER_LEN + 4) {
            byte[] candidate = Arrays.copyOfRange(data, USBMON_HEADER_LEN, data.length);
            if (candidate.length == 4 || candidate.length == 8) {
                return candidate;
            }
        }

        if (data.length >= USBMON_HEADER_LEN_MMAPPED + 4) {
            byte[] candidate = Arrays.copyOfRange(data, USBMON_HEADER_LEN_MMAPPED, data.length);
            if (candidate.length == 4 || candidate.length == 8) {
                return candidate;
            }
        }

        if (data.length == 4 || data.length == 8) {
            return data;
        }

        if (data.length > 8) {
            byte[] tail8 = Arrays.copyOfRange(data, data.length - 8, data.length);
            return tail8;
        }

        return null;
    }
}
