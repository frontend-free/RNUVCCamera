package com.rnuvccamera.native.utils

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Environment
import android.os.StatFs
import android.telephony.TelephonyManager
import java.io.File

object AppUtil {

    fun getVersionCode(context: Context): Int {
        val packageManager = context.packageManager
        return try {
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            0
        }
    }

    fun getVersionName(context: Context): String {
        val packageManager = context.packageManager
        return try {
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    fun getSystemProperty(key: String): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java)
            method.invoke(null, key) as String
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun getIMEI(context: Context): String {
        var imei = "000000000000001"
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val method = tm.javaClass.getMethod("getImei", Int::class.javaPrimitiveType)
            imei = method.invoke(tm, 0) as String
        } catch (e: Exception) {
            MLog.e("获取IMEI异常：" + e.message)
            e.printStackTrace()
        }
        return if (imei.isEmpty()) "000000000000001" else imei
    }

    fun getRAMSize(): Long {
        val activityManager = ContextUtil.getContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem
    }

} 