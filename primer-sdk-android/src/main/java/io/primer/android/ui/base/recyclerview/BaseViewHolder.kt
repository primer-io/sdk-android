package io.primer.android.ui.base.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView

internal abstract class BaseViewHolder<T : BaseAdapterItem>(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: T)

    open fun unbind() = Unit
}
