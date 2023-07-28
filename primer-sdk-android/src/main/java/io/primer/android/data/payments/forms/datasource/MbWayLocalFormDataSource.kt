package io.primer.android.data.payments.forms.datasource

import io.primer.android.R
import io.primer.android.components.domain.payments.paymentMethods.raw.phoneNumber.mbway.MBWayPhoneNumberValidator
import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import io.primer.android.data.configuration.models.CountryCode
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormDataResponse
import io.primer.android.data.payments.forms.models.FormInputDataResponse
import io.primer.android.data.payments.forms.models.FormType
import io.primer.android.data.payments.forms.models.helper.DialCodeCountryPrefix
import io.primer.android.domain.helper.CountriesRepository
import io.primer.android.ui.settings.PrimerTheme
import kotlinx.coroutines.flow.flowOf

internal class MbWayLocalFormDataSource(
    private val theme: PrimerTheme,
    private val countriesRepository: CountriesRepository
) : BaseFlowCacheDataSource<FormDataResponse, String> {

    override fun get() = flowOf(
        FormDataResponse(
            null,
            if (theme.isDarkMode == true) R.drawable.ic_logo_mbway_dark
            else R.drawable.ic_logo_mbway_light,
            ButtonType.PAY,
            null,
            listOf(
                FormInputDataResponse(
                    FormType.PHONE,
                    FORM_ID,
                    R.string.input_hint_form_phone_number,
                    null,
                    null,
                    null,
                    FORM_PHONE_MAX_LENGTH,
                    MBWayPhoneNumberValidator.PHONE_NUMBER_REGEX.pattern,
                    DialCodeCountryPrefix(
                        countriesRepository.getPhoneCodeByCountryCode(CountryCode.PT)
                    )
                )
            )
        )
    )

    private companion object {

        const val FORM_ID = "phoneNumber"
        const val FORM_PHONE_MAX_LENGTH = 18
    }
}
