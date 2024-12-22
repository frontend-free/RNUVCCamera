package com.rnuvccamera.native.utils

import android.text.TextUtils
import java.math.BigDecimal

object NumberUtil {
    fun stringToInt(str: String?): Int {
        if (TextUtils.isEmpty(str)) return 0
        return try {
            str!!.toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun parseLong(str: String?): Long {
        if (TextUtils.isEmpty(str)) return 0
        return try {
            str!!.toLong()
        } catch (e: Exception) {
            0
        }
    }

    fun stringToFloat(str: String?): Float {
        if (TextUtils.isEmpty(str)) return 0.0f
        return try {
            str!!.toFloat()
        } catch (e: Exception) {
            0.0f
        }
    }

    fun stringToDouble(str: String?): Double {
        if (TextUtils.isEmpty(str)) return 0.00
        return try {
            str!!.toDouble()
        } catch (e: Exception) {
            0.00
        }
    }

    fun doubleToStringThree(num: Double): String {
        return String.format("%.3f", num)
    }

    fun doubleToStringTwo(num: Double): String {
        return String.format("%.2f", num)
    }

    fun doubleToStringTwelve(num: Double): String {
        return String.format("%.12f", num)
    }

    fun toStrRemoveZero(num: Double): String {
        val bd = BigDecimal(num.toString())
        return bd.stripTrailingZeros().toPlainString()
    }

    fun floatValue(num: Double): Float {
        return try {
            num.toFloat()
        } catch (e: Exception) {
            0.0f
        }
    }

    fun stringToLong(str: String?): Long {
        var num: Long = 0
        if (str == null || str.isEmpty()) {
            return num
        }
        return try {
            str.trim().toLong()
        } catch (e: Exception) {
            num
        }
    }
} 