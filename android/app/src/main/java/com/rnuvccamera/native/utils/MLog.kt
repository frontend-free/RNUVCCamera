package com.rnuvccamera.native.utils

import android.os.Environment
import android.util.Log
import java.io.File

object MLog {
    private val LOG_PATH = "${Environment.getExternalStorageDirectory().absolutePath}/TX_DrivingTest/log"
    private const val APP_LOG_NAME = "app.log"
    private var isShowLog = false
    private var defaultMsg = ""
    private const val V = 1
    private const val D = 2
    private const val I = 3
    private const val W = 4
    private const val E = 5

    fun init(isShowLog: Boolean) {
        MLog.isShowLog = isShowLog
    }

    fun init(isShowLog: Boolean, defaultMsg: String) {
        MLog.isShowLog = isShowLog
        MLog.defaultMsg = defaultMsg
    }

    fun v() {
        llog(V, null, defaultMsg)
    }

    fun v(obj: Any?) {
        llog(V, null, obj)
    }

    fun v(tag: String?, obj: Any?) {
        llog(V, tag, obj)
    }

    fun d() {
        llog(D, null, defaultMsg)
    }

    fun d(obj: Any?) {
        llog(D, null, obj)
    }

    fun d(tag: String?, obj: Any?) {
        llog(D, tag, obj)
    }

    fun i() {
        llog(I, null, defaultMsg)
    }

    fun i(obj: Any?) {
        llog(I, null, obj)
    }

    fun i(tag: String?, obj: String?) {
        llog(I, tag, obj)
    }

    fun w() {
        llog(W, null, defaultMsg)
    }

    fun w(obj: Any?) {
        llog(W, null, obj)
    }

    fun w(tag: String?, obj: Any?) {
        llog(W, tag, obj)
    }

    fun e() {
        llog(E, null, defaultMsg)
    }

    fun e(obj: Any?) {
        llog(E, null, obj)
    }

    fun e(tag: String?, obj: Any?) {
        llog(E, tag, obj)
    }

    fun llog(type: Int, tagStr: String?, obj: Any?) {
        if (!isShowLog) return

        val stackTrace = Thread.currentThread().stackTrace
        val index = 4
        val className = stackTrace[index].fileName
        var methodName = stackTrace[index].methodName
        val lineNumber = stackTrace[index].lineNumber

        val tag = tagStr ?: className
        methodName = methodName.substring(0, 1).uppercase() + methodName.substring(1)

        val stringBuilder = StringBuilder()
        stringBuilder.append("[ (").append(className).append(":").append(lineNumber).append(")#").append(methodName).append(" ] ")

        val msg = obj?.toString() ?: "Log with null Object"
        stringBuilder.append(msg)

        val logStr = stringBuilder.toString()

        when (type) {
            V -> Log.v(tag, logStr)
            D -> Log.d(tag, logStr)
            I -> Log.i(tag, logStr)
            W -> Log.w(tag, logStr)
            E -> Log.e(tag, logStr)
        }
    }
} 