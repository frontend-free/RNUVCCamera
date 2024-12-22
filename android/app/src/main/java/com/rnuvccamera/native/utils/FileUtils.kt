package com.taixuan.subjectlive.utils

import android.text.TextUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.charset.Charset

/**
 * Copyright (c)
 *
 * @description 描述该文件做什么
 * @author chen_wei
 * @date 2024/2/18
 */
object FileUtils {

    fun readString(filePath: String): String? {
        if (TextUtils.isEmpty(filePath)) return null
        val srcFile = File(filePath)
        if (!srcFile.exists()) return null
        var fileInputStream: FileInputStream? = null
        var byteArrayOutputStream: ByteArrayOutputStream? = null
        try {
            fileInputStream = FileInputStream(srcFile)
            byteArrayOutputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var len: Int
            while (fileInputStream.read(buffer).also { len = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, len)
            }
            byteArrayOutputStream.flush()
            return String(byteArrayOutputStream.toByteArray(), Charset.forName("UTF-8"))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
        }
        return null
    }
}