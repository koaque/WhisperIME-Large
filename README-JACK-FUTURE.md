# WhisperIME v1.1 - Customization & Extension Guide

## 🎨 **Visual Customization & Branding**

### App Icons & Logo

**Current Status**: Using default Android launcher icons
**Location**: `app/src/main/res/mipmap-*` directories

#### Changing App Icons
1. **Create new icon assets**:
   ```bash
   # Use Android Studio's Image Asset Studio or manual replacement
   # Required sizes: 48dp, 72dp, 96dp, 144dp, 192dp, 512dp
   ```

2. **Replace existing files**:
   ```
   app/src/main/res/
   ├── mipmap-hdpi/ic_launcher.png (72x72)
   ├── mipmap-mdpi/ic_launcher.png (48x48) 
   ├── mipmap-xhdpi/ic_launcher.png (96x96)
   ├── mipmap-xxhdpi/ic_launcher.png (144x144)
   ├── mipmap-xxxhdpi/ic_launcher.png (192x192)
   └── mipmap-anydpi-v26/
       ├── ic_launcher.xml (Adaptive icon)
       └── ic_launcher_background.xml
   ```

3. **Update adaptive icon** (recommended):
   ```xml
   <!-- ic_launcher.xml -->
   <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
       <background android:drawable="@drawable/ic_launcher_background"/>
       <foreground android:drawable="@drawable/ic_launcher_foreground"/>
   </adaptive-icon>
   ```

4. **Design guidelines**:
   - **Concept**: Voice/audio-related imagery (microphone, sound waves, speech bubbles)
   - **Style**: Modern, minimal, professional
   - **Colors**: Align with app theme (blues, teals, voice-green accents)

### Color Scheme Customization

**Location**: `app/src/main/java/com/whispertflite/ui/theme/WhisperTheme.kt`

#### Primary Brand Colors
```kotlin
// Edit these in WhisperTheme.kt
private val WhisperBlue = Color(0xFF1565C0)        // Primary brand color
private val WhisperTeal = Color(0xFF00838F)        // Secondary accent
private val VoiceGreen = Color(0xFF2E7D32)         // Voice activity color
```

#### Audio Visualization Colors  
```kotlin
// In AudioColors object
object AudioColors {
    val voiceActive = Color(0xFF4CAF50)            // Active recording
    val voiceInactive = Color(0xFF81C784)          // Inactive state
    val audioLevel1 = Color(0xFF2E7D32)            // Low audio level
    val audioLevel5 = Color(0xFFD32F2F)            // High audio level
}
```

#### Dynamic Color Support
Enable/disable system dynamic colors:
```kotlin
// In WhisperTheme composable
@Composable
fun WhisperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,  // <- Toggle this
    content: @Composable () -> Unit
)
```

### Typography & Text Styling

**Location**: `app/src/main/java/com/whispertflite/ui/theme/WhisperTheme.kt`

#### Custom Font Integration
1. **Add font files** to `app/src/main/res/font/`:
   ```
   res/font/
   ├── whisper_regular.ttf
   ├── whisper_medium.ttf
   └── whisper_bold.ttf
   ```

2. **Create font family**:
   ```kotlin
   val WhisperFontFamily = FontFamily(
       Font(R.font.whisper_regular, FontWeight.Normal),
       Font(R.font.whisper_medium, FontWeight.Medium),
       Font(R.font.whisper_bold, FontWeight.Bold)
   )
   ```

3. **Update typography**:
   ```kotlin
   private val WhisperTypography = Typography(
       displayLarge = TextStyle(fontFamily = WhisperFontFamily),
       headlineLarge = TextStyle(fontFamily = WhisperFontFamily),
       // ... apply to all text styles
   )
   ```

### Design System Tokens

**Location**: `WhisperDesignTokens` object in theme files

#### Spacing System
```kotlin
object WhisperDesignTokens {
    val SpacingXXS = 2.dp     // Micro spacing
    val SpacingXS = 4.dp      // Tiny spacing  
    val SpacingS = 8.dp       // Small spacing
    val SpacingM = 16.dp      // Medium spacing (base)
    val SpacingL = 24.dp      // Large spacing
    val SpacingXL = 32.dp     // Extra large
    val SpacingXXL = 48.dp    // Maximum spacing
}
```

