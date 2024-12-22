package com.rnuvccamera.native.utils

import android.app.Application
import android.content.Context

object ContextUtil {
    private var application: Application? = null

    fun init(application: Application): ContextUtil {
        if (this.application == null) {
            synchronized(ContextUtil::class.java) {
                if (this.application == null) {
                    this.application = application
                }
            }
        }
        return this
    }

    fun getContext(): Context {
        return application ?: throw NullPointerException("Context 未初始化")
    }
} 