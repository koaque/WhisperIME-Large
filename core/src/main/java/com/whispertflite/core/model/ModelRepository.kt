package com.whispertflite.core.model

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import okhttp3.*
import java.io.*
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import java.util.concurrent.TimeUnit

@Serializable
data class ModelStatus(
    val modelId: String,
    val state: ModelState,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val error: String? = null
)

enum class ModelState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED,
    ERROR,
    VERIFYING
}

/**
 * Repository for managing Whisper model downloads and storage
 */
class ModelRepository(
    private val context: Context,
    private val workManager: WorkManager = WorkManager.getInstance(context)
) {
    private val modelsDir = File(context.filesDir, "models")
    private val _modelStatus = MutableStateFlow<Map<String, ModelStatus>>(emptyMap())
    val modelStatus: StateFlow<Map<String, ModelStatus>> = _modelStatus.asStateFlow()

    init {
        // Ensure models directory exists
        modelsDir.mkdirs()
        // Initialize model status
        updateModelStatuses()
    }

    /**
     * Get the file path for a model
     */
    fun getModelFile(modelId: String): File {
        val model = ModelCatalog.getModelById(modelId) ?: throw IllegalArgumentException("Unknown model: $modelId")
        return File(modelsDir, model.filename)
    }

    /**
     * Check if a model is downloaded and verified
     */
    fun isModelAvailable(modelId: String): Boolean {
        val file = getModelFile(modelId)
        return file.exists() && file.length() > 0
    }

    /**
     * Download a model
     */
    fun downloadModel(modelId: String) {
        val model = ModelCatalog.getModelById(modelId) ?: return
        
        // Update status to downloading
        updateModelStatus(modelId, ModelStatus(modelId, ModelState.DOWNLOADING))

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val downloadRequest = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setInputData(workDataOf("model_id" to modelId))
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        workManager.enqueue(downloadRequest)

        // Observe work progress
        workManager.getWorkInfoByIdLiveData(downloadRequest.id).observeForever { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> {
                    val progress = workInfo.progress.getFloat("progress", 0f)
                    val downloaded = workInfo.progress.getLong("downloaded", 0L)
                    val total = workInfo.progress.getLong("total", 0L)
                    updateModelStatus(modelId, ModelStatus(
                        modelId, ModelState.DOWNLOADING, progress, downloaded, total
                    ))
                }
                WorkInfo.State.SUCCEEDED -> {
                    updateModelStatus(modelId, ModelStatus(modelId, ModelState.DOWNLOADED))
                }
                WorkInfo.State.FAILED -> {
                    val error = workInfo.outputData.getString("error") ?: "Download failed"
                    updateModelStatus(modelId, ModelStatus(modelId, ModelState.ERROR, error = error))
                }
                else -> { /* Other states */ }
            }
        }
    }

    /**
     * Delete a model
     */
    suspend fun deleteModel(modelId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = getModelFile(modelId)
            val deleted = !file.exists() || file.delete()
            if (deleted) {
                updateModelStatus(modelId, ModelStatus(modelId, ModelState.NOT_DOWNLOADED))
            }
            deleted
        } catch (e: Exception) {
            Log.e("ModelRepository", "Error deleting model $modelId", e)
            false
        }
    }

    /**
     * Verify model checksum
     */
    suspend fun verifyModel(modelId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val model = ModelCatalog.getModelById(modelId) ?: return@withContext false
            val file = getModelFile(modelId)
            
            if (!file.exists()) return@withContext false

            updateModelStatus(modelId, ModelStatus(modelId, ModelState.VERIFYING))

            val calculated = calculateSHA1(file)
            val expected = model.checksum
            
            val isValid = calculated.equals(expected, ignoreCase = true)
            
            if (isValid) {
                updateModelStatus(modelId, ModelStatus(modelId, ModelState.DOWNLOADED))
            } else {
                updateModelStatus(modelId, ModelStatus(modelId, ModelState.ERROR, error = "Checksum mismatch"))
                file.delete()
            }
            
            isValid
        } catch (e: Exception) {
            Log.e("ModelRepository", "Error verifying model $modelId", e)
            updateModelStatus(modelId, ModelStatus(modelId, ModelState.ERROR, error = e.message))
            false
        }
    }

    /**
     * Get total storage used by all models
     */
    fun getTotalStorageUsed(): Long {
        return modelsDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    /**
     * Get available storage space
     */
    fun getAvailableStorage(): Long {
        return modelsDir.freeSpace
    }

    private fun updateModelStatuses() {
        val statuses = ModelCatalog.models.associate { model ->
            val isAvailable = isModelAvailable(model.id)
            val state = if (isAvailable) ModelState.DOWNLOADED else ModelState.NOT_DOWNLOADED
            model.id to ModelStatus(model.id, state)
        }
        _modelStatus.value = statuses
    }

    private fun updateModelStatus(modelId: String, status: ModelStatus) {
        _modelStatus.value = _modelStatus.value.toMutableMap().apply {
            this[modelId] = status
        }
    }

    private suspend fun calculateSHA1(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-1")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }
}

/**
 * Worker class for downloading models in the background
 */
class ModelDownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val modelId = inputData.getString("model_id") ?: return@withContext Result.failure()
            val model = ModelCatalog.getModelById(modelId) ?: return@withContext Result.failure()
            
            val modelsDir = File(applicationContext.filesDir, "models")
            modelsDir.mkdirs()
            val targetFile = File(modelsDir, model.filename)
            val tempFile = File(modelsDir, "${model.filename}.tmp")

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(model.downloadUrl)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(workDataOf("error" to "HTTP ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(workDataOf("error" to "Empty response"))
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            // Resume download if temp file exists
            if (tempFile.exists()) {
                downloadedBytes = tempFile.length()
            }

            if (downloadedBytes > 0) {
                // Resume download
                val resumeRequest = Request.Builder()
                    .url(model.downloadUrl)
                    .addHeader("Range", "bytes=$downloadedBytes-")
                    .build()
                
                val resumeResponse = client.newCall(resumeRequest).execute()
                if (!resumeResponse.isSuccessful) {
                    return@withContext Result.failure(workDataOf("error" to "Resume failed"))
                }
            }

            body.byteStream().use { input ->
                tempFile.outputStream().use { output ->
                    if (downloadedBytes > 0) {
                        output.channel.position(downloadedBytes)
                    }
                    
                    val buffer = ByteArray(8192)
                    var read: Int
                    
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        
                        val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
                        setProgress(workDataOf(
                            "progress" to progress,
                            "downloaded" to downloadedBytes,
                            "total" to totalBytes
                        ))
                    }
                }
            }

            // Move temp file to final location
            if (targetFile.exists()) targetFile.delete()
            tempFile.renameTo(targetFile)

            Result.success()
        } catch (e: Exception) {
            Log.e("ModelDownloadWorker", "Download failed", e)
            Result.failure(workDataOf("error" to (e.message ?: "Download failed")))
        }
    }
}