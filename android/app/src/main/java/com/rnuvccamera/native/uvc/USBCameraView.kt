package com.rnuvccamera.native.uvc

import android.content.Context
import android.hardware.usb.UsbDevice
import android.widget.FrameLayout
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.RenderManager
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.callback.IDeviceConnectCallBack
import com.jiangdg.ausbc.callback.ICameraStateCallBack.State

class USBCameraView(context: Context) : FrameLayout(context), ICameraStateCallBack, IDeviceConnectCallBack {
    private var multiCameraClient: MultiCameraClient? = null
    private var textureView: AspectRatioTextureView? = null
    private var currentCamera: CameraUVC? = null
    private var isPreview = false
    private var currentDeviceId: String? = null
    private var currentWidth: Int = 640
    private var currentHeight: Int = 480

    init {
        initView()
    }

    private fun initView() {
        // 初始化预览视图
        textureView = AspectRatioTextureView(context).apply {
            surfaceWidth = currentWidth
            surfaceHeight = currentHeight
        }
        addView(textureView)

        // 初始化相机客户端
        multiCameraClient = MultiCameraClient(context).apply {
            setStateCallback(this@USBCameraView)
            setDeviceCallback(this@USBCameraView)
        }
    }

    // ICameraStateCallBack 实现
    override fun onCameraState(self: MultiCameraClient.ICamera, code: State, msg: String?) {
        when (code) {
            State.OPENED -> {
                currentCamera?.let { camera ->
                    camera.setRenderManager(RenderManager())
                    camera.updateRenderView(textureView, currentWidth, currentHeight)
                    startPreview()
                }
            }
            State.CLOSED -> {
                currentCamera = null
                isPreview = false
            }
            State.PREVIEW_STARTED -> {
                isPreview = true
            }
            State.PREVIEW_STOPPED -> {
                isPreview = false
            }
        }
    }

    // IDeviceConnectCallBack 实现
    override fun onConnected(device: UsbDevice?) {
        device?.let {
            if (it.deviceId.toString() == currentDeviceId || currentDeviceId == null) {
                multiCameraClient?.requestPermission(it)
            }
        }
    }

    override fun onDisconnected(device: UsbDevice?) {
        if (isPreview) {
            stopPreview()
        }
    }

    override fun onPermissionGranted(device: UsbDevice?) {
        openCamera()
    }

    override fun onPermissionDenied(device: UsbDevice?) {
        // 处理权限被拒绝的情况
    }

    private fun openCamera() {
        val cameraRequest = CameraRequest.Builder()
            .setPreviewWidth(currentWidth)
            .setPreviewHeight(currentHeight)
            .create()

        multiCameraClient?.openCamera(cameraRequest)
    }

    fun setDeviceId(deviceId: String?) {
        currentDeviceId = deviceId
    }

    fun setResolution(width: Int, height: Int) {
        currentWidth = width
        currentHeight = height
        textureView?.apply {
            surfaceWidth = width
            surfaceHeight = height
        }
        if (isPreview) {
            stopPreview()
            openCamera()
        }
    }

    fun startPreview() {
        currentCamera?.startPreview()
    }

    fun stopPreview() {
        currentCamera?.stopPreview()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopPreview()
        currentCamera?.releaseCamera()
        multiCameraClient?.release()
    }
} 