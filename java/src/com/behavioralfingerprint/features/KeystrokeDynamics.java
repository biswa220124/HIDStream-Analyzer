package com.behavioralfingerprint.features;

import com.behavioralfingerprint.hid.HIDEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeystrokeDynamics {
    public void apply(List<HIDEvent> events, FeatureVector vector) {
        List<Double> holdTimes = new ArrayList<>();
        List<Double> flightTimes = new ArrayList<>();
        List<Double> digraphTimes = new ArrayList<>();

        Map<Integer, Long> keyDownTime = new HashMap<>();
        Long lastKeyDown = null;
        Long lastKeyUp = null;

        for (HIDEvent ev : events) {
            if (ev.event == HIDEvent.EventType.KEY_DOWN) {
                if (lastKeyUp != null) {
                    flightTimes.add((ev.timestampMicros - lastKeyUp) / 1000.0);
                }
                if (lastKeyDown != null) {
                    digraphTimes.add((ev.timestampMicros - lastKeyDown) / 1000.0);
                }
                lastKeyDown = ev.timestampMicros;
                if (!keyDownTime.containsKey(ev.keycode)) {
                    keyDownTime.put(ev.keycode, ev.timestampMicros);
                }
            } else if (ev.event == HIDEvent.EventType.KEY_UP) {
                Long start = keyDownTime.remove(ev.keycode);
                if (start != null) {
                    holdTimes.add((ev.timestampMicros - start) / 1000.0);
                }
                lastKeyUp = ev.timestampMicros;
            }
        }

        double[] hold = toArray(holdTimes);
        double[] flight = toArray(flightTimes);
        double[] digraph = toArray(digraphTimes);

        vector.holdMean = Stats.mean(hold);
        vector.holdStddev = Stats.stddev(hold);
        vector.holdMin = Stats.min(hold);
        vector.holdMax = Stats.max(hold);
        vector.holdMedian = Stats.median(hold);
        vector.holdIqr = Stats.iqr(hold);

        vector.flightMean = Stats.mean(flight);
        vector.flightStddev = Stats.stddev(flight);
        vector.flightMin = Stats.min(flight);
        vector.flightMax = Stats.max(flight);
        vector.flightMedian = Stats.median(flight);
        vector.flightIqr = Stats.iqr(flight);

        vector.digraphMean = Stats.mean(digraph);
        vector.digraphStddev = Stats.stddev(digraph);
        vector.digraphMin = Stats.min(digraph);
        vector.digraphMax = Stats.max(digraph);
        vector.digraphMedian = Stats.median(digraph);
        vector.digraphIqr = Stats.iqr(digraph);
    }

    private double[] toArray(List<Double> values) {
        double[] out = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            out[i] = values.get(i);
        }
        return out;
    }
}
