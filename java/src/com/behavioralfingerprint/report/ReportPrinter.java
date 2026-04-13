package com.behavioralfingerprint.report;

import com.behavioralfingerprint.classifier.ClassificationResult;
import com.behavioralfingerprint.classifier.ZScoreClassifier;
import com.behavioralfingerprint.features.FeatureVector;
import com.behavioralfingerprint.hid.HIDEvent;
import com.behavioralfingerprint.profile.BehavioralProfile;

import java.util.List;

public class ReportPrinter {
    public void printAnalysis(String inputPath,
                              int usbPackets,
                              List<HIDEvent> events,
                              FeatureVector vector,
                              ClassificationResult result,
                              BehavioralProfile profile,
                              ZScoreClassifier classifier) {
        int keyboardEvents = 0;
        int mouseEvents = 0;
        for (HIDEvent ev : events) {
            if (ev.device == HIDEvent.DeviceType.KEYBOARD) {
                keyboardEvents++;
            } else if (ev.device == HIDEvent.DeviceType.MOUSE) {
                mouseEvents++;
            }
        }

        System.out.println("============================================================");
        System.out.println("  BEHAVIORAL FINGERPRINTING ENGINE  v1.0");
        System.out.println("============================================================");
        System.out.println("[*] Loading capture : " + inputPath);
        System.out.println("[*] USB packets found: " + usbPackets);
        System.out.println("[*] HID events parsed: " + events.size());
        System.out.println("    Keyboard events : " + keyboardEvents);
        System.out.println("    Mouse events    : " + mouseEvents);
        System.out.println();

        System.out.println("---------------------- KEYSTROKE DYNAMICS -------------------");
        System.out.printf("Hold Time   mean=%.2fms  stddev=%.2fms\n", vector.holdMean, vector.holdStddev);
        System.out.printf("Flight Time mean=%.2fms  stddev=%.2fms\n", vector.flightMean, vector.flightStddev);
        System.out.printf("Digraph     mean=%.2fms  stddev=%.2fms\n", vector.digraphMean, vector.digraphStddev);
        System.out.println();

        System.out.println("----------------------- MOUSE DYNAMICS ----------------------");
        System.out.printf("Speed       mean=%.3fpx/ms  stddev=%.3fpx/ms\n", vector.speedMean, vector.speedStddev);
        System.out.printf("Curvature   mean=%.5f      stddev=%.5f\n", vector.curveMean, vector.curveStddev);
        System.out.printf("Straightness ratio=%.3f\n", vector.straightnessRatio);
        System.out.printf("Pauses      count=%d mean_duration=%.2fms\n", vector.pauseCount, vector.pauseMeanDuration);
        System.out.printf("Idle Ratio  %.3f\n", vector.idleRatio);
        System.out.println();

        if (result != null) {
            System.out.println("---------------------- CLASSIFICATION ------------------------");
            System.out.println("Method       : Z-Score (threshold=" + classifier.getThreshold() + ")");
            if (profile != null) {
                System.out.println("Baseline     : " + profile.label);
            }
            System.out.printf("Outlier Ratio: %.2f%% (%d/%d)\n", result.outlierRatio * 100.0, result.outlierCount, result.totalFeatures);
            System.out.println();
            System.out.println("Verdict      : " + result.verdict);
            System.out.printf("Confidence   : %.0f%%\n", result.confidence * 100.0);
            System.out.println();
        }
    }

    public void printFeatureDump(FeatureVector vector) {
        String[] names = FeatureVector.featureNames();
        double[] values = vector.toArray();
        System.out.println("Feature Dump:");
        for (int i = 0; i < names.length; i++) {
            System.out.printf("  %-20s : %.6f\n", names[i], values[i]);
        }
    }
}
