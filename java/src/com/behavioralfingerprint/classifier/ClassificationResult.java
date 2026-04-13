package com.behavioralfingerprint.classifier;

public class ClassificationResult {
    public enum Verdict { HUMAN, BOT, UNKNOWN }

    public Verdict verdict;
    public double confidence;
    public double outlierRatio;
    public int outlierCount;
    public int totalFeatures;
    public double[] zScores;

    public ClassificationResult(Verdict verdict, double confidence, double outlierRatio, int outlierCount, int totalFeatures, double[] zScores) {
        this.verdict = verdict;
        this.confidence = confidence;
        this.outlierRatio = outlierRatio;
        this.outlierCount = outlierCount;
        this.totalFeatures = totalFeatures;
        this.zScores = zScores;
    }
}
