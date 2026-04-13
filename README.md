# HIDStream Analyzer

**HIDStream Analyzer** is a high-performance Java-based behavioral biometric system designed to extract, analyze, and profile user interaction patterns from raw USB network traffic (PCAP). By parsing low-level Human Interface Device (HID) packets, the system builds unique "behavioral fingerprints" for users based on their typing and mouse movement characteristics.

## 🚀 Overview

In modern cybersecurity, credentials alone are often insufficient for identity verification. HIDStream Analyzer provides a continuous authentication layer by analyzing *how* a user interacts with their device. It can distinguish between legitimate human users and automated scripts or unauthorized actors by identifying deviations in established behavioral patterns.

## ✨ Key Features

- **Low-Level Protocol Parsing:** Direct extraction of HID events (Keyboard/Mouse) from raw USB PCAP captures.
- **Biometric Feature Engineering:**
    - **Keystroke Dynamics:** Analysis of hold times, flight times, and digraph latencies.
    - **Mouse Dynamics:** Processing of velocity, acceleration, curvature, and straightness ratios.
- **Statistical Profiling:** Generation of robust user profiles using Mean, Standard Deviation, IQRs, and 95th Percentiles.
- **Anomaly Detection:** Implementation of **Z-Score** and **Mahalanobis Distance** classifiers to detect behavioral outliers.
- **Forensic Reporting:** Detailed analysis reports comparing active sessions against baseline profiles.

## 🛠 Project Structure

```text
├── java
│   ├── src/com/behavioralfingerprint
│   │   ├── classifier/     # Z-Score and Mahalanobis implementations
│   │   ├── features/       # Keystroke and Mouse dynamics logic
│   │   ├── hid/            # USB HID protocol parsers
│   │   ├── pcap/           # PCAP reader and USB packet filters
│   │   ├── profile/        # Profile building and storage
│   │   └── Main.java       # CLI Entry point
│   └── build.sh            # Automated build script
├── profiles/               # Stored behavioral fingerprints (JSON)
└── test_captures/          # Sample PCAP data for testing
```

## ⚙️ Metrics Extracted

| Category | Features Monitored |
| :--- | :--- |
| **Keyboard** | Hold Time, Flight Time (Key-Key), Digraph Time, Typing Speed. |
| **Mouse** | Velocity, Acceleration, Curvature, Pause Durations, Idle Ratios. |
| **Clicks** | Single-click frequency, Double-click rates, Button hold durations. |
| **Style** | Straightness ratio of movements, burst frequency. |

## 🚀 Getting Started

### Prerequisites

- **Java JDK 11** or higher.
- **PCAP captures** (USB link-type) containing HID traffic.

### Build

Use the provided build script to compile the source code:

```bash
cd java
./build.sh
```

### Usage

The system operates in several modes via the command line:

#### 1. Build a User Profile
Create a baseline "fingerprint" from one or more capture files.
```bash
java -cp out com.behavioralfingerprint.Main --mode build-profile \
     --input test_captures/human_typing.pcap \
     --output profiles/my_profile.json \
     --label "JohnDoe"
```

#### 2. Analyze a Session
Compare a new capture against an existing profile to check for anomalies.
```bash
java -cp out com.behavioralfingerprint.Main --mode analyze \
     --input test_captures/new_session.pcap \
     --profile profiles/my_profile.json
```

#### 3. Dump Features
Extract and view the raw behavioral metrics from a capture file.
```bash
java -cp out com.behavioralfingerprint.Main --mode dump-features \
     --input test_captures/bot_typing.pcap
```

#### 4. Compare Sessions
Directly compare the top-10 feature deltas between two capture sessions.
```bash
java -cp out com.behavioralfingerprint.Main --mode compare \
     --input test_captures/human_typing.pcap \
     --against test_captures/bot_typing.pcap
```

## 🛡 Security Use Cases

- **Continuous Authentication:** Verifying a user's identity throughout a session, not just at login.
- **Anti-Automation:** Detecting "BadUSB" devices (like Rubber Ducky) that inject keystrokes at non-human speeds.
- **Insider Threat Detection:** Identifying when a workstation is being used by someone other than the primary user based on muscle-memory deviations.

---
*Created as a project for analyzing behavioral biometrics through network forensics.*
