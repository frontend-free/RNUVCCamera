package com.rnuvccamera.native.utils

import android.content.Context
import java.io.BufferedWriter
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class AppOperateUtils private constructor() {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private var crashWriter: BufferedWriter? = null
    private val executorService = Executors.newSingleThreadExecutor()

    companion object {
        const val TYPE_OPERATE = 1
        const val TYPE_CRASH = 2

        private val instance: AppOperateUtils by lazy { AppOperateUtils() }

        fun crashLogFile(context: Context, crashLog: String) {
            instance.executorService.execute {
                try {
                    if (instance.crashWriter == null) {
                        val logPath = "${context.cacheDir.absolutePath}/crash.log"
                        instance.crashWriter = BufferedWriter(FileWriter(logPath, true))
                    }
                    instance.crashWriter?.apply {
                        write(instance.dateFormat.format(Date()))
                        write(",")
                        write(crashLog)
                        newLine()
                        flush()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
} 