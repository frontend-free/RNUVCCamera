package com.rnuvccamera.native.uvc

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.widget.Toast
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.callback.IDeviceConnectCallBack
import com.jiangdg.usb.USBMonitor

class UVCDeviceModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private var mCameraClient: MultiCameraClient? = null
    private var pendingPermissionPromise: Promise? = null
    private var pendingPermissionDeviceId: Int? = null

    companion object {
        private val deviceCtrlBlockMap = mutableMapOf<Int, USBMonitor.UsbControlBlock>()

        fun getCtrlBlock(deviceId: Int): USBMonitor.UsbControlBlock? {
            return deviceCtrlBlockMap[deviceId]
        }
    }

    init {
        initMultiCamera()
    }

    override fun getName() = "UVCDeviceModule"

    private fun initMultiCamera() {
        mCameraClient = MultiCameraClient(reactApplicationContext, object : IDeviceConnectCallBack {
            override fun onAttachDev(device: UsbDevice?) {
                device?.let {
                    val params = Arguments.createMap().apply {
                        putInt("deviceId", it.deviceId)
                        putString("deviceName", it.deviceName)
                        putInt("productId", it.productId)
                        putInt("vendorId", it.vendorId)
                    }
                    sendEvent("onDeviceAttached", params)
                }
            }

            override fun onDetachDec(device: UsbDevice?) {
                device?.let {
                    val params = Arguments.createMap().apply {
                        putInt("deviceId", it.deviceId)
                    }
                    sendEvent("onDeviceDetached", params)
                }
            }

            override fun onConnectDev(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
                device?.let {
                    ctrlBlock?.let { block ->
                        deviceCtrlBlockMap[device.deviceId] = block
                    }

                    if (device.deviceId == pendingPermissionDeviceId) {
                        pendingPermissionPromise?.resolve(true)
                        pendingPermissionPromise = null
                        pendingPermissionDeviceId = null
                    }

                    val params = Arguments.createMap().apply {
                        putInt("deviceId", it.deviceId)
                    }
                    sendEvent("onDeviceConnected", params)
                }
            }

            override fun onDisConnectDec(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
                device?.let {
                    deviceCtrlBlockMap.remove(device.deviceId)

                    val params = Arguments.createMap().apply {
                        putInt("deviceId", it.deviceId)
                    }
                    sendEvent("onDeviceDisconnected", params)
                }
            }

            override fun onCancelDev(device: UsbDevice?) {
                device?.let {
                    if (device.deviceId == pendingPermissionDeviceId) {
                        pendingPermissionPromise?.resolve(false)
                        pendingPermissionPromise = null
                        pendingPermissionDeviceId = null
                    }

                    val params = Arguments.createMap().apply {
                        putInt("deviceId", it.deviceId)
                    }
                    sendEvent("onDevicePermissionDenied", params)
                }
            }
        })
        mCameraClient?.register()
    }

    @ReactMethod
    fun getDeviceList(promise: Promise) {
        try {
            val usbManager = reactApplicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
            // 筛选出UVC设备
            val devices = usbManager.deviceList.values.filter {
                // UVC设备的特征：
                // 1. deviceClass 可能是 USB_CLASS_VIDEO (0x0E) 或 USB_CLASS_MISC (0xEF)
                // 2. 如果是 USB_CLASS_MISC，需要检查接口类型
                when (it.deviceClass) {
                    UsbConstants.USB_CLASS_VIDEO -> true
                    UsbConstants.USB_CLASS_MISC -> {
                        // 检查接口是否包含视频类
                        var hasVideoInterface = false
                        for (i in 0 until it.interfaceCount) {
                            val intf = it.getInterface(i)
                            if (intf.interfaceClass == UsbConstants.USB_CLASS_VIDEO) {
                                hasVideoInterface = true
                                break
                            }
                        }
                        hasVideoInterface
                    }
                    else -> false
                }
            }

            val deviceArray = Arguments.createArray()
            devices.forEach { device ->
                val deviceInfo = Arguments.createMap().apply {
                    putInt("deviceId", device.deviceId)
                    putString("deviceName", device.deviceName)
                    putInt("productId", device.productId)
                    putInt("vendorId", device.vendorId)
                }
                deviceArray.pushMap(deviceInfo)
            }
            promise.resolve(deviceArray)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }

    @ReactMethod
    fun requestPermission(deviceId: Int, promise: Promise) {
        try {
            val usbManager = reactApplicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
            val device = usbManager.deviceList.values.find { it.deviceId == deviceId }

            if (device == null) {
                promise.reject("ERROR", "Device not found")
                return
            }

            pendingPermissionPromise = promise
            pendingPermissionDeviceId = deviceId

            mCameraClient?.requestPermission(device)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }

    @ReactMethod
    fun hasPermission(deviceId: Int, promise: Promise) {
        try {
            val usbManager = reactApplicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
            val device = usbManager.deviceList.values.find { it.deviceId == deviceId }

            if (device == null) {
                promise.reject("ERROR", "Device not found")
                return
            }

            promise.resolve(usbManager.hasPermission(device))
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }

    private fun sendEvent(eventName: String, params: WritableMap?) {
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    @ReactMethod
    fun addListener(eventName: String) {
        // Keep: Required for RN built in Event Emitter Calls
    }

    @ReactMethod
    fun removeListeners(count: Int) {
        // Keep: Required for RN built in Event Emitter Calls
    }

    override fun invalidate() {
        super.invalidate()
        mCameraClient?.unRegister()
        mCameraClient?.destroy()
        mCameraClient = null
        pendingPermissionPromise = null
        pendingPermissionDeviceId = null
    }
}
