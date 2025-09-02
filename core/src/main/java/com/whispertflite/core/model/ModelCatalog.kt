package com.whispertflite.core.model

import kotlinx.serialization.Serializable

/**
 * Model catalog containing information about available Whisper models
 * Based on OpenAI Whisper model specifications and whisper.cpp compatibility
 */
@Serializable
data class WhisperModel(
    val id: String,
    val name: String,
    val sizeMB: Int,
    val recommendedRAMGB: Int,
    val multilingual: Boolean,
    val description: String,
    val languages: List<String>,
    val speedLevel: SpeedLevel,
    val accuracyLevel: AccuracyLevel,
    val downloadUrl: String,
    val checksum: String,
    val filename: String
)

enum class SpeedLevel {
    FASTEST, FAST, BALANCED, SLOW, SLOWEST
}

enum class AccuracyLevel {
    LOW, GOOD, HIGH, HIGHER, BEST
}

object ModelCatalog {
    
    /**
     * All available Whisper models in the catalog
     */
    val models = listOf(
        WhisperModel(
            id = "tiny",
            name = "Tiny",
            sizeMB = 75,
            recommendedRAMGB = 2,
            multilingual = false,
            description = "English only, fastest processing, low accuracy",
            languages = listOf("en"),
            speedLevel = SpeedLevel.FASTEST,
            accuracyLevel = AccuracyLevel.LOW,
            downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin",
            checksum = "bd577a113a864445d4c299885e0cb97d4ba92b5f", // placeholder
            filename = "ggml-tiny.bin"
        ),
        
        WhisperModel(
            id = "tiny-multi",
            name = "Tiny Multilingual",
            sizeMB = 75,
            recommendedRAMGB = 2,
            multilingual = true,
            description = "Multilingual, fastest processing, low accuracy",
            languages = listOf("en", "zh", "de", "es", "ru", "ko", "fr", "ja", "pt", "tr", "pl", "ca", "nl", "ar", "sv", "it", "id", "hi", "fi", "vi", "he", "uk", "el", "ms", "cs", "ro", "da", "hu", "ta", "no", "th", "ur", "hr", "bg", "lt", "la", "mi", "ml", "cy", "sk", "te", "fa", "lv", "bn", "sr", "az", "sl", "kn", "et", "mk", "br", "eu", "is", "hy", "ne", "mn", "bs", "kk", "sq", "sw", "gl", "mr", "pa", "si", "km", "sn", "yo", "so", "af", "oc", "ka", "be", "tg", "sd", "gu", "am", "yi", "lo", "uz", "fo", "ht", "ps", "tk", "nn", "mt", "sa", "lb", "my", "bo", "tl", "mg", "as", "tt", "haw", "ln", "ha", "ba", "jw", "su"),
            speedLevel = SpeedLevel.FASTEST,
            accuracyLevel = AccuracyLevel.LOW,
            downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin",
            checksum = "bd577a113a864445d4c299885e0cb97d4ba92b5f", // placeholder
            filename = "ggml-tiny.en.bin"
        ),
        
        WhisperModel(
            id = "base",
            name = "Base",
            sizeMB = 142,
            recommendedRAMGB = 3,
            multilingual = false,
            description = "English only, fast processing, good for clean audio",
            languages = listOf("en"),
            speedLevel = SpeedLevel.FAST,
            accuracyLevel = AccuracyLevel.GOOD,
            downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin",
            checksum = "465678afde67b5e5a7a5d5c8cf6d8e5c4b2b4c2d", // placeholder
            filename = "ggml-base.en.bin"
        ),
        
        WhisperModel(
            id = "base-multi",
            name = "Base Multilingual",
            sizeMB = 142,
            recommendedRAMGB = 3,
            multilingual = true,
            description = "Multilingual, fast processing, good for clean audio",
            languages = listOf("en", "zh", "de", "es", "ru", "ko", "fr", "ja", "pt", "tr", "pl", "ca", "nl", "ar", "sv", "it", "id", "hi", "fi", "vi", "he", "uk", "el", "ms", "cs", "ro", "da", "hu", "ta", "no", "th", "ur", "hr", "bg", "lt", "la", "mi", "ml", "cy", "sk", "te", "fa", "lv", "bn", "sr", "az", "sl", "kn", "et", "mk", "br", "eu", "is", "hy", "ne", "mn", "bs", "kk", "sq", "sw", "gl", "mr", "pa", "si", "km", "sn", "yo", "so", "af", "oc", "ka", "be", "tg", "sd", "gu", "am", "yi", "lo", "uz", "fo", "ht", "ps", "tk", "nn", "mt", "sa", "lb", "my", "bo", "tl", "mg", "as", "tt", "haw", "ln", "ha", "ba", "jw", "su"),
            speedLevel = SpeedLevel.FAST,
            accuracyLevel = AccuracyLevel.GOOD,
            downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin",
            checksum = "465678afde67b5e5a7a5d5c8cf6d8e5c4b2b4c2d", // placeholder
            filename = "ggml-base.bin"
        ),
        
        WhisperModel(
            id = "small",
            name = "Small",
            sizeMB = 466,
            recommendedRAMGB = 4,
            multilingual = true,
            description = "Multilingual, balanced speed and accuracy",
            languages = listOf("en", "zh", "de", "es", "ru", "ko", "fr", "ja", "pt", "tr", "pl", "ca", "nl", "ar", "sv", "it", "id", "hi", "fi", "vi", "he", "uk", "el", "ms", "cs", "ro", "da", "hu", "ta", "no", "th", "ur", "hr", "bg", "lt", "la", "mi", "ml", "cy", "sk", "te", "fa", "lv", "bn", "sr", "az", "sl", "kn", "et", "mk", "br", "eu", "is", "hy", "ne", "mn", "bs", "kk", "sq", "sw", "gl", "mr", "pa", "si", "km", "sn", "yo", "so", "af", "oc", "ka", "be", "tg", "sd", "gu", "am", "yi", "lo", "uz", "fo", "ht", "ps", "tk", "nn", "mt", "sa", "lb", "my", "bo", "tl", "mg", "as", "tt", "haw", "ln", "ha", "ba", "jw", "su"),
            speedLevel = SpeedLevel.BALANCED,
            accuracyLevel = AccuracyLevel.HIGH,
            downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin",
            checksum = "55356b97d7c18e3af3de8c8fb6e3c5c4a1b5a4b3", // placeholder
            filename = "ggml-small.bin"
        ),
        
        WhisperModel(
            id = "medium",
            name = "Medium",
            sizeMB = 1460,
            recommendedRAMGB = 6,
            multilingual = true,
            description = "Multilingual, higher accuracy, slower processing",
            languages = listOf("en", "zh", "de", "es", "ru", "ko", "fr", "ja", "pt", "tr", "pl", "ca", "nl", "ar", "sv", "it", "id", "hi", "fi", "vi", "he", "uk", "el", "ms", "cs", "ro", "da", "hu", "ta", "no", "th", "ur", "hr", "bg", "lt", "la", "mi", "ml", "cy", "sk", "te", "fa", "lv", "bn", "sr", "az", "sl", "kn", "et", "mk", "br", "eu", "is", "hy", "ne", "mn", "bs", "kk", "sq", "sw", "gl", "mr", "pa", "si", "km", "sn", "yo", "so", "af", "oc", "ka", "be", "tg", "sd", "gu", "am", "yi", "lo", "uz", "fo", "ht", "ps", "tk", "nn", "mt", "sa", "lb", "my", "bo", "tl", "mg", "as", "tt", "haw", "ln", "ha", "ba", "jw", "su"),
            speedLevel = SpeedLevel.SLOW,
            accuracyLevel = AccuracyLevel.HIGHER,
            downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-medium.bin",
            checksum = "fd9727b6e1217c2f614027e6336b9ee0d4567b8a", // placeholder
            filename = "ggml-medium.bin"
        ),
        
        WhisperModel(
            id = "large-v3",
            name = "Large v3",
            sizeMB = 2950,
            recommendedRAMGB = 8,
            multilingual = true,
            description = "Multilingual, best accuracy, slowest processing",
            languages = listOf("en", "zh", "de", "es", "ru", "ko", "fr", "ja", "pt", "tr", "pl", "ca", "nl", "ar", "sv", "it", "id", "hi", "fi", "vi", "he", "uk", "el", "ms", "cs", "ro", "da", "hu", "ta", "no", "th", "ur", "hr", "bg", "lt", "la", "mi", "ml", "cy", "sk", "te", "fa", "lv", "bn", "sr", "az", "sl", "kn", "et", "mk", "br", "eu", "is", "hy", "ne", "mn", "bs", "kk", "sq", "sw", "gl", "mr", "pa", "si", "km", "sn", "yo", "so", "af", "oc", "ka", "be", "tg", "sd", "gu", "am", "yi", "lo", "uz", "fo", "ht", "ps", "tk", "nn", "mt", "sa", "lb", "my", "bo", "tl", "mg", "as", "tt", "haw", "ln", "ha", "ba", "jw", "su"),
            speedLevel = SpeedLevel.SLOWEST,
            accuracyLevel = AccuracyLevel.BEST,
            downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3.bin",
            checksum = "ad82bf6a9043ceed055076d0c39101a3aaa8c4e3", // placeholder
            filename = "ggml-large-v3.bin"
        )
    )
    
    /**
     * Get model by ID
     */
    fun getModelById(id: String): WhisperModel? = models.find { it.id == id }
    
    /**
     * Get all multilingual models
     */
    fun getMultilingualModels(): List<WhisperModel> = models.filter { it.multilingual }
    
    /**
     * Get all English-only models
     */
    fun getEnglishOnlyModels(): List<WhisperModel> = models.filter { !it.multilingual }
    
    /**
     * Get models suitable for a device with specific RAM
     */
    fun getModelsForRAM(availableRAMGB: Int): List<WhisperModel> = 
        models.filter { it.recommendedRAMGB <= availableRAMGB }
        
    /**
     * Get models that support a specific language
     */
    fun getModelsForLanguage(language: String): List<WhisperModel> =
        models.filter { it.languages.contains(language) }
}