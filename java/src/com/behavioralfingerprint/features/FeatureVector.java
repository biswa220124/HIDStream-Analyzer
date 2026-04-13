package com.behavioralfingerprint.features;

public class FeatureVector {
    public static final int FEATURE_COUNT = 34;

    public String sessionId;
    public long durationMicros;
    public int totalEvents;

    // 18 keystroke features
    public double holdMean, holdStddev, holdMin, holdMax, holdMedian, holdIqr;
    public double flightMean, flightStddev, flightMin, flightMax, flightMedian, flightIqr;
    public double digraphMean, digraphStddev, digraphMin, digraphMax, digraphMedian, digraphIqr;

    // 16 mouse features
    public double speedMean, speedStddev, speedMax, speedP95;
    public double accelMean, accelStddev, accelMax;
    public double curveMean, curveStddev;
    public int pauseCount;
    public double pauseMeanDuration;
    public int clickCount;
    public double doubleClickRate;
    public double straightnessRatio;
    public int movementBursts;
    public double idleRatio;

    public double[] toArray() {
        return new double[] {
            holdMean, holdStddev, holdMin, holdMax, holdMedian, holdIqr,
            flightMean, flightStddev, flightMin, flightMax, flightMedian, flightIqr,
            digraphMean, digraphStddev, digraphMin, digraphMax, digraphMedian, digraphIqr,
            speedMean, speedStddev, speedMax, speedP95,
            accelMean, accelStddev, accelMax,
            curveMean, curveStddev,
            pauseCount, pauseMeanDuration,
            clickCount, doubleClickRate,
            straightnessRatio, movementBursts, idleRatio
        };
    }

    public static String[] featureNames() {
        return new String[] {
            "hold_mean", "hold_stddev", "hold_min", "hold_max", "hold_median", "hold_iqr",
            "flight_mean", "flight_stddev", "flight_min", "flight_max", "flight_median", "flight_iqr",
            "digraph_mean", "digraph_stddev", "digraph_min", "digraph_max", "digraph_median", "digraph_iqr",
            "speed_mean", "speed_stddev", "speed_max", "speed_p95",
            "accel_mean", "accel_stddev", "accel_max",
            "curve_mean", "curve_stddev",
            "pause_count", "pause_mean_duration",
            "click_count", "doubleclick_rate",
            "straightness_ratio", "movement_bursts", "idle_ratio"
        };
    }
}
