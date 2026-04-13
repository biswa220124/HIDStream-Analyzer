package com.behavioralfingerprint.classifier;

import com.behavioralfingerprint.features.FeatureVector;

import java.util.List;

public class MahalanobisClassifier {
    private static final double EPSILON = 1e-6;

    private double[] mean;
    private double[][] invCov;

    public void train(List<FeatureVector> vectors) {
        int n = FeatureVector.FEATURE_COUNT;
        mean = new double[n];
        if (vectors.isEmpty()) {
            invCov = identity(n);
            return;
        }

        for (FeatureVector v : vectors) {
            double[] arr = v.toArray();
            for (int i = 0; i < n; i++) {
                mean[i] += arr[i];
            }
        }
        for (int i = 0; i < n; i++) {
            mean[i] /= vectors.size();
        }

        double[][] cov = new double[n][n];
        for (FeatureVector v : vectors) {
            double[] arr = v.toArray();
            for (int i = 0; i < n; i++) {
                double di = arr[i] - mean[i];
                for (int j = 0; j < n; j++) {
                    double dj = arr[j] - mean[j];
                    cov[i][j] += di * dj;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                cov[i][j] /= vectors.size();
            }
        }

        for (int i = 0; i < n; i++) {
            cov[i][i] += EPSILON;
        }

        invCov = invert(cov);
    }

    public ClassificationResult classify(FeatureVector vector, double threshold) {
        double d2 = distanceSquared(vector);
        ClassificationResult.Verdict verdict = d2 > threshold ? ClassificationResult.Verdict.BOT : ClassificationResult.Verdict.HUMAN;
        double outlierRatio = Math.min(1.0, d2 / threshold);
        double confidence = Math.max(0.0, 1.0 - outlierRatio);
        return new ClassificationResult(verdict, confidence, outlierRatio, 0, FeatureVector.FEATURE_COUNT, null);
    }

    public double distanceSquared(FeatureVector vector) {
        if (mean == null || invCov == null) {
            throw new IllegalStateException("Classifier not trained");
        }
        double[] x = vector.toArray();
        int n = x.length;
        double[] diff = new double[n];
        for (int i = 0; i < n; i++) {
            diff[i] = x[i] - mean[i];
        }

        double[] temp = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                sum += invCov[i][j] * diff[j];
            }
            temp[i] = sum;
        }

        double d2 = 0.0;
        for (int i = 0; i < n; i++) {
            d2 += diff[i] * temp[i];
        }
        return d2;
    }

    private double[][] invert(double[][] matrix) {
        int n = matrix.length;
        double[][] aug = new double[n][n * 2];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                aug[i][j] = matrix[i][j];
            }
            aug[i][n + i] = 1.0;
        }

        for (int i = 0; i < n; i++) {
            int pivot = i;
            double max = Math.abs(aug[i][i]);
            for (int r = i + 1; r < n; r++) {
                double v = Math.abs(aug[r][i]);
                if (v > max) {
                    max = v;
                    pivot = r;
                }
            }
            if (pivot != i) {
                double[] tmp = aug[i];
                aug[i] = aug[pivot];
                aug[pivot] = tmp;
            }

            double diag = aug[i][i];
            if (Math.abs(diag) < EPSILON) {
                diag = EPSILON;
            }
            for (int j = 0; j < n * 2; j++) {
                aug[i][j] /= diag;
            }

            for (int r = 0; r < n; r++) {
                if (r == i) continue;
                double factor = aug[r][i];
                if (factor == 0.0) continue;
                for (int c = 0; c < n * 2; c++) {
                    aug[r][c] -= factor * aug[i][c];
                }
            }
        }

        double[][] inv = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(aug[i], n, inv[i], 0, n);
        }
        return inv;
    }

    private double[][] identity(int n) {
        double[][] id = new double[n][n];
        for (int i = 0; i < n; i++) {
            id[i][i] = 1.0;
        }
        return id;
    }
}
