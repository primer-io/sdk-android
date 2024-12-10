package io.primer.android.ui.fragments.country

import android.view.LayoutInflater
import android.view.ViewGroup
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.databinding.ItemCountrySelectBinding
import io.primer.android.ui.base.recyclerview.BaseAdapterItem
import io.primer.android.ui.base.recyclerview.BaseRecyclerViewAdapter
import io.primer.android.ui.base.recyclerview.BaseViewHolder

internal data class CountryCodeItem(
    val code: CountryCode,
    val displayLabel: String
) : BaseAdapterItem {
    override fun getType(): Int = 0
}

internal class CountryItemViewHolder(
    private val binding: ItemCountrySelectBinding,
    private val theme: PrimerTheme,
    private val onItemSelect: (CountryCode) -> Unit
) : BaseViewHolder<CountryCodeItem>(binding.root) {

    override fun bind(item: CountryCodeItem) {
        binding.tvName.text = item.displayLabel
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
) : BaseRecyclerViewAdapter<CountryCodeItem>({ c1, c2 -> c1.code == c2.code }) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<CountryCodeItem> {
        val binding = ItemCountrySelectBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return CountryItemViewHolder(binding, theme, onItemSelect)
    }
}
