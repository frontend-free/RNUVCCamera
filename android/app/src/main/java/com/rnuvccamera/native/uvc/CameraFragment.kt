package com.rnuvccamera.native.uvc

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.BaseFragment
import com.jiangdg.ausbc.camera.bean.PreviewSize
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.callback.*
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.render.effect.AbstractEffect
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.Logger
import com.jiangdg.ausbc.utils.SettableFuture
import com.jiangdg.ausbc.widget.IAspectRatio
import java.util.concurrent.TimeUnit

/**Extends from BaseFragment for one uvc camera
 *
 * @author Created by jiangdg on 2023/2/3
 */
abstract class CameraFragment : BaseFragment(), ICameraStateCallBack {
    private var mCameraView: IAspectRatio? = null
    private val mCameraMap = hashMapOf<Int, MultiCameraClient.ICamera>()
    private var mCurrentCamera: SettableFuture<MultiCameraClient.ICamera>? = null
    protected var surfaceInited = false
    private var mCurrentDevice: UsbDevice? = null

    override fun initView() {
        when (val cameraView = getCameraView()) {
            is TextureView -> {
                handleTextureView(cameraView)
                cameraView
            }
            else -> {
                null
            }
        }.apply {
            mCameraView = this
            // offscreen render
            if (this == null) {
                registerMultiCamera()
                return
            }
        }?.also { view->
            getCameraViewContainer()?.apply {
                removeAllViews()
                addView(view, getViewLayoutParams(this))
            }
        }
    }

    override fun clear() {
        unRegisterMultiCamera()
    }

    protected fun setDevice(device: UsbDevice?) {
        mCurrentDevice = device;
        if(surfaceInited) {
            if (device != null) {
                openDevice(device)
            }
            return
        }
    }

