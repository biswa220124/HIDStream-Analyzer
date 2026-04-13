package com.behavioralfingerprint.hid;

public class HIDEvent {
    public enum DeviceType { KEYBOARD, MOUSE, UNKNOWN }
    public enum EventType { KEY_DOWN, KEY_UP, MOUSE_MOVE, MOUSE_CLICK }

    public long timestampMicros;
    public DeviceType device;
    public EventType event;

    // Keyboard fields
    public int keycode;
    public int modifier;
    public char character;

    // Mouse fields
    public int dx;
    public int dy;
    public int scroll;
    public boolean leftClick;
    public boolean rightClick;

    public HIDEvent(long timestampMicros, DeviceType device, EventType event) {
        this.timestampMicros = timestampMicros;
        this.device = device;
        this.event = event;
    }
}
