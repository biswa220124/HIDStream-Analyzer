package com.behavioralfingerprint.hid;

import java.util.ArrayList;
import java.util.List;

public class MouseHIDParser {
    private int lastButtons = 0;

    public boolean looksLikeMouse(byte[] report) {
        return report != null && report.length == 4;
    }

    public List<HIDEvent> parseReport(long timestampMicros, byte[] report) {
        List<HIDEvent> events = new ArrayList<>();
        if (!looksLikeMouse(report)) {
            return events;
        }

        int buttons = report[0] & 0xff;
        int dx = (byte) report[1];
        int dy = (byte) report[2];
        int scroll = (byte) report[3];

        boolean left = (buttons & 0x01) != 0;
        boolean right = (buttons & 0x02) != 0;

        boolean buttonsChanged = buttons != lastButtons;
        boolean moved = dx != 0 || dy != 0 || scroll != 0;

        if (buttonsChanged || moved) {
            HIDEvent.EventType type = buttonsChanged ? HIDEvent.EventType.MOUSE_CLICK : HIDEvent.EventType.MOUSE_MOVE;
            HIDEvent ev = new HIDEvent(timestampMicros, HIDEvent.DeviceType.MOUSE, type);
            ev.dx = dx;
            ev.dy = dy;
            ev.scroll = scroll;
            ev.leftClick = left;
            ev.rightClick = right;
            events.add(ev);
        }

        lastButtons = buttons;
        return events;
    }
}
