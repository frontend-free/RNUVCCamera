package com.rnuvccamera.native.uvc

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class USBCameraViewManager : SimpleViewManager<FrameLayout>() {
    override fun getName(): String = REACT_CLASS

    override fun createViewInstance(reactContext: ThemedReactContext): FrameLayout {
        val container = FrameLayout(reactContext)
        val fragment = USBCameraView(reactContext)
        
        // 获取 FragmentActivity
        val activity = reactContext.currentActivity as? androidx.fragment.app.FragmentActivity
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            add(container.id, fragment)
            commit()
        }
        
        return container
    }

    @ReactProp(name = "deviceId")
    fun setDeviceId(view: FrameLayout, deviceId: String?) {
        val fragment = getFragment(view)
        fragment?.setDeviceId(deviceId)
    }

    @ReactProp(name = "resolution")
    fun setResolution(view: FrameLayout, resolution: ReadableMap?) {
        val fragment = getFragment(view)
        resolution?.let {
            val width = it.getInt("width")
            val height = it.getInt("height")
            fragment?.setResolution(width, height)
        }
    }

    private fun getFragment(view: FrameLayout): USBCameraView? {
        val activity = (view.context as? ThemedReactContext)?.currentActivity as? androidx.fragment.app.FragmentActivity
        return activity?.supportFragmentManager?.findFragmentById(view.id) as? USBCameraView
    }

    companion object {
        private const val REACT_CLASS = "RNUSBCameraView"
    }
} 