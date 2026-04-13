package com.behavioralfingerprint.profile;

import com.behavioralfingerprint.features.FeatureVector;

import java.util.List;

public class ProfileBuilder {
    public BehavioralProfile build(List<FeatureVector> vectors, String label) {
        int count = FeatureVector.FEATURE_COUNT;
        double[] mean = new double[count];
        double[] stddev = new double[count];

        if (vectors.isEmpty()) {
            return new BehavioralProfile(label, mean, stddev);
        }

        for (FeatureVector v : vectors) {
            double[] arr = v.toArray();
            for (int i = 0; i < count; i++) {
                mean[i] += arr[i];
            }
        }
        for (int i = 0; i < count; i++) {
            mean[i] /= vectors.size();
        }

        for (FeatureVector v : vectors) {
            double[] arr = v.toArray();
            for (int i = 0; i < count; i++) {
                double d = arr[i] - mean[i];
                stddev[i] += d * d;
            }
        }
        for (int i = 0; i < count; i++) {
            stddev[i] = Math.sqrt(stddev[i] / vectors.size());
        }

        return new BehavioralProfile(label, mean, stddev);
    }
}
