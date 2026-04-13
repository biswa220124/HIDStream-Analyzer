package com.behavioralfingerprint;

import com.behavioralfingerprint.classifier.ClassificationResult;
import com.behavioralfingerprint.classifier.ZScoreClassifier;
import com.behavioralfingerprint.features.FeatureExtractor;
import com.behavioralfingerprint.features.FeatureVector;
import com.behavioralfingerprint.hid.HIDEvent;
import com.behavioralfingerprint.hid.HIDParser;
import com.behavioralfingerprint.pcap.PcapReadResult;
import com.behavioralfingerprint.pcap.PcapReader;
import com.behavioralfingerprint.pcap.UsbPacketFilter;
import com.behavioralfingerprint.profile.BehavioralProfile;
import com.behavioralfingerprint.profile.ProfileBuilder;
import com.behavioralfingerprint.profile.ProfileStore;
import com.behavioralfingerprint.report.ReportPrinter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0 || hasArg(args, "--help")) {
            printUsage();
            return;
        }

        String mode = null;
        List<String> inputs = new ArrayList<>();
        String profilePath = null;
        String outputPath = null;
        String label = "human_baseline";
        String against = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--mode":
                    mode = nextArg(args, ++i, "--mode requires a value");
                    break;
                case "--input":
                    i++;
                    while (i < args.length && !args[i].startsWith("--")) {
                        inputs.add(args[i]);
                        i++;
                    }
                    i--;
                    break;
                case "--profile":
                    profilePath = nextArg(args, ++i, "--profile requires a value");
                    break;
                case "--output":
                    outputPath = nextArg(args, ++i, "--output requires a value");
                    break;
                case "--label":
                    label = nextArg(args, ++i, "--label requires a value");
                    break;
                case "--against":
                    against = nextArg(args, ++i, "--against requires a value");
                    break;
                default:
                    System.err.println("Unknown arg: " + arg);
                    printUsage();
                    return;
            }
        }

        if (mode == null) {
            System.err.println("Missing --mode");
            printUsage();
            return;
        }

        try {
            switch (mode) {
                case "analyze":
                    runAnalyze(inputs, profilePath);
                    break;
                case "build-profile":
                    runBuildProfile(inputs, outputPath, label);
                    break;
                case "compare":
                    runCompare(inputs, against);
                    break;
                case "dump-features":
                    runDumpFeatures(inputs);
                    break;
                default:
                    System.err.println("Unknown mode: " + mode);
                    printUsage();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private static void runAnalyze(List<String> inputs, String profilePath) throws IOException {
        if (inputs.size() != 1 || profilePath == null) {
            System.err.println("analyze requires --input <pcap> and --profile <json>");
            return;
        }

        String input = inputs.get(0);
        PcapReadResult pcap = readPcap(input);
        HIDParser hidParser = new HIDParser();
        List<HIDEvent> events = hidParser.parse(pcap.packets);

        FeatureExtractor extractor = new FeatureExtractor();
        FeatureVector vector = extractor.extract(events, input);

        ProfileStore store = new ProfileStore();
        BehavioralProfile profile = store.load(profilePath);

        ZScoreClassifier classifier = new ZScoreClassifier();
        ClassificationResult result = classifier.classify(vector, profile);

        ReportPrinter printer = new ReportPrinter();
        printer.printAnalysis(input, pcap.packets.size(), events, vector, result, profile, classifier);
    }

    private static void runBuildProfile(List<String> inputs, String outputPath, String label) throws IOException {
        if (inputs.isEmpty() || outputPath == null) {
            System.err.println("build-profile requires --input <pcap...> and --output <json>");
            return;
        }

        FeatureExtractor extractor = new FeatureExtractor();
        List<FeatureVector> vectors = new ArrayList<>();
        for (String input : inputs) {
            PcapReadResult pcap = readPcap(input);
            HIDParser parser = new HIDParser();
            List<HIDEvent> events = parser.parse(pcap.packets);
            FeatureVector vector = extractor.extract(events, input);
            vectors.add(vector);
        }

        ProfileBuilder builder = new ProfileBuilder();
        BehavioralProfile profile = builder.build(vectors, label);
        ProfileStore store = new ProfileStore();
        store.save(outputPath, profile);

        System.out.println("Profile saved: " + outputPath);
        System.out.println("Label: " + label);
        System.out.println("Sessions: " + vectors.size());
    }

    private static void runCompare(List<String> inputs, String against) throws IOException {
        if (inputs.size() != 1 || against == null) {
            System.err.println("compare requires --input <pcap> and --against <pcap>");
            return;
        }

        FeatureExtractor extractor = new FeatureExtractor();

        FeatureVector a = extractor.extract(parseEvents(inputs.get(0)), inputs.get(0));
        FeatureVector b = extractor.extract(parseEvents(against), against);

        double[] arrA = a.toArray();
        double[] arrB = b.toArray();
        String[] names = FeatureVector.featureNames();

        List<Diff> diffs = new ArrayList<>();
        for (int i = 0; i < arrA.length; i++) {
            diffs.add(new Diff(names[i], Math.abs(arrA[i] - arrB[i])));
        }
        diffs.sort(Comparator.comparingDouble((Diff d) -> -d.value));

        System.out.println("Feature comparison (top 10 deltas):");
        for (int i = 0; i < Math.min(10, diffs.size()); i++) {
            Diff d = diffs.get(i);
            System.out.printf("  %-20s : %.6f\n", d.name, d.value);
        }
    }

    private static void runDumpFeatures(List<String> inputs) throws IOException {
        if (inputs.size() != 1) {
            System.err.println("dump-features requires --input <pcap>");
            return;
        }
        FeatureExtractor extractor = new FeatureExtractor();
        FeatureVector vector = extractor.extract(parseEvents(inputs.get(0)), inputs.get(0));
        ReportPrinter printer = new ReportPrinter();
        printer.printFeatureDump(vector);
    }

    private static List<HIDEvent> parseEvents(String input) throws IOException {
        PcapReadResult pcap = readPcap(input);
        HIDParser parser = new HIDParser();
        return parser.parse(pcap.packets);
    }

    private static PcapReadResult readPcap(String input) throws IOException {
        PcapReader reader = new PcapReader();
        PcapReadResult result = reader.read(input);
        UsbPacketFilter filter = new UsbPacketFilter();
        if (!filter.isUsbLinkType(result.linkType)) {
            System.err.println("Warning: Non-USB linktype detected (" + result.linkType + ")");
        }
        return new PcapReadResult(result.linkType, filter.filter(result.packets));
    }

    private static boolean hasArg(String[] args, String name) {
        for (String arg : args) {
            if (arg.equals(name)) return true;
        }
        return false;
    }

    private static String nextArg(String[] args, int index, String error) {
        if (index >= args.length) {
            throw new IllegalArgumentException(error);
        }
        return args[index];
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  --mode analyze --input <pcap> --profile <profile.json>");
        System.out.println("  --mode build-profile --input <pcap...> --output <profile.json> --label <label>");
        System.out.println("  --mode compare --input <pcap> --against <pcap>");
        System.out.println("  --mode dump-features --input <pcap>");
    }

    private static class Diff {
        public final String name;
        public final double value;

        public Diff(String name, double value) {
            this.name = name;
            this.value = value;
        }
    }
}