#### Component Dimensions
```kotlin
// Microphone button sizing
val MicButtonSize = 56.dp          // Standard size
val MicButtonSizeSmall = 40.dp     // Compact mode

// VU meter sizing  
val VUMeterHeight = 4.dp           // Thin meter
val VUMeterHeightLarge = 8.dp      // Thick meter
```

## 🔧 **Feature Extensions**

### Adding New Speech Engines

1. **Create engine class** implementing `WhisperEngine`:
   ```kotlin
   class CustomWhisperEngine(private val context: Context) : WhisperEngine {
       override suspend fun initialize(modelPath: String, options: WhisperOptions): Boolean {
           // Your initialization logic
       }
       
       override fun transcribe(audioSamples: Flow<FloatArray>): Flow<WhisperResult> {
           // Your transcription pipeline
       }
   }
   ```

2. **Register in settings**:
   ```kotlin
   // Add to SettingsScreen.kt engine selector
   SpeechEngineOption(
       title = "Custom Engine",
       subtitle = "Your custom speech recognition",
       selected = uiState.speechEngineType == "CUSTOM",
       onSelect = { viewModel.setSpeechEngineType("CUSTOM") },
       icon = Icons.Default.Extension
   )
   ```

3. **Update engine factory**:
   ```kotlin
   // In WhisperImeService.kt setupWhisperEngine()
   whisperEngine = when (engineType) {
       "CUSTOM" -> CustomWhisperEngine(this@WhisperImeService)
       // ... existing engines
   }
   ```

### Custom Audio Processing

**Location**: `core/src/main/java/com/whispertflite/core/audio/AudioEngine.kt`

#### Adding Audio Effects
```kotlin
class AudioEngine {
    private var audioEffects = mutableListOf<AudioEffect>()
    
    fun addAudioEffect(effect: AudioEffect) {
        audioEffects.add(effect)
    }
    
    private suspend fun processAudioEffects(samples: FloatArray): FloatArray {
        var processed = samples
        audioEffects.forEach { effect ->
            processed = effect.process(processed)
        }
        return processed
    }
}
```

#### Custom VAD Implementation
```kotlin
class CustomVADEngine : VoiceActivityDetector {
    override fun isSpeech(samples: ShortArray): Boolean {
        // Your VAD algorithm
        return detectVoiceActivity(samples)
    }
}
```

### Adding New Output Modes

1. **Extend OutputMode enum**:
   ```kotlin
   enum class OutputMode {
       DIRECT,
       BUFFERED,
       CUSTOM_MODE  // Your new mode
   }
   ```

2. **Create custom sink**:
   ```kotlin
   class CustomOutputSink : OutputSink {
       override fun commitText(text: String): Boolean {
           // Your custom output logic
           return true
       }
   }
   ```

3. **Update OutputRouter**:
   ```kotlin
   private fun processCustomMode(result: TranscriptionResult) {
       // Handle your custom output mode
   }
   ```

### Model Format Support

**Location**: `core/src/main/java/com/whispertflite/core/model/ModelCatalog.kt`

#### Adding New Model Formats
```kotlin
data class WhisperModel(
    // ... existing fields
    val modelFormat: ModelFormat,  // Add format support
    val quantization: String?      // Add quantization info
)

enum class ModelFormat {
    GGML,           // whisper.cpp format
    TFLITE,         // TensorFlow Lite
    ONNX,           // ONNX format
    PYTORCH_MOBILE  // PyTorch Mobile
}
```

#### Custom Model Downloader
```kotlin
class CustomModelDownloader {
    suspend fun downloadModel(
        model: WhisperModel,
        onProgress: (Float) -> Unit
    ): Result<File> {
        // Your download implementation
    }
}
```

## 🏗️ **Architecture Extensions**

### Adding New UI Screens

1. **Create screen composable**:
   ```kotlin
   @Composable
   fun CustomScreen(
       onNavigateBack: () -> Unit,
       viewModel: CustomViewModel = viewModel()
   ) {
       // Your screen implementation
   }
   ```

