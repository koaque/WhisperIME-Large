# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

WhisperIME is an Android voice input method app that uses OpenAI Whisper for speech recognition. It implements a custom Input Method Editor (IME) and Recognition Service using TensorFlow Lite models. This is a modified version of woheller69's project that uses a larger Whisper model.

## Build Commands

```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease

# Clean build
./gradlew clean

# Install debug version to connected device
./gradlew installDebug

# Run lint checks
./gradlew lint
```

## Architecture

### Core Components

- **MainActivity** - Main app interface for testing voice recognition with language selection and copy functionality
- **WhisperInputMethodService** - Core IME service that provides voice input keyboard functionality
- **WhisperRecognitionService** - Android RecognitionService implementation for system-wide voice recognition
- **WhisperRecognizeActivity** - Floating activity that handles `android.speech.action.RECOGNIZE_SPEECH` intents
- **DownloadActivity** - Handles initial model downloads from Hugging Face

### ASR (Automatic Speech Recognition) Package

Located in `app/src/main/java/com/whispertflite/asr/`:

- **Whisper.java** - Core TensorFlow Lite model wrapper for speech recognition
- **Recorder.java** - Audio recording and VAD (Voice Activity Detection) using WebRTC VAD
- **WhisperResult.java** - Result data structure for recognition results
- **RecordBuffer.java** - Audio buffer management

### Utilities Package

Located in `app/src/main/java/com/whispertflite/utils/`:

- **InputLang.java** - Language support and token management
- **Downloader.java** - Model download functionality
- **HapticFeedback.java** - Haptic feedback utilities
- **WhisperUtil.java** - General utility functions

## Model Configuration

The app uses a single large Whisper model:
- Model file: `whisper-large-v3.tflite`
- Vocabulary file: `filters_vocab_multilingual.bin`
- Models are downloaded from Hugging Face on first launch
- Models are stored in app's external files directory

## Key Dependencies

- **TensorFlow Lite 2.15.0** - For running Whisper models (note: newer versions cause crashes)
- **WebRTC VAD 2.0.9** - Voice Activity Detection
- **OpenCC4J 1.8.1** - Chinese text conversion
- **AndroidX libraries** - Standard Android support libraries

## Development Notes

- Target SDK: 34, Min SDK: 28
- Uses Java 11 compatibility
- Large heap enabled for TensorFlow Lite model loading
- Supports ARM64 and ARMv7 architectures only in release builds
- Foreground services with microphone type for audio processing
- Requires RECORD_AUDIO and other audio-related permissions

## Project Structure

```
app/src/main/java/com/whispertflite/
├── MainActivity.java                    # Main testing interface
├── WhisperInputMethodService.java       # IME service
├── WhisperRecognitionService.java       # Recognition service
├── WhisperRecognizeActivity.java        # Speech recognition activity
├── DownloadActivity.java                # Model download
├── asr/                                 # Speech recognition core
│   ├── Whisper.java                     # TensorFlow Lite model wrapper
│   ├── Recorder.java                    # Audio recording with VAD
│   ├── WhisperResult.java               # Recognition results
│   └── RecordBuffer.java                # Audio buffer management
└── utils/                               # Utility classes
    ├── InputLang.java                   # Language support
    ├── Downloader.java                  # Download utilities
    ├── HapticFeedback.java              # Haptic feedback
    └── WhisperUtil.java                 # General utilities
```

## Testing

No specific test framework is configured. Testing is primarily done through:
1. Installing the debug APK on a device
2. Enabling the IME in Android settings
3. Testing voice input in various applications
4. Using the main activity for standalone testing