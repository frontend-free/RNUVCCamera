package com.rnuvccamera.native.uvc

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class USBCameraViewManager : SimpleViewManager<USBCameraView>() {
    override fun getName(): String = REACT_CLASS

    override fun createViewInstance(reactContext: ThemedReactContext): USBCameraView {
        return USBCameraView(reactContext)
    }

    @ReactProp(name = "deviceId")
    fun setDeviceId(view: USBCameraView, deviceId: String?) {
        view.setDeviceId(deviceId)
    }

    @ReactProp(name = "resolution")
    fun setResolution(view: USBCameraView, resolution: ReadableMap?) {
        resolution?.let {
            val width = it.getInt("width")
            val height = it.getInt("height")
            view.setResolution(width, height)
        }
    }

    companion object {
        private const val REACT_CLASS = "RNUSBCameraView"
    }
} 