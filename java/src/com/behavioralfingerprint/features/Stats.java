package com.behavioralfingerprint.features;

import java.util.Arrays;

public class Stats {
    public static double mean(double[] values) {
        if (values.length == 0) return 0.0;
        double sum = 0.0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    public static double stddev(double[] values) {
        if (values.length == 0) return 0.0;
        double mean = mean(values);
        double sumSq = 0.0;
        for (double v : values) {
            double d = v - mean;
            sumSq += d * d;
        }
        return Math.sqrt(sumSq / values.length);
    }

    public static double min(double[] values) {
        if (values.length == 0) return 0.0;
        double m = values[0];
        for (double v : values) m = Math.min(m, v);
        return m;
    }

    public static double max(double[] values) {
        if (values.length == 0) return 0.0;
        double m = values[0];
        for (double v : values) m = Math.max(m, v);
        return m;
    }

    public static double median(double[] values) {
        if (values.length == 0) return 0.0;
        double[] copy = Arrays.copyOf(values, values.length);
        Arrays.sort(copy);
        int mid = copy.length / 2;
        if (copy.length % 2 == 0) {
            return (copy[mid - 1] + copy[mid]) / 2.0;
        }
        return copy[mid];
    }

    public static double iqr(double[] values) {
        if (values.length < 4) return 0.0;
        double[] copy = Arrays.copyOf(values, values.length);
        Arrays.sort(copy);
        double q1 = percentile(copy, 25.0);
        double q3 = percentile(copy, 75.0);
        return q3 - q1;
    }

    public static double percentile(double[] sortedValues, double percentile) {
        if (sortedValues.length == 0) return 0.0;
        if (sortedValues.length == 1) return sortedValues[0];
        double rank = (percentile / 100.0) * (sortedValues.length - 1);
        int low = (int) Math.floor(rank);
        int high = (int) Math.ceil(rank);
        if (low == high) return sortedValues[low];
        double weight = rank - low;
        return sortedValues[low] * (1.0 - weight) + sortedValues[high] * weight;
    }

    public static double percentile(double[] values, double percentile, boolean sortCopy) {
        double[] copy = sortCopy ? Arrays.copyOf(values, values.length) : values;
        if (sortCopy) {
            Arrays.sort(copy);
        }
        return percentile(copy, percentile);
    }
}
