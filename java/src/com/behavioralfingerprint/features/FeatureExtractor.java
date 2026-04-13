package com.behavioralfingerprint.features;

import com.behavioralfingerprint.hid.HIDEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FeatureExtractor {
    private final KeystrokeDynamics keystrokeDynamics = new KeystrokeDynamics();
    private final MouseDynamics mouseDynamics = new MouseDynamics();

    public FeatureVector extract(List<HIDEvent> events, String sessionId) {
        FeatureVector vector = new FeatureVector();
        vector.sessionId = sessionId;
        vector.totalEvents = events.size();

        if (events.isEmpty()) {
            return vector;
        }

        events.sort(Comparator.comparingLong(e -> e.timestampMicros));
        long start = events.get(0).timestampMicros;
        long end = events.get(events.size() - 1).timestampMicros;
        vector.durationMicros = Math.max(0, end - start);

        List<HIDEvent> keyboard = new ArrayList<>();
        List<HIDEvent> mouse = new ArrayList<>();
        for (HIDEvent ev : events) {
            if (ev.device == HIDEvent.DeviceType.KEYBOARD) {
                keyboard.add(ev);
            } else if (ev.device == HIDEvent.DeviceType.MOUSE) {
                mouse.add(ev);
            }
        }

        keystrokeDynamics.apply(keyboard, vector);
        mouseDynamics.apply(mouse, vector);
        return vector;
    }
}
