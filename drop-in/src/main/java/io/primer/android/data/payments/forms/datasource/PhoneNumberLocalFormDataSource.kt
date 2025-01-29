package io.primer.android.data.payments.forms.datasource

import io.primer.android.R
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.data.datasource.BaseFlowCacheDataSource
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormDataResponse
import io.primer.android.data.payments.forms.models.FormInputDataResponse
import io.primer.android.data.payments.forms.models.FormType
import io.primer.android.data.payments.forms.models.helper.DialCodeCountryPrefix
import io.primer.android.domain.helper.CountriesRepository
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.settings.PrimerTheme
import kotlinx.coroutines.flow.flow

internal class PhoneNumberLocalFormDataSource(
    private val theme: PrimerTheme,
    private val countriesRepository: CountriesRepository,
    private val paymentMethodType: PaymentMethodType,
) : BaseFlowCacheDataSource<FormDataResponse, String> {
    private val logos =
        mapOf(
            PaymentMethodType.ADYEN_MBWAY to
                mapOf(
                    true to R.drawable.ic_logo_mbway_dark,
                    false to R.drawable.ic_logo_mbway_light,
                ),
            // TODO TWS: get XENDIT_OVO logos when drop-in support is introduced
            PaymentMethodType.XENDIT_OVO to
                mapOf(
                    true to R.drawable.ic_logo_mbway_dark,
                    false to R.drawable.ic_logo_mbway_light,
                ),
        )

    override fun get() =
        flow {
            emit(
                FormDataResponse(
                    title = null,
                    logo = getLogo(theme.isDarkMode == true),
                    buttonType = ButtonType.PAY,
                    description = null,
                    inputs =
                    listOf(
                        FormInputDataResponse(
                            type = FormType.PHONE,
                            id = FORM_ID,
                            hint = R.string.input_hint_form_phone_number,
                            level = null,
                            mask = null,
                            inputCharacters = null,
                            maxInputLength = FORM_PHONE_MAX_LENGTH,
                            // Validation is handled in the component
                            validation = null,
                            inputPrefix =
                            DialCodeCountryPrefix(
                                countriesRepository.getPhoneCodeByCountryCode(getCountryCode()),
                            ),
                        ),
                    ),
                ),
            )
        }

    private fun getLogo(isDarkMode: Boolean) =
        when (paymentMethodType) {
            PaymentMethodType.ADYEN_MBWAY,
            PaymentMethodType.XENDIT_OVO,
            -> logos.getValue(paymentMethodType).getValue(isDarkMode)

            else -> error("Unsupported payment method type '$paymentMethodType'")
        }

    private fun getCountryCode() =
        when (paymentMethodType) {
            PaymentMethodType.ADYEN_MBWAY -> CountryCode.PT
            PaymentMethodType.XENDIT_OVO -> CountryCode.ID
            else -> error("Unsupported payment method type '$paymentMethodType'")
        }

    private companion object {
        const val FORM_ID = "phoneNumber"
        const val FORM_PHONE_MAX_LENGTH = 18
    }
}
