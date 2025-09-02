# Changelog

All notable changes to WhisperIME will be documented in this file.

## [1.1.0] - 2025-01-XX (In Progress)

### 🎯 Major Features
- **Complete architecture overhaul**: Migrated from monolithic Java to modular Kotlin architecture
- **Material 3 UI**: Modern Compose-based interface with dynamic theming
- **Multimodal input**: Added OCR (Optical Character Recognition) for image text extraction
- **Smart output routing**: Choose between Direct mode (real-time input) and Buffered mode (paste on demand)
- **Multiple speech engines**: whisper.cpp (native), TensorFlow Lite (legacy), Android SpeechRecognizer (fallback)
- **Advanced model management**: Download, verify, and manage multiple Whisper model sizes

### 🏗️ Architecture Changes
- **Modular design**: Split into `app`, `core`, and `native` modules
- **New minimum SDK**: Lowered to API 24 (Android 7.0) for broader device support
- **Kotlin-first**: Complete migration from Java with modern coroutines and Flow APIs
- **Repository pattern**: Clean separation of data layer with Room database integration

### 🔧 Audio & Recognition
- **Enhanced AudioEngine**: Improved VAD (Voice Activity Detection) with WebRTC
- **Multiple engine support**: 
  - FakeWhisperEngine for testing and emulator development
  - TensorFlowWhisperEngine adapter for existing TFLite models  
  - SpeechRecognizerEngine for system fallback
  - Native whisper.cpp integration (placeholder for future implementation)
- **Better audio processing**: Optional noise suppression, configurable endpointing sensitivity
- **Real-time audio level**: VU meter display with voice detection feedback

### 📱 User Interface
- **Material 3 design**: Consistent theming with light/dark modes
- **Compose screens**: 
  - Onboarding flow for new users
  - Model picker with download progress and device requirements
  - Live transcript screen with partial/final results
  - Comprehensive settings with organized categories
  - Diagnostics and about screens
- **Enhanced IME keyboard**: 
  - Material 3 styled buttons (mic, language selector, paste arrow)
  - Visual VU meter integration
  - Support for both push-to-talk and continuous modes

### ⚙️ Settings & Configuration  
- **DataStore integration**: Modern preference storage replacing SharedPreferences
- **Comprehensive options**:
  - Output routing mode (Direct/Buffered)
  - Language selection with auto-detection
  - Audio processing toggles (VAD, noise suppression)
  - Transcription options (punctuation, profanity masking)
  - UI preferences (haptic feedback, VU meter visibility)
  - Text formatting (prefix/suffix, case conversion)
  - Download management (WiFi-only, resume support)

### 🌐 Model Management
- **Whisper model catalog**: Comprehensive database of available models
  - Tiny (75MB), Base (142MB), Small (466MB), Medium (1460MB), Large-v3 (2950MB)
  - Both English-only and multilingual variants
  - RAM requirements and performance characteristics
  - Language support matrix for each model
- **Download system**:
  - WorkManager-based background downloads
  - Resume interrupted downloads
  - SHA-1 checksum verification  
  - Storage usage monitoring
  - Batch download capabilities

### 🔒 Privacy & Permissions
- **On-device processing**: No network transcription by default
- **Minimal permissions**: Only RECORD_AUDIO and FOREGROUND_SERVICE required
- **Privacy-first design**: All speech processing happens locally
- **No telemetry**: Zero data collection or usage tracking

### 🛠️ Developer Experience
- **Testing framework**: FakeWhisperEngine for reliable automated testing
- **Emulator support**: Full functionality without native libraries
- **Debug tools**: Enhanced logging, audio diagnostics, performance monitoring
- **Module separation**: Clear boundaries between UI, business logic, and native code

### 🐛 Bug Fixes & Improvements
- **Memory management**: Better cleanup of audio resources and model loading
- **Crash prevention**: Comprehensive error handling throughout the pipeline  
- **Performance optimization**: Reduced CPU usage during idle periods
- **Battery optimization**: Proper foreground service lifecycle management

### 🔧 Technical Improvements
- **Gradle modernization**: Updated to latest Android Gradle Plugin and dependencies
- **Compose BOM**: Centralized version management for Compose libraries
- **Kotlinx Serialization**: Type-safe JSON handling for model metadata
- **Coroutines integration**: Async programming throughout the stack
- **Flow-based architecture**: Reactive streams for audio and transcription data

### 📚 Documentation
- **README-JACK.md**: Comprehensive developer guide for building and extending
- **Architecture documentation**: Clear module boundaries and API contracts
- **Code comments**: Extensive inline documentation for complex algorithms
- **Build instructions**: Step-by-step setup for different development environments

### 🎯 v1.2 Roadmap (TODO)
- **Native whisper.cpp**: Replace TensorFlow Lite with optimized C++ implementation
- **Punctuation model**: Advanced punctuation and capitalization
- **Translation support**: Real-time language translation capabilities  
- **Speaker diarization**: Multi-speaker identification in transcripts
- **Long-form processing**: Efficient handling of extended audio sessions
- **Export functionality**: Save transcripts in multiple formats (TXT, SRT, VTT)

### ⚠️ Breaking Changes
- **Minimum SDK**: Changed from API 28 to API 24
- **Package structure**: Complete reorganization of codebase
- **Settings format**: Migration required from old SharedPreferences format
- **Model format**: New model catalog incompatible with v3.x models

### 🏷️ Version History
- **v3.1** (Previous): TensorFlow Lite implementation with basic IME
- **v1.1.0** (Current): Complete Kotlin rewrite with modular architecture
- **v1.2.0** (Planned): Native whisper.cpp integration and advanced features

---

## Migration from v3.1

### For Users
1. Uninstall the old version (v3.1) 
2. Install the new version (v1.1.0)
3. Re-enable the keyboard in system settings
4. Download models through the new model picker
5. Configure preferences in the updated settings screen

### For Developers  
1. The codebase has been completely restructured
2. Java code has been migrated to Kotlin
3. New module system requires updated build scripts
4. Settings API has changed - use WhisperSettings class instead of SharedPreferences
5. Engine interface has been redesigned - implement WhisperEngine for custom recognizers

### Data Migration
- Settings: Automatic migration on first launch
- Models: Re-download required due to new storage format  
- Transcription history: Not preserved (feature removed in v1.1)

---

*Note: Version 1.1.0 represents a complete architectural rewrite. While maintaining feature parity with v3.1, the internal implementation has been modernized for better maintainability and extensibility.*