package com.rnuvccamera.native.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

object DimenUtil {
    fun dp2px(dpValue: Float): Float {
        val density = Resources.getSystem().displayMetrics.density
        return dpValue * density + 0.5f
    }

    fun dp2px(context: Context, dpValue: Float): Float {
        val density = context.resources.displayMetrics.density
        return dpValue * density + 0.5f
    }

    fun px2dp(pxValue: Float): Float {
        val density = Resources.getSystem().displayMetrics.density
        return pxValue / density + 0.5f
    }

    fun px2dp(context: Context, pxValue: Float): Float {
        val density = context.resources.displayMetrics.density
        return pxValue / density + 0.5f
    }

    fun sp2px(context: Context, spValue: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.resources.displayMetrics)
    }

    fun getScreenW(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun getScreenH(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    fun getDefaultDialogW(context: Context): Int {
        return (getScreenW(context) * (3 / 4f)).toInt()
    }

    fun getDefaultDialogH(context: Context): Int {
        return (getScreenH(context) - dp2px(context, 60f)).toInt()
    }
} 