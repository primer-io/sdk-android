package com.example.myapplication.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.CellCountryItemBinding
import com.example.myapplication.datamodels.AppCountryCode

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
            binding.countryItem.setText(item.countryCode.flag)
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