2. **Add navigation route**:
   ```kotlin
   // In navigation setup
   composable("custom_screen") {
       CustomScreen(
           onNavigateBack = { navController.popBackStack() }
       )
   }
   ```

3. **Update navigation drawer/menu**:
   ```kotlin
   // Add menu item
   ListTile(
       icon = Icons.Default.Extension,
       title = "Custom Feature",
       onClick = { navController.navigate("custom_screen") }
   )
   ```

### Database Extensions

**Location**: `core` module (add Room database)

#### Adding Data Persistence
1. **Create entities**:
   ```kotlin
   @Entity(tableName = "transcription_history")
   data class TranscriptionRecord(
       @PrimaryKey val id: String,
       val text: String,
       val timestamp: Long,
       val confidence: Float,
       val language: String
   )
   ```

2. **Create DAOs**:
   ```kotlin
   @Dao
   interface TranscriptionDao {
       @Query("SELECT * FROM transcription_history ORDER BY timestamp DESC")
       fun getAllTranscriptions(): Flow<List<TranscriptionRecord>>
       
       @Insert
       suspend fun insertTranscription(record: TranscriptionRecord)
   }
   ```

3. **Setup database**:
   ```kotlin
   @Database(
       entities = [TranscriptionRecord::class],
       version = 1,
       exportSchema = false
   )
   abstract class WhisperDatabase : RoomDatabase() {
       abstract fun transcriptionDao(): TranscriptionDao
   }
   ```

### Custom Keyboards & Input Methods

#### Creating Additional Keyboard Layouts
1. **Design XML layout**:
   ```xml
   <!-- res/layout/ime_custom_keyboard.xml -->
   <LinearLayout>
       <!-- Your keyboard layout -->
   </LinearLayout>
   ```

2. **Handle keyboard switching**:
   ```kotlin
   class WhisperImeService {
       private fun switchToKeyboard(keyboardType: KeyboardType) {
           val layoutRes = when (keyboardType) {
               KeyboardType.VOICE -> R.layout.ime_whisper_keyboard
               KeyboardType.CUSTOM -> R.layout.ime_custom_keyboard
           }
           // Switch layout
       }
   }
   ```

## 🔌 **Integration Patterns**

### Webhook/API Integration

```kotlin
class WebhookManager {
    suspend fun sendTranscription(
        text: String,
        confidence: Float,
        webhookUrl: String
    ) {
        val payload = TranscriptionWebhook(
            text = text,
            confidence = confidence,
            timestamp = System.currentTimeMillis()
        )
        
        httpClient.post(webhookUrl) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
    }
}
```

### Cloud Sync Integration

```kotlin
class CloudSyncManager {
    suspend fun syncTranscriptionHistory() {
        // Sync with Google Drive, Dropbox, etc.
    }
    
    suspend fun uploadModel(modelFile: File) {
        // Upload custom models to cloud storage
    }
}
```

### Accessibility Integration

```kotlin
class AccessibilityManager {
    fun announceTranscription(text: String) {
        if (isScreenReaderActive()) {
            announceForAccessibility(text)
        }
    }
    
    fun provideHapticFeedback(pattern: HapticPattern) {
        // Custom haptic patterns for different states
    }
}
```

## 🧪 **Testing Extensions**

### Unit Testing Setup

```kotlin
class WhisperEngineTest {
    @Test
    fun `test transcription accuracy`() = runTest {
        val engine = FakeWhisperEngine()
        engine.initialize("", WhisperOptions())
        
        val result = engine.transcribeBuffer(generateTestAudio())
        
        assertThat(result.text).isNotEmpty()
        assertThat(result.confidence).isGreaterThan(0.5f)
    }
}
```

