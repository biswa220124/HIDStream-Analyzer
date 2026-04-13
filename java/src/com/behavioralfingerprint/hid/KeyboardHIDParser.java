package com.behavioralfingerprint.hid;

import java.util.ArrayList;
import java.util.List;

public class KeyboardHIDParser {
    private byte[] lastReport;

    public boolean looksLikeKeyboard(byte[] report) {
        return report != null && report.length == 8 && report[1] == 0x00;
    }

    public List<HIDEvent> parseReport(long timestampMicros, byte[] report) {
        List<HIDEvent> events = new ArrayList<>();
        if (!looksLikeKeyboard(report)) {
            return events;
        }

        boolean[] prev = new boolean[256];
        boolean[] curr = new boolean[256];

        if (lastReport != null) {
            for (int i = 2; i < 8; i++) {
                int code = lastReport[i] & 0xff;
                if (code != 0) {
                    prev[code] = true;
                }
            }
        }

        for (int i = 2; i < 8; i++) {
            int code = report[i] & 0xff;
            if (code != 0) {
                curr[code] = true;
            }
        }

        int modifier = report[0] & 0xff;
        boolean shift = (modifier & 0x22) != 0; // left or right shift

        for (int code = 0; code < curr.length; code++) {
            if (curr[code] && !prev[code]) {
                HIDEvent ev = new HIDEvent(timestampMicros, HIDEvent.DeviceType.KEYBOARD, HIDEvent.EventType.KEY_DOWN);
                ev.keycode = code;
                ev.modifier = modifier;
                ev.character = mapKeycodeToChar(code, shift);
                events.add(ev);
            }
        }

        for (int code = 0; code < prev.length; code++) {
            if (prev[code] && !curr[code]) {
                HIDEvent ev = new HIDEvent(timestampMicros, HIDEvent.DeviceType.KEYBOARD, HIDEvent.EventType.KEY_UP);
                ev.keycode = code;
                ev.modifier = modifier;
                ev.character = mapKeycodeToChar(code, shift);
                events.add(ev);
            }
        }

        lastReport = report.clone();
        return events;
    }

    private char mapKeycodeToChar(int keycode, boolean shift) {
        if (keycode >= 0x04 && keycode <= 0x1d) {
            char base = (char) ('a' + (keycode - 0x04));
            return shift ? Character.toUpperCase(base) : base;
        }
        if (keycode >= 0x1e && keycode <= 0x27) {
            char[] normal = {'1','2','3','4','5','6','7','8','9','0'};
            char[] shifted = {'!','@','#','$','%','^','&','*','(',')'};
            int idx = keycode - 0x1e;
            return shift ? shifted[idx] : normal[idx];
        }
        switch (keycode) {
            case 0x28: return '\n'; // Enter
            case 0x2c: return ' ';
            case 0x2d: return shift ? '_' : '-';
            case 0x2e: return shift ? '+' : '=';
            case 0x2f: return shift ? '{' : '[';
            case 0x30: return shift ? '}' : ']';
            case 0x31: return shift ? '|' : '\\';
            case 0x33: return shift ? ':' : ';';
            case 0x34: return shift ? '"' : '\'';
            case 0x35: return shift ? '~' : '`';
            case 0x36: return shift ? '<' : ',';
            case 0x37: return shift ? '>' : '.';
            case 0x38: return shift ? '?' : '/';
            case 0x2a: return '\b'; // Backspace
            case 0x2b: return '\t'; // Tab
            default: return 0;
        }
    }
}
