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
import com.rnuvccamera.databinding.ItemUvcCameraLayoutBinding
import com.rnuvccamera.native.base.BaseViewHolder


class USBCameraView(context: Context) : UvcCameraFragment() {
//    lateinit var mBinding: ActivityUvcCameraBinding
    private var multiCameraClient: MultiCameraClient? = null
    private var textureView: AspectRatioTextureView? = null
    private var currentCamera: CameraUVC? = null
    private var isPreview = false
    private var currentDeviceId: Int? = 0
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

    fun setDeviceId(deviceId: Int) {
        if (currentDeviceId == deviceId) return
        currentDeviceId = deviceId
        
        // 遍历当前连接的摄像头
        uvcCameraAdapter?.dataList?.forEach { camera ->
            val usbDevice = camera.getUsbDevice()
            
            if (deviceId == usbDevice.deviceId) {
                // 找到匹配的设备，请求权限并打开
                if (!hasPermission(usbDevice)) {
                    requestPermission(usbDevice)
                } else {
                    // 关闭其他摄像头
                    uvcCameraAdapter?.dataList?.forEach { otherCamera ->
                        if (otherCamera.getUsbDevice().deviceId != deviceId) {
                            otherCamera.closeCamera()
                        }
                    }
                    // 打开指定摄像头
                    openCamera(camera)
                }
            }
        }
    }

    fun setResolution(width: Int, height: Int) {
        if (currentWidth == width && currentHeight == height) return
        currentWidth = width
        currentHeight = height
        
        // 更新当前打开的摄像头分辨率
        uvcCameraAdapter?.dataList?.find { camera ->
            camera.getUsbDevice().deviceId == currentDeviceId
        }?.let { camera ->
            camera.closeCamera()
            openCamera(camera)
        }
    }

    private fun openCamera(camera: MultiCameraClient.ICamera) {
        val request = CameraRequest.Builder()
            .setPreviewWidth(currentWidth)
            .setPreviewHeight(currentHeight)
            .create()
            
        // 找到对应的 SurfaceView
        uvcCameraAdapter?.dataList?.indexOf(camera)?.let { index ->
            val itemBinding = mBinding.rvCamera.findViewHolderForAdapterPosition(index) as? BaseViewHolder<ItemUvcCameraLayoutBinding>
            itemBinding?.binding?.surface?.let { surfaceView ->
                camera.openCamera(surfaceView, request)
                camera.setCameraStateCallBack(this)
            }
        }
    }

    override fun onCameraConnected(camera: MultiCameraClient.ICamera) {
        super.onCameraConnected(camera)
        
        // 如果是当前选中的设备，自动打开
        if (camera.getUsbDevice().deviceId == currentDeviceId) {
            openCamera(camera)
        }
    }

    override fun onCameraDetached(camera: MultiCameraClient.ICamera) {
        super.onCameraDetached(camera)
        
        // 如果断开的是当前设备，清除设备ID
        if (camera.getUsbDevice().deviceId == currentDeviceId) {
            currentDeviceId = null
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