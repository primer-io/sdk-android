package io.primer.android.ui.base.recyclerview

import androidx.recyclerview.widget.RecyclerView
import io.primer.android.ui.extensions.autoNotify
import kotlin.properties.Delegates

internal abstract class BaseRecyclerViewAdapter<T : BaseAdapterItem>(compare: (T, T) -> Boolean) :
    RecyclerView.Adapter<BaseViewHolder<T>>() {

    var items: List<T> by Delegates.observable(emptyList()) { _, oldList, newList ->
        autoNotify(oldList, newList, compare)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.bind(items[position])
    }

    override fun onViewRecycled(holder: BaseViewHolder<T>) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = items[position].getType()
}
