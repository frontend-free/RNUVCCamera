package com.rnuvccamera.native.uvc

import android.content.Context
import android.hardware.usb.UsbDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.MultiCameraFragment
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.rnuvccamera.BuildConfig
import com.rnuvccamera.R
import com.rnuvccamera.databinding.ActivityUvcCameraBinding
import com.rnuvccamera.databinding.ItemUvcCameraLayoutBinding
import com.rnuvccamera.native.base.BaseViewHolder
import com.rnuvccamera.native.utils.MLog


/**
 * Copyright (c)
 *
 * @description 描述该文件做什么
 * @author chen_wei
 * @date 2024/1/31
 */
open class UvcCameraFragment : MultiCameraFragment(), ICameraStateCallBack {
    lateinit var mBinding: ActivityUvcCameraBinding
    private var uvcCameraAdapter: UvcCameraAdapter? = null
    private var isPause = false
    override fun generateCamera(ctx: Context, device: UsbDevice): MultiCameraClient.ICamera {
        return CameraUVC(ctx, device)
    }

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        mBinding = DataBindingUtil.inflate<ActivityUvcCameraBinding>(
            inflater,
            R.layout.activity_uvc_camera,
            container,
            false
        )
        return mBinding.root
    }

    override fun initView() {
        super.initView()
        uvcCameraAdapter = UvcCameraAdapter()
        mBinding.rvCamera.adapter = uvcCameraAdapter
        mBinding.rvCamera.layoutManager = LinearLayoutManager(context)
    }

    override fun onCameraAttached(camera: MultiCameraClient.ICamera) {
        log("摄像头获取 camera id : ${camera.getUsbDevice().deviceId}  name : ${camera.getUsbDevice().deviceName}")
        uvcCameraAdapter?.let {
            for (cam in it.dataList) {
                if (cam.getUsbDevice().deviceId == camera.getUsbDevice().deviceId) {
                    return
                }
            }
            it.dataList.add(camera)
            it.notifyItemInserted(it.dataList.size - 1)
        }
    }

    override fun onCameraConnected(camera: MultiCameraClient.ICamera) {
        var index = -1
        val dataList = uvcCameraAdapter?.dataList ?: return
        for ((position, cam) in dataList.withIndex()) {
            if (camera.getUsbDevice().deviceId == cam.getUsbDevice().deviceId) {
                index = position
                break
            }
        }
        log("摄像头连接 index: $index")
        if (index < 0) {
            return
        }

        openCameraBySurface(index)
        dataList.forEach { cam ->
            val usbDevice = cam.getUsbDevice();
            if (!hasPermission(usbDevice)) {
                requestPermission(usbDevice)
                return@forEach
            }
        }
    }

    private fun openCameraBySurface(index: Int) {

        log("打开摄像头 index: $index")
        val camera = uvcCameraAdapter?.dataList?.getOrNull(index)
        camera ?: return
        val itemBinding =
            mBinding.rvCamera.findViewHolderForAdapterPosition(index) as? BaseViewHolder<ItemUvcCameraLayoutBinding>
        itemBinding ?: return
        val surfaceView = itemBinding.binding.surface
        camera.openCamera(surfaceView, getCameraRequest())
        camera.setCameraStateCallBack(this)

        updateVideoLivingUI()
    }

    private fun getCameraRequest(): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewWidth(1080)
            .setPreviewHeight(634)
            .create()
    }

    override fun onCameraDetached(camera: MultiCameraClient.ICamera) {
        val dataList = uvcCameraAdapter?.dataList ?: return

        for ((position, cam) in dataList.withIndex()) {
            if (camera.getUsbDevice().deviceId == cam.getUsbDevice().deviceId) {
                camera.closeCamera()
                dataList.removeAt(position)
                uvcCameraAdapter?.notifyItemRemoved(position)
                break
            }
        }
        updateVideoLivingUI()
    }

    override fun onCameraDisConnected(camera: MultiCameraClient.ICamera) {
        camera.closeCamera()
    }

    override fun onCameraState(
        self: MultiCameraClient.ICamera,
        code: ICameraStateCallBack.State,
        msg: String?
    ) {
        log("摄像头状态: code: $code , msg:$msg")
        for ((position, cam) in uvcCameraAdapter!!.dataList.withIndex()) {
            if (cam.getUsbDevice().deviceId == self.getUsbDevice().deviceId) {
                uvcCameraAdapter?.notifyItemChanged(position, "switch")
                break
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MLog.d("摄像头 fragment onResume")
        if (isPause) {
            isPause = false

        }
    }

    override fun onPause() {
        super.onPause()
        MLog.d("摄像头 fragment onPause")
        isPause = true
    }

    private fun updateVideoLivingUI() {
        val dataList = uvcCameraAdapter?.dataList ?: return
//        mBinding.ivLiving.visibility = if (dataList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun log(text: String?) {
        text?.apply {
            Log.d("uvcCamera", text)
        }
    }
}