package com.rnuvccamera.native.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewAdapter<Data, VH : ViewDataBinding> :
    RecyclerView.Adapter<BaseViewHolder<VH>>() {

    var dataList = ArrayList<Data>()
    var onItemClickListener: OnItemClickListener<Data>? = null
    private var onItemLongClickListener: OnItemLongClickListener<Data>? = null

    override fun getItemCount(): Int = dataList.size

    fun updateList(list: List<Data>) {
        dataList.clear()
        if (list.isNotEmpty()) {
            dataList.addAll(list)
        }
        notifyDataSetChanged()
    }

    fun addItem(item: Data?) {
        item?.let {
            dataList.add(it)
            notifyDataSetChanged()
        }
    }

    fun removeItem(position: Int) {
        if (position in 0 until dataList.size) {
            dataList.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun getItem(position: Int): Data? {
        return if (position in 0 until dataList.size) dataList[position] else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VH> {
        val binding: VH = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            getLayoutRes(),
            parent,
            false
        )
        val baseViewHolder = BaseViewHolder(binding)
        baseViewHolder.itemView.setOnLongClickListener { v ->
            onItemLongClickListener?.let {
                val adapterPosition = baseViewHolder.adapterPosition
                val data = dataList[adapterPosition]
                it.onItemLongClick(v, data, adapterPosition)
            }
            false
        }
        return baseViewHolder
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VH>, position: Int) {
        val data = dataList[position]
        onItemClickListener?.let { listener ->
            holder.itemView.setOnClickListener { view ->
                listener.onItemClick(view, data, holder.adapterPosition)
            }
        }
        onBindViewHolder(holder, data, position)
    }

    abstract fun getLayoutRes(): Int
    abstract fun onBindViewHolder(holder: BaseViewHolder<VH>, item: Data, position: Int)

    interface OnItemClickListener<Data> {
        fun onItemClick(itemView: android.view.View, itemBean: Data, position: Int)
    }

    interface OnItemLongClickListener<Data> {
        fun onItemLongClick(itemView: android.view.View, itemBean: Data, position: Int)
    }
} 