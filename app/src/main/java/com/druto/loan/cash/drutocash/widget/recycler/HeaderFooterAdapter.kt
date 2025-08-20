package com.druto.loan.cash.drutocash.widget.recycler

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.druto.loan.cash.drutocash.widget.recycler.HeaderFooterAdapter.HeaderFooterViewHolder

/**
 * Created by su on 16-7-20.
 */
abstract class HeaderFooterAdapter protected constructor(
    private val mHasHeader: Boolean,
    private var mHasFooter: Boolean
) :
    RecyclerView.Adapter<HeaderFooterViewHolder>() {
    abstract override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HeaderFooterViewHolder

    abstract override fun onBindViewHolder(holder: HeaderFooterViewHolder, position: Int)

    override fun getItemViewType(position: Int): Int {
        if (isPositionHeader(position)) return TYPE_HEADER
        if (isPositionFooter(position)) return TYPE_FOOTER
        return getItemDataType(position)
    }

    abstract fun getItemDataType(position: Int): Int

    fun countHeader(): Int {
        return if (mHasHeader) 1 else 0
    }

    fun countFooter(): Int {
        return if (mHasFooter) 1 else 0
    }

    private fun isPositionHeader(position: Int): Boolean {
        return mHasHeader && position == 0
    }

    private fun isPositionFooter(position: Int): Boolean {
        return mHasFooter && position == itemCount - 1
    }

    fun removeFooter() {
        mHasFooter = false
    }

    fun addFooter() {
        mHasFooter = true
    }

    class HeaderFooterViewHolder(itemView: View, val type: Int) : RecyclerView.ViewHolder(itemView)
    companion object {
        const val TYPE_INVALID: Int = -10000
        const val TYPE_CHILD: Int = -4
        const val TYPE_GROUP: Int = -3
        const val TYPE_HEADER: Int = -2
        const val TYPE_FOOTER: Int = -1
        const val TYPE_NORMAL: Int = 0

        fun isHeader(viewType: Int): Boolean {
            return viewType == TYPE_HEADER
        }

        fun isFooter(viewType: Int): Boolean {
            return viewType == TYPE_FOOTER
        }
    }
}
