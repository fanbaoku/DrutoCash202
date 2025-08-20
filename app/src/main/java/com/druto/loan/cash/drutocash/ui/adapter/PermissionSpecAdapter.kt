package com.druto.loan.cash.drutocash.ui.adapter

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.druto.loan.cash.drutocash.R
import com.druto.loan.cash.drutocash.widget.recycler.BaseRecyclerAdapter
import com.druto.loan.cash.drutocash.widget.recycler.BaseViewHolder

class PermissionSpecAdapter(val context: Context, data: MutableList<CharSequence>) :
    BaseRecyclerAdapter<CharSequence>(data) {
    override fun getItemType(position: Int): Int {
        return if (position == 0) {
            ITEM_TYPE_DESC
        } else if (position % 2 == 1) {
            ITEM_TYPE_TITLE
        } else {
            ITEM_TYPE_CONTENT
        }
    }

    override fun getLayoutId(itemType: Int): Int {
        return when (itemType) {
            ITEM_TYPE_DESC -> R.layout.item_permission_spec_desc
            ITEM_TYPE_TITLE -> R.layout.item_permission_spec_title
            else -> R.layout.item_permission_spec_content
        }
    }

    override fun bindData(
        holder: BaseViewHolder, position: Int,
        itemType: Int
    ) {
        val textView = holder.getView<TextView>(R.id.content)
        val iconView = holder.getView<ImageView>(R.id.icon)
        textView?.text = data[position]
        val resourceName = "permission_info_${position / 2}"
        val id = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        iconView?.setImageResource(id)
    }

    companion object {
        private const val ITEM_TYPE_DESC = 5
        private const val ITEM_TYPE_TITLE = 6
        private const val ITEM_TYPE_CONTENT = 7
    }
}
