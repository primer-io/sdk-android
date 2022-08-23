package io.primer.android.ui.fragments.phonecodes

import io.primer.android.R
import io.primer.android.data.configuration.models.CountryCode
import io.primer.android.ui.fragments.country.BaseCountryChooserFragment
import io.primer.android.ui.fragments.country.CountryDataType

internal class SelectPhoneCodeFragment : BaseCountryChooserFragment() {

    override val dataSourceType: CountryDataType = CountryDataType.DIAL_CODE

    override fun onSelectCountryCode(code: CountryCode) {
        viewModel.getPhoneCodeByCountryCode(code) { country ->
            primerViewModel.setSelectedPhoneCode(country)
            parentFragmentManager.popBackStack()
        }
    }

    override val searchPlaceholderId: Int = R.string.phone_code_search_hint

    companion object {

        @JvmStatic
        fun newInstance(): SelectPhoneCodeFragment {
            return SelectPhoneCodeFragment()
        }
    }
}
