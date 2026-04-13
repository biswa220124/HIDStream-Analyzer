package com.behavioralfingerprint.features;

import com.behavioralfingerprint.hid.HIDEvent;

import java.util.ArrayList;
import java.util.List;

public class MouseDynamics {
    private static final long PAUSE_THRESHOLD_US = 200_000L;
    private static final long DOUBLE_CLICK_THRESHOLD_US = 500_000L;

    public void apply(List<HIDEvent> events, FeatureVector vector) {
        if (events.isEmpty()) {
            return;
        }

        List<Double> speeds = new ArrayList<>();
        List<Double> accels = new ArrayList<>();
        List<Double> curvatures = new ArrayList<>();
        List<Double> pauseDurations = new ArrayList<>();

        List<Double> angles = new ArrayList<>();
        List<Double> dists = new ArrayList<>();

        long prevTime = -1;
        double prevSpeed = Double.NaN;
        boolean inBurst = false;

        long totalTime = 0;
        long idleTime = 0;

        double x = 0;
        double y = 0;
        double pathLength = 0;

        int clickCount = 0;
        int doubleClickCount = 0;
        long lastClickTime = -1;
        boolean prevLeft = false;
        boolean prevRight = false;

        for (HIDEvent ev : events) {
            if (ev.device != HIDEvent.DeviceType.MOUSE) {
                continue;
            }

            if (!prevLeft && ev.leftClick) {
                clickCount++;
                if (lastClickTime >= 0 && (ev.timestampMicros - lastClickTime) <= DOUBLE_CLICK_THRESHOLD_US) {
                    doubleClickCount++;
                }
                lastClickTime = ev.timestampMicros;
            }
            if (!prevRight && ev.rightClick) {
                clickCount++;
                if (lastClickTime >= 0 && (ev.timestampMicros - lastClickTime) <= DOUBLE_CLICK_THRESHOLD_US) {
                    doubleClickCount++;
                }
                lastClickTime = ev.timestampMicros;
            }
            prevLeft = ev.leftClick;
            prevRight = ev.rightClick;

            x += ev.dx;
            y += ev.dy;

            double dist = Math.sqrt((double) ev.dx * ev.dx + (double) ev.dy * ev.dy);
            pathLength += dist;

            if (prevTime >= 0) {
                long dt = ev.timestampMicros - prevTime;
                if (dt > 0) {
                    totalTime += dt;
                    if (dist == 0.0) {
                        idleTime += dt;
                    }

                    if (dt > PAUSE_THRESHOLD_US) {
                        pauseDurations.add(dt / 1000.0);
                        inBurst = false;
                    } else if (dist > 0.0) {
                        if (!inBurst) {
                            vector.movementBursts++;
                            inBurst = true;
                        }
                    }

                    if (dist > 0.0) {
                        double speed = dist * 1000.0 / dt; // px/ms
                        speeds.add(speed);
                        dists.add(dist);
                        angles.add(Math.atan2(ev.dy, ev.dx));

                        if (!Double.isNaN(prevSpeed)) {
                            double accel = (speed - prevSpeed) * 1000.0 / dt; // px/ms^2
                            accels.add(accel);
                        }
                        prevSpeed = speed;
                    }
                }
            }

            prevTime = ev.timestampMicros;
        }

        for (int i = 1; i < angles.size(); i++) {
            double dist = dists.get(i);
            if (dist == 0.0) {
                continue;
            }
            double delta = angles.get(i) - angles.get(i - 1);
            curvatures.add(delta / dist);
        }

        double[] speedArr = toArray(speeds);
        double[] accelArr = toArray(accels);
        double[] curveArr = toArray(curvatures);
        double[] pauseArr = toArray(pauseDurations);

        vector.speedMean = Stats.mean(speedArr);
        vector.speedStddev = Stats.stddev(speedArr);
        vector.speedMax = Stats.max(speedArr);
        vector.speedP95 = Stats.percentile(speedArr, 95.0, true);

        vector.accelMean = Stats.mean(accelArr);
        vector.accelStddev = Stats.stddev(accelArr);
        vector.accelMax = Stats.max(accelArr);

        vector.curveMean = Stats.mean(curveArr);
        vector.curveStddev = Stats.stddev(curveArr);

        vector.pauseCount = pauseArr.length;
        vector.pauseMeanDuration = Stats.mean(pauseArr);

        vector.clickCount = clickCount;
        vector.doubleClickRate = clickCount == 0 ? 0.0 : (double) doubleClickCount / clickCount;

        double displacement = Math.sqrt(x * x + y * y);
        vector.straightnessRatio = pathLength == 0.0 ? 0.0 : displacement / pathLength;
        vector.idleRatio = totalTime == 0 ? 0.0 : (double) idleTime / totalTime;
    }

    private double[] toArray(List<Double> values) {
        double[] out = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            out[i] = values.get(i);
        }
        return out;
    }
}
