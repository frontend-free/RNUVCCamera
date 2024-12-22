package com.rnuvccamera.native.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {

    fun getThisNowDate(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date(System.currentTimeMillis())
        return format.format(date)
    }

    fun getThisNowDate(pattern: String): String {
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        val date = Date(System.currentTimeMillis())
        return format.format(date)
    }

    fun getThisNowDateMillisecond(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        val date = Date(System.currentTimeMillis())
        return format.format(date)
    }

    fun getOldDate(distanceDay: Int, pattern: String): String {
        val dft = SimpleDateFormat(pattern, Locale.getDefault())
        val beginDate = Date()
        val date = Calendar.getInstance()
        date.time = beginDate
        date.add(Calendar.DATE, distanceDay)
        return dft.format(date.time)
    }

    fun timeStampToDate(time: Long, pattern: String): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(time)
    }

    fun paresDateToTimeStamp(date: String, pattern: String): Long {
        return try {
            val format = SimpleDateFormat(pattern, Locale.getDefault())
            val parse = format.parse(date)
            parse?.time ?: 0L
        } catch (e: Exception) {
            MLog.d("解析失败：" + e.message)
            0L
        }
    }

    fun paresDateToTimeStamp(date: String, format: SimpleDateFormat?): Long {
        if (format == null) return 0L
        return try {
            val parse = format.parse(date)
            parse?.time ?: 0L
        } catch (e: Exception) {
            MLog.d("解析失败：" + e.message)
            0L
        }
    }
} 