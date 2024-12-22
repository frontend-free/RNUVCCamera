package com.rnuvccamera.native.uvc

import android.content.Context
import android.hardware.usb.UsbDevice
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.rnuvccamera.databinding.ItemUvcCameraLayoutBinding
import com.rnuvccamera.native.base.BaseViewHolder

class USBCameraView(context: Context) : UvcCameraFragment() {
    private var currentDeviceId: Int? = 0
    private var currentWidth: Int = 1080
    private var currentHeight: Int = 634

    override fun generateCamera(ctx: Context, device: UsbDevice): MultiCameraClient.ICamera {
        return super.generateCamera(ctx, device)
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
}