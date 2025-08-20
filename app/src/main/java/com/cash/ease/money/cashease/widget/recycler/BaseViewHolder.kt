package com.cash.ease.money.cashease.widget.recycler

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val mViewMap: MutableMap<Int, View?> =
        HashMap()

    /**
     * 根据id获取布局上的view
     */
    fun <S : View?> getView(id: Int): S? {
        var view = mViewMap[id]
        if (view == null) {
            view = itemView.findViewById(id)
            mViewMap[id] = view
        }
        return view as S?
    }
}
