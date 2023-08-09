package io.primer.sample.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.primer.sample.R
import io.primer.sample.databinding.CellCountryItemBinding
import io.primer.sample.datamodels.AppCountryCode

class CountriesAdapter : RecyclerView.Adapter<CountriesAdapter.ViewHolder>() {

    var onItemClick: ((AppCountryCode) -> Unit)? = null

    private val items = mutableListOf<CountryItem>()

    fun setItems(items: List<CountryItem>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding = CellCountryItemBinding.bind(view)

        fun bind(item: CountryItem, onItemClick: ((AppCountryCode) -> Unit)?) {
            binding.countryItem.setText(buildString {
                append(item.countryCode.flag)
                append(' ')
                append(item.countryCode.name)
            })
            binding.root.setOnClickListener { onItemClick?.invoke(item.countryCode) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.cell_country_item,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount(): Int = items.size

    fun setSelected(country: AppCountryCode?) {
        country ?: return
        items.find { it.countryCode == country }?.let {
            it.isSelected
            notifyItemChanged(items.indexOf(it))
        }
    }
}
