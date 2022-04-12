package io.primer.android.ui.fragments.country

import android.view.LayoutInflater
import android.view.ViewGroup
import io.primer.android.PrimerTheme
import io.primer.android.databinding.ItemCountrySelectBinding
import io.primer.android.model.dto.CountryCode
import io.primer.android.ui.base.recyclerview.BaseAdapterItem
import io.primer.android.ui.base.recyclerview.BaseRecyclerViewAdapter
import io.primer.android.ui.base.recyclerview.BaseViewHolder

data class CountryItem(
    val name: String,
    val code: CountryCode,
    val flag: String
) : BaseAdapterItem {
    override fun getType(): Int = 0
}

internal class CountryItemViewHolder(
    private val binding: ItemCountrySelectBinding,
    private val theme: PrimerTheme,
    private val onItemSelect: (CountryCode) -> Unit
) : BaseViewHolder<CountryItem>(binding.root) {

    override fun bind(item: CountryItem) {
        binding.tvName.text = String.format("%s %s", item.flag, item.name)
        binding.tvName.setTextColor(
            theme.titleText.defaultColor.getColor(
                binding.root.context,
                theme.isDarkMode
            )
        )
        itemView.setOnClickListener { onItemSelect(item.code) }
    }
}

internal class CountriesSelectionAdapter(
    private val onItemSelect: (CountryCode) -> Unit,
    private val theme: PrimerTheme
) : BaseRecyclerViewAdapter<CountryItem>({ c1, c2 -> c1.code == c2.code }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<CountryItem> {
        val binding = ItemCountrySelectBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return CountryItemViewHolder(binding, theme, onItemSelect)
    }
}
