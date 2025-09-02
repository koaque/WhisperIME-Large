# WhisperIME v1.1 - Developer Guide

## Overview

WhisperIME v1.1 is a complete revamp of the voice input method app with:
- **Multimodal capture**: Audio-to-text (primary) + OCR + screen text capture
- **Material 3 UI**: Modern Compose-based interface
- **Modular architecture**: Separated into `app`, `core`, and `native` modules
- **Multiple engines**: whisper.cpp (native), TensorFlow Lite (legacy), Android SpeechRecognizer (fallback)
- **Smart output routing**: Direct input or buffered mode with paste-on-demand

## Project Structure

```
WhisperIME-Large/
├── app/                    # Main Android application module
│   ├── src/main/java/com/whispertflite/
│   │   ├── ui/            # Compose UI screens
│   │   ├── ime/           # InputMethodService implementation  
│   │   └── legacy/        # Adapted legacy Java code
├── core/                  # Core business logic module
│   ├── src/main/java/com/whispertflite/core/
│   │   ├── model/         # Model catalog & repository
│   │   ├── audio/         # Audio recording engine
│   │   ├── whisper/       # Speech recognition engines
│   │   ├── output/        # Output routing system
│   │   ├── ocr/           # OCR functionality
│   │   └── settings/      # Settings management
└── native/                # Native whisper.cpp integration (placeholder)
    ├── src/main/cpp/      # C++ JNI code
    └── src/main/java/     # Kotlin JNI wrappers
```

## Build Instructions

### Prerequisites

- **Android Studio**: Iguana (2023.2.1) or newer
- **JDK**: 11 or higher
- **Android SDK**: API 24-34
- **NDK**: 25.1.8937393 or newer (for native module)

### Quick Start

```bash
# Clone and enter project
cd WhisperIME-Large

# Build debug version
./gradlew assembleDebug

# Install to connected device
./gradlew installDebug

# Run tests
./gradlew test

# Build release (uses debug signing for now)
./gradlew assembleRelease
```

### Module-specific builds

```bash
# Build only core module
./gradlew :core:assembleDebug

# Build only native module  
./gradlew :native:assembleDebug

# Clean all modules
./gradlew clean
```

## Current Implementation Status

### ✅ Completed
- **Module structure**: app, core, native modules created
- **Gradle setup**: Compose BOM, Material 3, Kotlin serialization
- **Model catalog**: Full Whisper model definitions with download URLs
- **Model repository**: Download manager with WorkManager, checksums, resume support
- **Audio engine**: Recording with VAD, noise suppression, audio level monitoring
- **Whisper engines**: Interface + Fake (testing) + TensorFlow (legacy adapter) + SpeechRecognizer (fallback)
- **Output router**: Direct mode (InputConnection) + Buffered mode with paste-on-demand

### 🚧 In Progress (Next Steps)
- **WhisperImeService**: Material 3 keyboard with mic, language, up arrow buttons
- **Compose UI screens**: Onboarding, ModelPicker, Transcript, Settings, About
- **Settings system**: DataStore preferences integration
- **OCR engine**: ML Kit integration for image text recognition
- **Legacy migration**: Kotlin adapters for existing Java business logic

### 📋 TODO (v1.2)
- **whisper.cpp integration**: Replace placeholder native module
- **Advanced features**: Punctuation model, translation, diarization
- **Long-form processing**: Segmentation for extended audio

## Configuration & Customization

### Package Name & Signing

The app currently uses:
- **Package**: `org.woheller69.whisper` (in `app/build.gradle`)
- **Namespace**: `com.whispertflite` (consistent across modules)
- **Signing**: Debug keystore (for development)

To change for production:

1. **Update package name** in `app/build.gradle`:
   ```gradle
   defaultConfig {
       applicationId "your.package.name"
   ```

2. **Add release signing** in `app/build.gradle`:
   ```gradle
   android {
       signingConfigs {
           release {
               storeFile file('path/to/your/keystore.jks')
               storePassword 'your_store_password'
               keyAlias 'your_key_alias'
               keyPassword 'your_key_password'
           }
       }
       buildTypes {
           release {
               signingConfig signingConfigs.release
   ```

