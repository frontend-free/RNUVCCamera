package com.rnuvccamera.native.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class BaseViewHolder<VH : ViewDataBinding>(val binding: VH) : RecyclerView.ViewHolder(binding.root) 