### UI Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun onboarding_completes_successfully() {
        composeTestRule.setContent {
            WhisperTheme {
                OnboardingScreen(onComplete = {})
            }
        }
        
        // Test onboarding flow
        composeTestRule.onNodeWithText("Next").performClick()
    }
}
```

## 🚀 **Deployment & Distribution**

### Build Variants

**Location**: `app/build.gradle`

```gradle
android {
    buildTypes {
        debug {
            applicationIdSuffix ".debug"
            debuggable true
        }
        
        beta {
            applicationIdSuffix ".beta"
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
        }
        
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
        }
    }
    
    flavorDimensions "version"
    productFlavors {
        free {
            dimension "version"
            applicationIdSuffix ".free"
        }
        
        pro {
            dimension "version"
            applicationIdSuffix ".pro"
        }
    }
}
```

### Signing Configuration

```gradle
android {
    signingConfigs {
        release {
            storeFile file('path/to/keystore.jks')
            storePassword System.getenv("KEYSTORE_PASSWORD")
            keyAlias System.getenv("KEY_ALIAS")  
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }
}
```

### Gradle Publishing

```gradle
// For library modules
apply plugin: 'maven-publish'

publishing {
    publications {
        release(MavenPublication) {
            from components.release
            
            groupId = 'com.whispertflite'
            artifactId = 'core'
            version = '1.1.0'
        }
    }
}
```

## 🔍 **Debugging & Monitoring**

### Advanced Logging

```kotlin
// Custom log categories
class WhisperLogger {
    fun logModelPerformance(
        modelId: String,
        inputDuration: Long,
        processingTime: Long,
        outputLength: Int
    ) {
        if (isExtensiveLoggingEnabled) {
            d("ModelPerformance", 
              "Model: $modelId, Input: ${inputDuration}ms, " +
              "Processing: ${processingTime}ms, Output: $outputLength chars")
        }
    }
    
    fun logUserInteraction(action: String, context: Map<String, Any>) {
        if (isExtensiveLoggingEnabled) {
            d("UserInteraction", "$action - ${context.entries.joinToString()}")
        }
    }
}
```

### Performance Monitoring

```kotlin
class PerformanceMonitor {
    fun measureTranscriptionLatency(): Long {
        return System.currentTimeMillis() - transcriptionStartTime
    }
    
    fun trackMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        logger.d("Performance", "Memory usage: ${usedMemory / 1024 / 1024}MB")
    }
}
```

### Crash Reporting Integration

```kotlin
// Add to Application class
class WhisperApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Setup crash reporting (Firebase, Bugsnag, etc.)
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            logger.e("CrashReport", "Uncaught exception in $thread", exception)
            // Send to crash reporting service
        }
    }
}
```

## 📱 **Platform-Specific Customizations**

### Wear OS Support

```kotlin
class WearWhisperEngine : WhisperEngine {
    override suspend fun initialize(modelPath: String, options: WhisperOptions): Boolean {
        // Optimizations for Wear OS constraints
        return initializeLightweightModel()
    }
}
```

### TV/Android Auto Integration

```kotlin
class AndroidAutoIntegration {
    fun setupCarInterface() {
        // Voice input integration for Android Auto
    }
}
```

### Tablet Optimizations

```kotlin
@Composable
fun TabletOptimizedLayout() {
    if (isTablet()) {
        // Two-pane layout for tablets
        TwoPaneLayout()
    } else {
        // Single pane for phones
        SinglePaneLayout()
    }
}
```

---

## 🎯 **Quick Customization Checklist**

### Essential Branding (30 minutes)
- [ ] Replace app icons in `/res/mipmap-*`
- [ ] Update primary colors in `WhisperTheme.kt`
- [ ] Change app name in `/res/values/strings.xml`
- [ ] Update package name in `build.gradle`

### Visual Polish (2 hours)
- [ ] Customize audio visualization colors
- [ ] Add custom fonts
- [ ] Adjust component sizing tokens
- [ ] Create branded splash screen

### Feature Extensions (1 day)
- [ ] Add new speech engine
- [ ] Implement custom output mode
- [ ] Add database persistence
- [ ] Create additional UI screens

### Advanced Integration (1 week)
- [ ] Cloud sync capability
- [ ] Webhook/API integration
- [ ] Advanced analytics
- [ ] Multi-platform support

---

**Ready to customize? Start with the Essential Branding checklist and work your way up! Each section is designed to be modular and independent.** 🚀