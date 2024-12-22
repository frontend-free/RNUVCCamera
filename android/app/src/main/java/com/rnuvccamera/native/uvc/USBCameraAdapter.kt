package com.rnuvccamera.native.uvc

import com.jiangdg.ausbc.MultiCameraClient
import com.rnuvccamera.R
import com.rnuvccamera.databinding.ItemUvcCameraLayoutBinding
import com.rnuvccamera.native.base.BaseRecyclerViewAdapter
import com.rnuvccamera.native.base.BaseViewHolder
import com.rnuvccamera.native.utils.ContextUtil
import com.rnuvccamera.native.utils.DimenUtil


/**
 * Copyright (c)
 *
 * @description 描述该文件做什么
 * @author chen_wei
 * @date 2024/1/31
 */
class UvcCameraAdapter :
    BaseRecyclerViewAdapter<MultiCameraClient.ICamera, ItemUvcCameraLayoutBinding>() {
    override fun onBindViewHolder(
        holder: BaseViewHolder<ItemUvcCameraLayoutBinding>,
        item: MultiCameraClient.ICamera,
        position: Int
    ) {
        item ?: return
        val layoutParams = holder.binding.surface.layoutParams
        val screenW = DimenUtil.getScreenW(ContextUtil.getContext())
        val height = 634 * screenW / 1080f
        layoutParams.height = height.toInt()
        holder.binding.surface.layoutParams = layoutParams
    }

    override fun getLayoutRes(): Int = R.layout.item_uvc_camera_layout
}