package com.faceantispoof

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = FaceAntiSpoofModule.NAME)
class FaceAntiSpoofModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val NAME = "FaceAntiSpoof"
    }

    init {
        // Initialize shared manager
        FaceAntiSpoofManager.initialize(reactContext.assets)
    }

    override fun getName(): String = NAME

    @ReactMethod
    fun initialize(promise: Promise) {
        try {
            val success = FaceAntiSpoofManager.initialize(reactApplicationContext.assets)
            promise.resolve(success)
        } catch (e: Exception) {
            android.util.Log.e("FaceAntiSpoof", "initialize() error", e)
            promise.reject("INIT_ERROR", "Failed to initialize face anti-spoof", e)
        }
    }

    @ReactMethod
    fun checkModelStatus(promise: Promise) {
        try {
            val isInitialized = FaceAntiSpoofManager.isReady()
            val result = mapOf(
                "pluginAvailable" to isInitialized,
                "modelLoaded" to isInitialized,
                "moduleInitialized" to isInitialized
            )
            promise.resolve(com.facebook.react.bridge.Arguments.makeNativeMap(result))
        } catch (e: Exception) {
            android.util.Log.e("FaceAntiSpoof", "checkModelStatus() error", e)
            promise.reject("STATUS_ERROR", "Failed to check model status", e)
        }
    }

    @ReactMethod
    fun isAvailable(promise: Promise) {
        try {
            promise.resolve(FaceAntiSpoofManager.isReady())
        } catch (e: Exception) {
            android.util.Log.e("FaceAntiSpoof", "isAvailable() error", e)
            promise.reject("AVAIL_ERROR", "Failed to check availability", e)
        }
    }

    @ReactMethod
    fun testMethod(promise: Promise) {
        try {
            promise.resolve("Native module is working!")
        } catch (e: Exception) {
            android.util.Log.e("FaceAntiSpoof", "testMethod() error", e)
            promise.reject("TEST_ERROR", "Failed executing testMethod", e)
        }
    }

    @ReactMethod
    fun getModuleInfo(promise: Promise) {
        try {
            val info = mapOf(
                "name" to NAME,
                "methods" to listOf("initialize", "checkModelStatus", "isAvailable", "testMethod", "getModuleInfo")
            )
            promise.resolve(com.facebook.react.bridge.Arguments.makeNativeMap(info))
        } catch (e: Exception) {
            android.util.Log.e("FaceAntiSpoof", "getModuleInfo() error", e)
            promise.reject("INFO_ERROR", "Failed to get module info", e)
        }
    }

    @ReactMethod
    fun cleanup(promise: Promise) {
        try {
            FaceAntiSpoofManager.cleanup()
            android.util.Log.i("FaceAntiSpoof", "Cleanup completed")
            promise.resolve(true)
        } catch (e: Exception) {
            android.util.Log.e("FaceAntiSpoof", "cleanup() error", e)
            promise.reject("CLEANUP_ERROR", "Failed to cleanup face anti-spoof", e)
        }
    }

    override fun onCatalystInstanceDestroy() {
        try {
            FaceAntiSpoofManager.cleanup()
            android.util.Log.i("FaceAntiSpoof", "onCatalystInstanceDestroy cleanup completed")
        } catch (e: Exception) {
            android.util.Log.e("FaceAntiSpoof", "Error in onCatalystInstanceDestroy", e)
        }
        super.onCatalystInstanceDestroy()
    }

}