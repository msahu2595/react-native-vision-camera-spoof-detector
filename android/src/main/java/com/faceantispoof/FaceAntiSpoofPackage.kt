package com.faceantispoof

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.margelo.nitro.com.faceantispoof.FaceAntiSpoofDetectorOnLoad

class FaceAntiSpoofPackage : ReactPackage {

    companion object {
        init {
            FaceAntiSpoofDetectorOnLoad.initializeNative()
        }
    }

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf(FaceAntiSpoofModule(reactContext))
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}
