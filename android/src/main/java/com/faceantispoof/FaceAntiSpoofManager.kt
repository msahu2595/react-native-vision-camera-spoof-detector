package com.faceantispoof

import android.content.res.AssetManager
import android.util.Log

/**
 * Singleton manager for the FaceAntiSpoofingAdvanced instance.
 * Shared between the Nitro HybridObject plugin and the React Native bridge module.
 */
object FaceAntiSpoofManager {

    private const val TAG = "FaceAntiSpoofManager"

    @Volatile
    private var instance: FaceAntiSpoofingAdvanced? = null

    @Volatile
    private var isInitializing = false

    fun initialize(assets: AssetManager): Boolean {
        if (instance != null) return true
        if (isInitializing) return false

        synchronized(this) {
            if (instance != null) return true
            isInitializing = true
            return try {
                instance = FaceAntiSpoofingAdvanced(assets)
                Log.i(TAG, "FaceAntiSpoofingAdvanced initialized")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize FaceAntiSpoofingAdvanced: ${e.message}", e)
                false
            } finally {
                isInitializing = false
            }
        }
    }

    fun getInstance(): FaceAntiSpoofingAdvanced? = instance

    fun isReady(): Boolean = instance?.isInitialized() == true

    fun cleanup() {
        synchronized(this) {
            try {
                instance?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing instance: ${e.message}", e)
            } finally {
                instance = null
            }
        }
    }
}
