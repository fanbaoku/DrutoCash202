package com.cash.ease.money.cashease.widget.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by mahao on 2017/4/19.
 */
abstract class BaseRecyclerAdapter<T>(protected var mData: MutableList<T>) : RecyclerView.Adapter<BaseViewHolder>() {
    // 头部控件
    private var mHeaderView: View? = null

    // 底部控件
    private var mFooterView: View? = null

    private var isHasHeader = false
    private var isHasFooter = false

    fun updateData(data: List<T>) {
        mData = ArrayList(data)
        notifyDataSetChanged()
    }

    fun setData(data: MutableList<T>) {
        mData = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        if (viewType == ITEM_TYPE_FOOTER) {
            // 如果是底部类型，返回底部视图
            return BaseViewHolder(mFooterView!!)
        }
        if (viewType == ITEM_TYPE_HEADER) {
            return BaseViewHolder(mHeaderView!!)
        }
        val view = LayoutInflater.from(parent.context).inflate(getLayoutId(viewType), parent, false)
        return BaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        var position = position
        val type = getItemViewType(position)
        if (type == ITEM_TYPE_HEADER || getItemViewType(position) == ITEM_TYPE_FOOTER) {
            // ignore
        } else {
            position -= if (isHasHeader) 1 else 0
            bindData(holder, position, getItemType(position))
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int, payloads: List<Any>) {
        var position = position
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val type = getItemViewType(position)
            if (type == ITEM_TYPE_HEADER || getItemViewType(position) == ITEM_TYPE_FOOTER) {
                // ignore
            } else {
                position -= if (isHasHeader) 1 else 0
                bindData(holder, position, getItemType(position), payloads)
            }
        }
    }

    val data: List<T>
        get() = mData

    /**
     * 添加头部视图
     */
    fun setHeaderView(header: View?) {
        this.mHeaderView = header
        isHasHeader = true
        notifyDataSetChanged()
    }

    /**
     * 移除头部视图
     */
    fun removeHeaderView() {
        if (isHasHeader) {
            this.mHeaderView = null
            isHasHeader = false
            notifyDataSetChanged()
        }
    }

    /**
     * 添加底部视图
     */
    fun setFooterView(footer: View?) {
        this.mFooterView = footer
        isHasFooter = true
        notifyDataSetChanged()
    }

    /**
     * 移除底部视图
     */
    fun removeFooterView() {
        if (isHasFooter) {
            this.mFooterView = null
            isHasFooter = false
            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        // 根据索引获取当前View的类型，以达到复用的目的
        // 根据位置的索引，获取当前position的类型
        if (isHasHeader && position == 0) {
            return ITEM_TYPE_HEADER
        }
        if (isHasHeader && isHasFooter && position == mData.size + 1) {
            // 有头部和底部时，最后底部的应该等于size+!
            return ITEM_TYPE_FOOTER
        } else if (!isHasHeader && isHasFooter && position == mData.size) {
            // 没有头部，有底部，底部索引为size
            return ITEM_TYPE_FOOTER
        }
        return getItemType(position - (if (isHasHeader) 1 else 0))
    }

    /**
     * 刷新数据
     */
    fun refresh(data: List<T>) {
        mData.clear()
        mData.addAll(data)
        notifyDataSetChanged()
    }

    fun clear() {
        mData.clear()
        notifyDataSetChanged()
    }

    /**
     * 获取item类型
     */
    open fun getItemType(position: Int): Int {
        return ITEM_TYPE_NORMAL
    }

    /**
     * 获取item布局
     */
    abstract fun getLayoutId(itemType: Int): Int

    /**
     * 添加数据
     */
    fun addData(datas: List<T>) {
        mData.addAll(datas)
        notifyDataSetChanged()
    }

    /**
     * 绑定数据
     *
     * @param holder   具体的viewHolder
     * @param position 对应的索引
     * @param itemType 当前条目对应类型
     */
    protected abstract fun bindData(holder: BaseViewHolder, position: Int, itemType: Int)

    protected fun bindData(
        holder: BaseViewHolder,
        position: Int,
        itemType: Int,
        payloads: List<Any>
    ) {
    }

    override fun getItemCount(): Int {
        var size = mData.size
        if (isHasFooter) size++
        if (isHasHeader) size++
        return size
    }

    companion object {
        // item 的三种类型
        const val ITEM_TYPE_NORMAL: Int = 1 // 正常的item类型
        const val ITEM_TYPE_HEADER: Int = 2 // header
        const val ITEM_TYPE_FOOTER: Int = 3 // footer
        const val ITEM_TYPE_GROUP: Int = 4 // 分组条目的item类型
    }
}
