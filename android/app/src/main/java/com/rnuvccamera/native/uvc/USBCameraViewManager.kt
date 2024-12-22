package com.rnuvccamera.native.uvc

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.facebook.react.bridge.Dynamic
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class USBCameraViewManager : SimpleViewManager<FrameLayout>() {
    override fun getName(): String = "RNUSBCameraView"

    override fun createViewInstance(reactContext: ThemedReactContext): FrameLayout {
        return FrameLayout(reactContext).apply {
            // 生成一个唯一的 ID
            id = View.generateViewId()

            // 设置布局参数
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // 在视图添加到窗口时添加Fragment
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    val fragment = USBCameraView(reactContext)
                    val activity = reactContext.currentActivity as? androidx.fragment.app.FragmentActivity

                    activity?.supportFragmentManager?.beginTransaction()?.apply {
                        replace(id, fragment)
                        commitAllowingStateLoss()
                    }

                    removeOnAttachStateChangeListener(this)
                }

                override fun onViewDetachedFromWindow(v: View) {
                    // 清理Fragment
                    val activity = reactContext.currentActivity as? androidx.fragment.app.FragmentActivity
                    val fragment = activity?.supportFragmentManager?.findFragmentById(id)
                    if (fragment != null) {
                        activity.supportFragmentManager.beginTransaction()
                            .remove(fragment)
                            .commitAllowingStateLoss()
                    }
                }
            })
        }
    }

    @ReactProp(name = "deviceId")
    fun setDeviceId(view: FrameLayout, deviceId: Dynamic) {
//        val fragment = getFragment(view)
//        fragment?.setDeviceId(deviceId)
        try {
            val fragment = getFragment(view)
            when {
                deviceId == null -> fragment?.setDeviceId(null)
                deviceId.isNull -> fragment?.setDeviceId(null)
                deviceId.type.name == "String" -> fragment?.setDeviceId(deviceId.asString())
                deviceId.type.name == "Number" -> fragment?.setDeviceId(deviceId.asInt().toString())
                else -> fragment?.setDeviceId(deviceId.toString())
            }
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
