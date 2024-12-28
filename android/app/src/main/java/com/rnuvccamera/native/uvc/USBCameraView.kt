package com.rnuvccamera.native.uvc

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.IAspectRatio
import com.rnuvccamera.R
import com.rnuvccamera.databinding.FragmentDemoBinding

class USBCameraView : CameraFragment() {
    lateinit var mViewBinding: FragmentDemoBinding
    private var currentDeviceId: Int? = null

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        mViewBinding = FragmentDemoBinding.inflate(inflater, container, false)
        return mViewBinding.root
    }

    override fun getGravity(): Int = Gravity.CENTER

    override fun onCameraState(
        self: MultiCameraClient.ICamera,
        code: ICameraStateCallBack.State,
        msg: String?
    ) {
        when (code) {
            ICameraStateCallBack.State.OPENED -> handleCameraOpened()
            ICameraStateCallBack.State.CLOSED -> handleCameraClosed()
            ICameraStateCallBack.State.ERROR -> handleCameraError(msg)
        }
    }

    private fun handleCameraError(msg: String?) {
        Toast.makeText(requireContext(), "handleCameraError: $msg", Toast.LENGTH_SHORT).show()
    }

    private fun handleCameraClosed() {
        //不要加，requireContext会导致崩溃
        //Toast.makeText(requireContext(), "handleCameraClosed", Toast.LENGTH_SHORT).show()
    }

    private fun handleCameraOpened() {
        Toast.makeText(requireContext(), "handleCameraOpened", Toast.LENGTH_SHORT).show()
    }

    override fun getCameraView(): IAspectRatio? {
        return AspectRatioTextureView(requireContext())
    }


    override fun getCameraViewContainer(): ViewGroup? {
        return mViewBinding.cameraViewContainer
    }

    /**
     * 获取所有 USB 设备列表
     */
    @SuppressLint("ServiceCast")
    private fun getUsbDeviceList(): List<UsbDevice>? {
        val usbManager = requireContext().getSystemService(Context.USB_SERVICE) as? UsbManager
        return usbManager?.deviceList?.values?.toList()
    }

    fun setDeviceId(deviceId: Int?) {
        if (currentDeviceId == deviceId) return
        currentDeviceId = deviceId
        var isSet = false
        getUsbDeviceList()?.forEach { device ->
            if (device.deviceId == deviceId) {
                Toast.makeText(requireContext(), "设备id"+deviceId, Toast.LENGTH_SHORT).show()
                isSet = true
                setDevice(device)
            }
        }
        if(!isSet) {
            setDevice(null);
        }
    }

    override fun getDefaultCamera(): UsbDevice? {
        return this.getUsbDeviceList()?.find { it.deviceId == currentDeviceId }
    }


    fun setResolution(width: Int, height: Int) {
//        if (currentWidth == width && currentHeight == height) return
//        currentWidth = width
//        currentHeight = height
//
//        // 如果摄像头已经打开，需要重新打开以应用新的分辨率
//        if (isCameraOpened()) {
//            closeCamera()
//            openCamera()
//        }
    }
}
