package com.behavioralfingerprint.classifier;

import com.behavioralfingerprint.features.FeatureVector;
import com.behavioralfingerprint.profile.BehavioralProfile;

public class ZScoreClassifier {
    private static final double DEFAULT_THRESHOLD = 2.5;
    private static final double EPSILON = 1e-6;

    private final double threshold;

    public ZScoreClassifier() {
        this(DEFAULT_THRESHOLD);
    }

    public ZScoreClassifier(double threshold) {
        this.threshold = threshold;
    }

    public ClassificationResult classify(FeatureVector vector, BehavioralProfile profile) {
        double[] values = vector.toArray();
        int count = Math.min(values.length, Math.min(profile.mean.length, profile.stddev.length));
        double[] zScores = new double[count];
        int outliers = 0;

        for (int i = 0; i < count; i++) {
            double std = profile.stddev[i];
            if (std < EPSILON) {
                std = EPSILON;
            }
            double z = Math.abs(values[i] - profile.mean[i]) / std;
            zScores[i] = z;
            if (z > threshold) {
                outliers++;
            }
        }

        double outlierRatio = count == 0 ? 0.0 : (double) outliers / count;
        ClassificationResult.Verdict verdict;
        if (outlierRatio > 0.40) {
            verdict = ClassificationResult.Verdict.BOT;
        } else if (outlierRatio < 0.20) {
            verdict = ClassificationResult.Verdict.HUMAN;
        } else {
            verdict = ClassificationResult.Verdict.UNKNOWN;
        }

        double confidence = Math.max(0.0, 1.0 - outlierRatio);
        return new ClassificationResult(verdict, confidence, outlierRatio, outliers, count, zScores);
    }

    public double getThreshold() {
        return threshold;
    }
}
