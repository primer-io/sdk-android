package io.primer.android.ui.fragments.country

import io.primer.android.R
import io.primer.android.data.configuration.models.CountryCode

internal class SelectCountryFragment : BaseCountryChooserFragment() {

    override val dataSourceType: CountryDataType = CountryDataType.NAME

    override fun onSelectCountryCode(code: CountryCode) {
        viewModel.getCountryByCode(code) { country ->
            primerViewModel.setSelectedCountry(country)
            parentFragmentManager.popBackStack()
        }
    }

    override val searchPlaceholderId: Int = R.string.country_search_hint

    companion object {

        @JvmStatic
        fun newInstance(): SelectCountryFragment {
            return SelectCountryFragment()
        }
    }
}
