package com.margelo.nitro.com.faceantispoof

import android.util.Log
import androidx.annotation.Keep
import com.facebook.proguard.annotations.DoNotStrip
import com.faceantispoof.FaceAntiSpoofManager
import com.margelo.nitro.camera.HybridFrameSpec
import com.margelo.nitro.camera.public.NativeFrame
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

/**
 * Nitro HybridObject frame processor plugin for VisionCamera v5.
 * Performs face anti-spoofing detection on camera frames.
 *
 * IMPORTANT: All heavy processing is offloaded to a dedicated background thread
 * to prevent frame drops and ensure smooth camera preview.
 */
@DoNotStrip
@Keep
class HybridFaceAntiSpoofPlugin : HybridFaceAntiSpoofPluginSpec() {

    companion object {
        private const val TAG = "FaceAntiSpoof"
    }

    private val processingExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "FaceAntiSpoofProcessor").apply { isDaemon = true }
    }

    private val latestResult: AtomicReference<FaceAntiSpoofResult> = AtomicReference(
        FaceAntiSpoofResult(
            neuralNetworkScore = 0.0,
            laplacianScore = 0.0,
            combinedScore = 0.0,
            confidence = 0.0,
            isLive = false,
            label = "Not Initialized",
            error = "Not initialized"
        )
    )

    private val isProcessing = AtomicReference(false)

    override fun initialize(): Boolean {
        return FaceAntiSpoofManager.isReady()
    }

    override fun isReady(): Boolean {
        return FaceAntiSpoofManager.isReady()
    }

    override fun cleanup() {
        try {
            processingExecutor.shutdown()
            if (!processingExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                Log.w(TAG, "Executor did not terminate in time, forcing shutdown")
                processingExecutor.shutdownNow()
            }
            FaceAntiSpoofManager.cleanup()
            Log.i(TAG, "Cleanup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}", e)
        }
    }

    override fun call(frame: HybridFrameSpec): FaceAntiSpoofResult {
        if (!FaceAntiSpoofManager.isReady()) {
            return latestResult.get()
        }

        if (!isProcessing.compareAndSet(false, true)) {
            return latestResult.get()
        }

        val nativeFrame = frame as? NativeFrame ?: run {
            isProcessing.set(false)
            val errorResult = FaceAntiSpoofResult(
                neuralNetworkScore = 0.0,
                laplacianScore = 0.0,
                combinedScore = 0.0,
                confidence = 0.0,
                isLive = false,
                label = "Error: Invalid frame type",
                error = "Expected NativeFrame"
            )
            latestResult.set(errorResult)
            return errorResult
        }

        val imageProxy = nativeFrame.image
        if (imageProxy == null) {
            isProcessing.set(false)
            val errorResult = FaceAntiSpoofResult(
                neuralNetworkScore = 0.0,
                laplacianScore = 0.0,
                combinedScore = 0.0,
                confidence = 0.0,
                isLive = false,
                label = "Error: ImageProxy is null",
                error = "ImageProxy is null"
            )
            latestResult.set(errorResult)
            return errorResult
        }

        // Extract YUV data immediately on the camera thread before the frame is recycled
        val nv21: ByteArray
        val width: Int
        val height: Int

        try {
            val extractResult = extractNV21(imageProxy)
            if (extractResult == null) {
                isProcessing.set(false)
                val errorResult = FaceAntiSpoofResult(
                    neuralNetworkScore = 0.0,
                    laplacianScore = 0.0,
                    combinedScore = 0.0,
                    confidence = 0.0,
                    isLive = false,
                    label = "Error: Failed to extract frame data",
                    error = "Failed to extract frame data"
                )
                latestResult.set(errorResult)
                return errorResult
            }
            nv21 = extractResult.first
            width = extractResult.second
            height = extractResult.third
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting frame data: ${e.message}", e)
            isProcessing.set(false)
            val errorResult = FaceAntiSpoofResult(
                neuralNetworkScore = 0.0,
                laplacianScore = 0.0,
                combinedScore = 0.0,
                confidence = 0.0,
                isLive = false,
                label = "Error: ${e.message}",
                error = e.message
            )
            latestResult.set(errorResult)
            return errorResult
        }

        // Offload heavy processing to background thread
        processingExecutor.submit {
            try {
                val faceAntispoof = FaceAntiSpoofManager.getInstance()
                    ?: throw Exception("FaceAntiSpoofing not initialized")

                val result = faceAntispoof.advancedAntiSpoofing(nv21, width, height)

                val resultMap = FaceAntiSpoofResult(
                    neuralNetworkScore = result.neuralNetworkScore.toDouble(),
                    laplacianScore = result.laplacianScore.toDouble(),
                    combinedScore = result.combinedScore.toDouble(),
                    confidence = result.confidence.toDouble(),
                    isLive = result.combinedScore > 0.5f && result.laplacianScore > com.faceantispoof.FaceAntiSpoofingAdvanced.LAPLACIAN_THRESHOLD,
                    label = if (result.combinedScore > 0.5f && result.laplacianScore > com.faceantispoof.FaceAntiSpoofingAdvanced.LAPLACIAN_THRESHOLD) "Live Face" else "Spoof Face",
                    error = null
                )

                latestResult.set(resultMap)
            } catch (e: Exception) {
                Log.e(TAG, "Processing error: ${e.message}", e)
                val errorMap = FaceAntiSpoofResult(
                    neuralNetworkScore = 0.0,
                    laplacianScore = 0.0,
                    combinedScore = 0.0,
                    confidence = 0.0,
                    isLive = false,
                    label = "Error: ${e.message}",
                    error = e.message
                )
                latestResult.set(errorMap)
            } finally {
                isProcessing.set(false)
            }
        }

        return latestResult.get()
    }

    private fun extractNV21(image: androidx.camera.core.ImageProxy): Triple<ByteArray, Int, Int>? {
        val planes = image.planes
        if (planes == null || planes.size < 3) {
            Log.e(TAG, "Invalid frame planes: expected 3, got ${planes?.size ?: 0}")
            return null
        }

        val yBuffer = planes[0]?.buffer ?: return null
        val uBuffer = planes[1]?.buffer ?: return null
        val vBuffer = planes[2]?.buffer ?: return null

        yBuffer.rewind()
        uBuffer.rewind()
        vBuffer.rewind()

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        if (ySize <= 0 || uSize <= 0 || vSize <= 0) {
            Log.e(TAG, "Invalid buffer sizes: Y=$ySize, U=$uSize, V=$vSize")
            return null
        }

        val nv21 = ByteArray(ySize + uSize + vSize)
        try {
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)
        } catch (e: Exception) {
            Log.e(TAG, "Error copying YUV buffers: ${e.message}", e)
            return null
        }

        val width = image.width
        val height = image.height

        if (width <= 0 || height <= 0) {
            Log.e(TAG, "Invalid frame dimensions: width=$width, height=$height")
            return null
        }

        return Triple(nv21, width, height)
    }
}
