package com.rnuvccamera.native.uvc

import android.content.Context
import android.hardware.usb.UsbDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.MultiCameraFragment
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.RenderManager
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.callback.IDeviceConnectCallBack
import com.jiangdg.ausbc.callback.ICameraStateCallBack.State
import com.rnuvccamera.databinding.ActivityUvcCameraBinding


class USBCameraView(context: Context) : UvcCameraFragment() {
//    lateinit var mBinding: ActivityUvcCameraBinding
    private var multiCameraClient: MultiCameraClient? = null
    private var textureView: AspectRatioTextureView? = null
    private var currentCamera: CameraUVC? = null
    private var isPreview = false
    private var currentDeviceId: String? = null
    private var currentWidth: Int = 640
    private var currentHeight: Int = 480

    init {
//        initView()
    }

    override fun generateCamera(ctx: Context, device: UsbDevice): MultiCameraClient.ICamera {
        return CameraUVC(ctx, device)
    }


//    private fun initView() {
//        textureView = AspectRatioTextureView(context).apply {
//            setAspectRatio(currentWidth, currentHeight)
//        }
//        addView(textureView)
//
//        multiCameraClient = MultiCameraClient(context).apply {
//            setStateCallback(this@USBCameraView)
//            setDeviceCallback(this@USBCameraView)
//        }
//    }
//


    // IDeviceConnectCallBack 实现
//    override fun onAttachDev(device: UsbDevice?) {
//        device?.let {
//            if (it.deviceName == currentDeviceId || currentDeviceId == null) {
//                multiCameraClient?.requestPermission(it)
//            }
//        }
//    }

//    override fun onDetachDec(device: UsbDevice?) {
//        if (isPreview) {
//            stopPreview()
//        }
//    }
//
//    override fun onConnectDev(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
//        openCamera()
//    }
//
//    override fun onDisConnectDec(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
//        stopPreview()
//        currentCamera = null
//    }
//
//    override fun onCancelDev(device: UsbDevice?) {
//        // 处理设备取消操作
//    }

    private fun openCamera() {
        val cameraRequest = CameraRequest.Builder()
            .setPreviewWidth(currentWidth)
            .setPreviewHeight(currentHeight)
            .create()

//        multiCameraClient?.openCamera(cameraRequest)
    }

    fun setDeviceId(deviceId: String?) {
        currentDeviceId = deviceId
    }

    fun setResolution(width: Int, height: Int) {
        currentWidth = width
        currentHeight = height
        textureView?.setAspectRatio(width, height)
        if (isPreview) {
//            stopPreview()
            openCamera()
        }
    }



} 