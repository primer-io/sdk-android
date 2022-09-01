package io.primer.android.ui.fragments.country

import io.primer.android.R
import io.primer.android.data.configuration.models.CountryCode
import io.primer.android.domain.action.models.PrimerCountry

internal class SelectCountryFragment(
    private val onSelectCountry: ((PrimerCountry) -> Unit)? = null
) : BaseCountryChooserFragment() {

    override val dataSourceType: CountryDataType = CountryDataType.NAME

    override fun onSelectCountryCode(code: CountryCode) {
        viewModel.getCountryByCode(code) { country ->
            primerViewModel.setSelectedCountry(country)
            onSelectCountry?.invoke(country)
            parentFragmentManager.popBackStack()
        }
    }

    override val searchPlaceholderId: Int = R.string.country_search_hint

    companion object {

        @JvmStatic
        fun newInstance(onSelectCountry: ((PrimerCountry) -> Unit)? = null): SelectCountryFragment {
            return SelectCountryFragment(onSelectCountry)
        }
    }
}
