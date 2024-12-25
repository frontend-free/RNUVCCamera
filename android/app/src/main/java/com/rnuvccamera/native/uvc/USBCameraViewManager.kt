package com.rnuvccamera.native.uvc

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.Dynamic
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class USBCameraViewManager : SimpleViewManager<FrameLayout>() {
    override fun getName(): String = "RNUSBCameraView"

    override fun createViewInstance(reactContext: ThemedReactContext): FrameLayout {
        return FrameLayout(reactContext).apply {
            id = View.generateViewId()
            
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    val fragment = USBCameraView()
                    val activity = reactContext.currentActivity as? FragmentActivity
                    
                    activity?.supportFragmentManager?.beginTransaction()?.apply {
                        replace(id, fragment)
                        commitAllowingStateLoss()
                    }
                    
                    removeOnAttachStateChangeListener(this)
                }

                override fun onViewDetachedFromWindow(v: View) {
                    val activity = reactContext.currentActivity as? FragmentActivity
                    val fragment = activity?.supportFragmentManager?.findFragmentById(id)
                    fragment?.let {
                        activity.supportFragmentManager.beginTransaction()
                            .remove(it)
                            .commitAllowingStateLoss()
                    }
                }
            })
        }
    }

    @ReactProp(name = "deviceId")
    fun setDeviceId(view: FrameLayout, deviceId: Int) {
//        val fragment = getFragment(view)
//        fragment?.setDeviceId(deviceId)
        try {
            val fragment = getFragment(view)
            fragment?.setDeviceId(deviceId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
