package com.behavioralfingerprint.profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileStore {
    public void save(String path, BehavioralProfile profile) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"label\": \"").append(escape(profile.label)).append("\",\n");
        sb.append("  \"feature_count\": ").append(profile.mean.length).append(",\n");
        sb.append("  \"mean\": ").append(arrayToJson(profile.mean)).append(",\n");
        sb.append("  \"stddev\": ").append(arrayToJson(profile.stddev)).append("\n");
        sb.append("}\n");
        Files.writeString(Path.of(path), sb.toString());
    }

    public BehavioralProfile load(String path) throws IOException {
        String json = Files.readString(Path.of(path));
        String label = extractString(json, "label");
        double[] mean = extractArray(json, "mean");
        double[] stddev = extractArray(json, "stddev");
        return new BehavioralProfile(label, mean, stddev);
    }

    private String arrayToJson(double[] values) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(Double.toString(values[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String extractString(String json, String key) {
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"(.*?)\\\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private double[] extractArray(String json, String key) {
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (!m.find()) {
            return new double[0];
        }
        String body = m.group(1).trim();
        if (body.isEmpty()) {
            return new double[0];
        }
        String[] parts = body.split(",");
        List<Double> values = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            values.add(Double.parseDouble(trimmed));
        }
        double[] out = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            out[i] = values.get(i);
        }
        return out;
    }
}