    protected fun openDevice(device:UsbDevice) {
        context?.let {
            if (mCameraMap.containsKey(device.deviceId)) {
                return
            }
            generateCamera(it, device).apply {
                mCameraMap[device.deviceId] = this
            }
            mCameraMap[device.deviceId]?.apply {
                UVCDeviceModule.getCtrlBlock(device.deviceId)?.let { block ->
                    setUsbControlBlock(block)
                }
            }?.also { camera ->
                try {
                    mCurrentCamera?.cancel(true)
                    mCurrentCamera = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mCurrentCamera = SettableFuture()
                mCurrentCamera?.set(camera)
                openCamera(mCameraView)
                Logger.i(TAG, "camera connection. pid: ${device.productId}, vid: ${device.vendorId}")
            }
        }
    }

    protected fun registerMultiCamera() {
        Log.d("test1", "4444")
    }

    protected fun unRegisterMultiCamera() {
        mCameraMap.values.forEach {
            it.closeCamera()
        }
        mCameraMap.clear()
    }

    private fun handleTextureView(textureView: TextureView) {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                surfaceInited = true;
                val defaultCamera = mCurrentDevice
                if (defaultCamera == null) {
                    return
                }
                openDevice(defaultCamera)
//                registerMultiCamera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
                surfaceSizeChanged(p1, p2)
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                surfaceInited = false;
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

            }
        }
    }

    /**
     * Get current opened camera
     *
     * @return current camera, see [MultiCameraClient.ICamera]
     */
    protected fun getCurrentCamera(): MultiCameraClient.ICamera? {
        return try {
            mCurrentCamera?.get(2, TimeUnit.SECONDS)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate camera
     *
     * @param ctx context [Context]
     * @param device Usb device, see [UsbDevice]
     * @return Inheritor assignment camera api policy
     */
    protected open fun generateCamera(ctx: Context, device: UsbDevice): MultiCameraClient.ICamera {
        return CameraUVC(ctx, device)
    }

    /**
     * Get default camera
     *
     * @return Open camera by default, should be [UsbDevice]
     */
    protected open fun getDefaultCamera(): UsbDevice? = null

    /**
     * Capture image
     *
     * @param callBack capture status, see [ICaptureCallBack]
     * @param savePath custom image path
     */
    protected fun captureImage(callBack: ICaptureCallBack, savePath: String? = null) {
        getCurrentCamera()?.captureImage(callBack, savePath)
    }


    /**
     * Get default effect
     */
    protected fun getDefaultEffect() = getCurrentCamera()?.getDefaultEffect()

    /**
     * Is camera opened
     *
     * @return camera open status
     */
    protected fun isCameraOpened() = getCurrentCamera()?.isCameraOpened()  ?: false

    /**
     * Update resolution
     *
     * @param width camera preview width
     * @param height camera preview height
     */
    protected fun updateResolution(width: Int, height: Int) {
        getCurrentCamera()?.updateResolution(width, height)
    }

    /**
     * Get all preview sizes
     *
     * @param aspectRatio preview size aspect ratio,
     *                      null means getting all preview sizes
     */
    protected fun getAllPreviewSizes(aspectRatio: Double? = null) = getCurrentCamera()?.getAllPreviewSizes(aspectRatio)

    /**
     * Add render effect
     *
     * @param effect a effect will be added, only enable opengl render worked, see [AbstractEffect]
     */
    protected fun addRenderEffect(effect: AbstractEffect) {
        getCurrentCamera()?.addRenderEffect(effect)
    }

    /**
     * Remove render effect
     *
     * @param effect a effect will be removed, only enable opengl render worked, see [AbstractEffect]
     */
    protected fun removeRenderEffect(effect: AbstractEffect) {
        getCurrentCamera()?.removeRenderEffect(effect)
    }

    /**
     * Update render effect
     *
     * @param classifyId effect classify id
     * @param effect new effect, null means set none
     */
    protected fun updateRenderEffect(classifyId: Int, effect: AbstractEffect?) {
        getCurrentCamera()?.updateRenderEffect(classifyId, effect)
    }

    /**
     * Start capture H264 & AAC only
     */
    protected fun captureStreamStart() {
        getCurrentCamera()?.captureStreamStart()
    }

    /**
     * Stop capture H264 & AAC only
     */
    protected fun captureStreamStop() {
        getCurrentCamera()?.captureStreamStop()
    }

    /**
     * Add encode data call back
     *
     * @param callBack encode data call back, see [IEncodeDataCallBack]
     */
    protected fun setEncodeDataCallBack(callBack: IEncodeDataCallBack) {
        getCurrentCamera()?.setEncodeDataCallBack(callBack)
    }

    /**
     * Add preview data call back
     *
     * @param callBack preview data call back, see [IPreviewDataCallBack]
     */
    protected fun addPreviewDataCallBack(callBack: IPreviewDataCallBack) {
        getCurrentCamera()?.addPreviewDataCallBack(callBack)
    }

    /**
     * Remove preview data call back
     *
     * @param callBack preview data call back, see [IPreviewDataCallBack]
     */
    fun removePreviewDataCallBack(callBack: IPreviewDataCallBack) {
        getCurrentCamera()?.removePreviewDataCallBack(callBack)
    }

    /**
     * Capture video start
     *
     * @param callBack capture status, see [ICaptureCallBack]
     * @param path custom save path
     * @param durationInSec divided record duration time in seconds
     */
    protected fun captureVideoStart(callBack: ICaptureCallBack, path: String ?= null, durationInSec: Long = 0L) {
        getCurrentCamera()?.captureVideoStart(callBack, path, durationInSec)
    }

    /**
     * Capture video stop
     */
    protected fun captureVideoStop() {
        getCurrentCamera()?.captureVideoStop()
    }

    /**
     * Capture audio start
     *
     * @param callBack capture status, see [ICaptureCallBack]
     * @param path custom save path
     */
    protected fun captureAudioStart(callBack: ICaptureCallBack, path: String ?= null) {
        getCurrentCamera()?.captureAudioStart(callBack, path)
    }

    /**
     * Capture audio stop
     */
    protected fun captureAudioStop() {
        getCurrentCamera()?.captureAudioStop()
    }

    /**
     * Start play mic
     *
     * @param callBack play mic in real-time, see [IPlayCallBack]
     */
    protected fun startPlayMic(callBack: IPlayCallBack? = null) {
        getCurrentCamera()?.startPlayMic(callBack)
    }

    /**
     * Stop play mic
     */
    protected fun stopPlayMic() {
        getCurrentCamera()?.stopPlayMic()
    }

    /**
     * Get current preview size
     *
     * @return camera preview size, see [PreviewSize]
     */
    protected fun getCurrentPreviewSize(): PreviewSize? {
        return getCurrentCamera()?.getCameraRequest()?.let {
            PreviewSize(it.previewWidth, it.previewHeight)
        }
    }

    /**
     * Rotate camera angle
     *
     * @param type rotate angle, null means rotating nothing
     * see [RotateType.ANGLE_90], [RotateType.ANGLE_270],...etc.
     */
    protected fun setRotateType(type: RotateType) {
        getCurrentCamera()?.setRotateType(type)
    }

    /***********************************************************************************************/
    /*********************************Camera parameter control *************************************/
    /**
     * Send camera command of uvc camera
     *
     * @param command hex value
     * @return control result
     */
    protected fun sendCameraCommand(command: Int) {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.sendCameraCommand(command)
        }
    }

    /**
     * Set auto focus
     *
     * @param focus
     */
    protected fun setAutoFocus(focus: Boolean) {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.setAutoFocus(focus)
        }
    }

    /**
     * Get auto focus
     *
     * @return is camera auto focus opened
     */
    protected fun getAutoFocus(): Boolean? {
        return getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return@let false
            }
            camera.getAutoFocus()
        }
    }

    /**
     * Reset auto focus
     */
    protected fun resetAutoFocus() {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.resetAutoFocus()
        }
    }



    /**
     * Set brightness
     *
     * @param brightness camera brightness
     */
    protected fun setBrightness(brightness: Int) {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.setBrightness(brightness)
        }
    }

    /**
     * Get brightness
     *
     * @return current brightness value
     */
    protected fun getBrightness(): Int? {
        return getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return@let null
            }
            camera.getBrightness()
        }
    }

    /**
     * Reset brightness
     */
    protected fun resetBrightness() {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.resetBrightness()
        }
    }

    /**
     * Set contrast
     *
     * @param contrast camera contrast
     */
    protected fun setContrast(contrast: Int) {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.setContrast(contrast)
        }
    }

    /**
     * Get contrast
     *
     * @return current contrast value
     */
    protected fun getContrast(): Int? {
        return getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return@let null
            }
            camera.getContrast()
        }
    }

    /**
     * Reset contrast
     */
    protected fun resetContrast() {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.resetContrast()
        }
    }

    /**
     * Set gain
     *
     * @param gain camera gain
     */
    protected fun setGain(gain: Int) {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.setGain(gain)
        }
    }

    /**
     * Get gain
     *
     * @return current gain value
     */
    protected fun getGain(): Int? {
        return getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return@let null
            }
            camera.getGain()
        }
    }

    /**
     * Reset gain
     */
    protected fun resetGain() {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.resetGain()
        }
    }

    /**
     * Set gamma
     *
     * @param gamma camera gamma
     */
    protected fun setGamma(gamma: Int) {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.setGamma(gamma)
        }
    }

    /**
     * Get gamma
     *
     * @return current gamma value
     */
    protected fun getGamma(): Int? {
        return getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return@let null
            }
            camera.getGamma()
        }
    }

    /**
     * Reset gamma
     */
    protected fun resetGamma() {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.resetGamma()
        }
    }

    /**
     * Set hue
     *
     * @param hue camera hue
     */
    protected fun setHue(hue: Int) {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.setHue(hue)
        }
    }

    /**
     * Get hue
     *
     * @return current hue value
     */
    protected fun getHue(): Int? {
        return getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return@let null
            }
            camera.getHue()
        }
    }

    /**
     * Reset hue
     */
    protected fun resetHue() {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.resetHue()
        }
    }

    /**
     * Set zoom
     *
     * @param zoom camera zoom
     */
    protected fun setZoom(zoom: Int) {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.setZoom(zoom)
        }
    }

    /**
     * Get hue
     *
     * @return current hue value
     */
    protected fun getZoom(): Int? {
        return getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return@let null
            }
            camera.getZoom()
        }
    }

    /**
     * Reset hue
     */
    protected fun resetZoom() {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.resetZoom()
        }
    }

    /**
     * Set sharpness
     *
     * @param sharpness camera sharpness
     */
    protected fun setSharpness(sharpness: Int) {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.setSharpness(sharpness)
        }
    }

    /**
     * Get sharpness
     *
     * @return current sharpness value
     */
    protected fun getSharpness(): Int? {
        return getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return@let null
            }
            camera.getSharpness()
        }
    }

    /**
     * Reset sharpness
     */
    protected fun resetSharpness() {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.resetSharpness()
        }
    }

    /**
     * Set saturation
     *
     * @param saturation camera saturation
     */
    protected fun setSaturation(saturation: Int) {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.setSaturation(saturation)
        }
    }

    /**
     * Get saturation
     *
     * @return current saturation value
     */
    protected fun getSaturation(): Int? {
        return getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return@let null
            }
            camera.getSaturation()
        }
    }

    /**
     * Reset saturation
     */
    protected fun resetSaturation() {
        getCurrentCamera()?.let { camera ->
            if (camera !is CameraUVC) {
                return
            }
            camera.resetSaturation()
        }
    }

    protected fun openCamera(st: IAspectRatio? = null) {
        when (st) {
            is TextureView, is SurfaceView -> {
                st
            }
            else -> {
                null
            }
        }.apply {
            getCurrentCamera()?.openCamera(this, getCameraRequest())
            getCurrentCamera()?.setCameraStateCallBack(this@CameraFragment)
        }
    }

    protected fun closeCamera() {
        getCurrentCamera()?.closeCamera()
    }

    private fun surfaceSizeChanged(surfaceWidth: Int, surfaceHeight: Int) {
        getCurrentCamera()?.setRenderSize(surfaceWidth, surfaceHeight)
    }

    private fun getViewLayoutParams(viewGroup: ViewGroup): ViewGroup.LayoutParams {
        return when(viewGroup) {
            is FrameLayout -> {
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    getGravity()
                )
            }
            is LinearLayout -> {
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = getGravity()
                }
            }
            is RelativeLayout -> {
                RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                ).apply{
                    when(getGravity()) {
                        Gravity.TOP -> {
                            addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
                        }
                        Gravity.BOTTOM -> {
                            addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                        }
                        else -> {
                            addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
                            addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
                        }
                    }
                }
            }
            else -> throw IllegalArgumentException("Unsupported container view, " +
                    "you can use FrameLayout or LinearLayout or RelativeLayout")
        }
    }

    /**
     * Get camera view
     *
     * @return CameraView, such as AspectRatioTextureView etc.
     */
    protected abstract fun getCameraView(): IAspectRatio?

    /**
     * Get camera view container
     *
     * @return camera view container, such as FrameLayout ect
     */
    protected abstract fun getCameraViewContainer(): ViewGroup?

    /**
     * Camera render view show gravity
     */
    protected open fun getGravity() = Gravity.CENTER

    protected open fun getCameraRequest(): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewWidth(640)
            .setPreviewHeight(480)
            .setRenderMode(CameraRequest.RenderMode.OPENGL)
            .setDefaultRotateType(RotateType.ANGLE_0)
            .setAudioSource(CameraRequest.AudioSource.SOURCE_SYS_MIC)
//            .setPreviewFormat(CameraRequest.PreviewFormat.FORMAT_MJPEG)
            .setAspectRatioShow(true)
            .setCaptureRawImage(false)
            .setRawPreviewData(false)
            .create()
    }

    companion object {
        private const val TAG = "CameraFragment"
    }
}
