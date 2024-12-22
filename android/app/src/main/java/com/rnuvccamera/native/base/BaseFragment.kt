package com.rnuvccamera.native.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
//import org.greenrobot.eventbus.EventBus

/**
 * Copyright (c)
 *
 * @author chen_wei
 * @description 描述该文件做什么
 * @date 2023/3/4
 */
abstract class BaseFragment<T : ViewDataBinding> : Fragment() {
    lateinit var viewBinding: T
    abstract fun getLayoutRes(): Int
    abstract fun setUpViewAndData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (getLayoutRes() != 0) {
            viewBinding = DataBindingUtil.inflate(inflater, getLayoutRes(), container, false)
        }
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewAndData()
    }

//    fun registerEventBus() {
//        EventBus.getDefault().register(this)
//    }
//
//    fun unregisterEventBus() {
//        EventBus.getDefault().unregister(this)
//    }
}