### Model URLs & Checksums

Models are defined in `core/src/main/java/com/whispertflite/core/model/ModelCatalog.kt`.

**Important**: The current checksums are placeholders. Before production:

1. Download actual models from HuggingFace
2. Calculate real SHA-1 checksums: `sha1sum ggml-model.bin`
3. Update `ModelCatalog.kt` with real checksums

### Engine Selection Priority

1. **Native whisper.cpp** (when implemented) - best performance
2. **TensorFlow Lite** (legacy) - current fallback for existing models
3. **Android SpeechRecognizer** - system fallback, requires network

Toggle in Settings → Speech Engine.

## Development Workflow

### Testing on Device

1. **Enable keyboard**:
   ```
   Settings → System → Languages & input → On-screen keyboard → Manage keyboards
   → Enable "Whisper Voice Input"
   ```

2. **Switch to keyboard**:
   ```
   In any text field → Tap keyboard switcher → Select WhisperIME
   ```

3. **Test modes**:
   - **Direct**: Hold mic → speak → see real-time text in field
   - **Buffered**: Hold mic → speak → text in buffer → tap ↑ → paste once

### Debugging

```bash
# View logs
adb logcat | grep -E "(Whisper|WhisperIME|AudioEngine|ModelRepository)"

# Clear app data
adb shell pm clear org.woheller69.whisper

# Check permissions
adb shell dumpsys package org.woheller69.whisper | grep permission
```

### Model Testing

For development without large model downloads:

1. **Use FakeWhisperEngine**: Returns mock transcription results instantly
2. **Enable in settings**: Toggle "Use fake engine for testing"  
3. **Emulator friendly**: No model files or native libs required

## Known Issues & Workarounds

### Current Limitations

1. **Native module**: whisper.cpp integration is placeholder only
2. **Legacy integration**: TensorFlow adapter needs buffer management refinement
3. **OCR**: ML Kit integration not yet implemented
4. **UI**: Compose screens scaffolded but not complete

### Workarounds

- **For testing**: Use FakeWhisperEngine or SpeechRecognizer fallback
- **For models**: Download manually and place in `files/models/` directory
- **For IME**: XML-based view works for basic functionality

## Architecture Notes

### Why Three Modules?

- **app**: UI, Activities, IME service - Android-specific components
- **core**: Business logic, engines, models - pure Kotlin, testable
- **native**: JNI wrappers for whisper.cpp - performance-critical C++

### Engine Pattern

All speech recognition engines implement `WhisperEngine` interface:
```kotlin
interface WhisperEngine {
    suspend fun initialize(modelPath: String, options: WhisperOptions): Boolean
    fun transcribe(audioSamples: Flow<FloatArray>): Flow<WhisperResult>
    suspend fun transcribeBuffer(samples: FloatArray): WhisperResult
    suspend fun release()
}
```

This allows hot-swapping between native, TensorFlow, and system recognizers.

### Output Routing

Two modes supported:
- **Direct**: `audioSamples → WhisperEngine → OutputRouter → InputConnection`
- **Buffered**: `audioSamples → WhisperEngine → OutputRouter → Buffer → User tap → InputConnection`

## Next Development Session

1. **Complete IME UI**: Material 3 keyboard buttons with VU meter
2. **Implement settings**: DataStore integration for all preferences  
3. **Add OCR**: ML Kit Text Recognition for image capture
4. **Create Compose screens**: Complete the main app interface
5. **Test end-to-end**: Full workflow from model download to transcription

The foundation is solid - focus on UI completion and connecting the pieces!

## Support

- **Build issues**: Check Android Studio sync, SDK versions, NDK setup
- **Runtime crashes**: Check permissions (RECORD_AUDIO, FOREGROUND_SERVICE)  
- **Model downloads**: Verify network, storage space, correct URLs
- **IME not appearing**: Enable in system settings, check app not disabled

Ready to continue development! 🎙️