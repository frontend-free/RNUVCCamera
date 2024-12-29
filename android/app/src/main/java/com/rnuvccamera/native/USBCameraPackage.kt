package com.rnuvccamera.native

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.rnuvccamera.native.uvc.UVCCameraViewManager
import com.rnuvccamera.native.uvc.UVCDeviceModule

class USBCameraPackage : ReactPackage {
    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return listOf(UVCCameraViewManager())
    }

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf(UVCDeviceModule(reactContext))
    }
} 