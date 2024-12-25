package com.rnuvccamera.native.uvc

import android.util.Log
import android.view.Choreographer
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp


class USBCameraViewManager : ViewGroupManager<FrameLayout>() {
    override fun getName(): String = "RNUSBCameraView"


    override fun createViewInstance(context: ThemedReactContext): FrameLayout {
        return FrameLayout(context).apply {
            // 生成唯一ID很重要
            id = View.generateViewId()
            Log.d("TestView", "Creating view with id: $id")

            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            // 确保视图已经被添加到窗口后再添加Fragment
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    Log.d("TestView", "View attached to window")
                    addFragment(v as FrameLayout)
                }

                override fun onViewDetachedFromWindow(v: View) {
                    Log.d("TestView", "View detached from window")
                    removeFragment(v as FrameLayout)
                }
            })
        }
    }

    private fun addFragment(container: FrameLayout) {
        try {
            val activity = (container.context as? ThemedReactContext)?.currentActivity as? FragmentActivity
            if (activity == null) {
                Log.e("TestView", "Activity is null")
                return
            }

            // 检查Fragment是否已经添加
            val existingFragment = activity.supportFragmentManager.findFragmentById(container.id)
            if (existingFragment != null) {
                Log.d("TestView", "Fragment already exists")
                return
            }

            val fragment = USBCameraView()
            activity.runOnUiThread {
                try {
                    activity.supportFragmentManager
                        .beginTransaction()
                        .add(container.id, fragment) // 使用add而不是replace
                        .commitNowAllowingStateLoss()
                    Log.d("TestView", "Fragment added successfully")
                } catch (e: Exception) {
                    Log.e("TestView", "Error adding fragment: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("TestView", "Error in addFragment: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun removeFragment(container: FrameLayout) {
        try {
            val activity = (container.context as? ThemedReactContext)?.currentActivity as? FragmentActivity
            val fragment = getFragment(container)

            if (activity != null && fragment != null) {
                activity.runOnUiThread {
                    activity.supportFragmentManager
                        .beginTransaction()
                        .remove(fragment)
                        .commitNowAllowingStateLoss()
                    Log.d("TestView", "Fragment removed successfully")
                }
            }
        } catch (e: Exception) {
            Log.e("TestView", "Error removing fragment: ${e.message}")
        }
    }

    private fun getFragment(view: FrameLayout): USBCameraView? {
        val activity = (view.context as? ThemedReactContext)?.currentActivity as? FragmentActivity
        val fragment = activity?.supportFragmentManager?.findFragmentById(view.id)
        Log.d("TestView", "Getting fragment: ${fragment != null}")
        return fragment as? USBCameraView
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
}

    /**
     * Replace your React Native view with a custom fragment
     */
//    fun createFragment(root: FrameLayout, reactNativeViewId: Int) {
//        val parentView = root.findViewById<View>(reactNativeViewId).parent as ViewGroup
////        setupLayout(parentView)
//        val myFragment: MyFragment = MyFragment()
//        reactContext.getCurrentActivity().supportFragmentManager
//            .beginTransaction()
//            .replace(reactNativeViewId, myFragment, reactNativeViewId.toString())
//            .commit()
//    }

//    fun setupLayout(view: View) {
//        Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback() {
//            override fun doFrame(frameTimeNanos: Long) {
//                manuallyLayoutChildren(view)
//                view.viewTreeObserver.dispatchOnGlobalLayout()
//                Choreographer.getInstance().postFrameCallback(this)
//            }
//        })
//    }
