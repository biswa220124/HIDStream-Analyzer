package com.behavioralfingerprint.profile;

public class BehavioralProfile {
    public String label;
    public double[] mean;
    public double[] stddev;

    public BehavioralProfile(String label, double[] mean, double[] stddev) {
        this.label = label;
        this.mean = mean;
        this.stddev = stddev;
    }
}
