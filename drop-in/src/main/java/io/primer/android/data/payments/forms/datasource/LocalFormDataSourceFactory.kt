package io.primer.android.data.payments.forms.datasource

import io.primer.android.domain.helper.CountriesRepository
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.settings.PrimerTheme

internal class LocalFormDataSourceFactory(
    private val primerTheme: PrimerTheme,
    private val countriesRepository: CountriesRepository,
) {
    fun getLocalFormDataSource(paymentMethodType: PaymentMethodType) =
        when (paymentMethodType) {
            PaymentMethodType.ADYEN_BLIK -> BlikLocalFormDataSource(primerTheme)
            PaymentMethodType.XENDIT_OVO,
            PaymentMethodType.ADYEN_MBWAY,
            ->
                PhoneNumberLocalFormDataSource(
                    theme = primerTheme,
                    countriesRepository = countriesRepository,
                    paymentMethodType = paymentMethodType,
                )

            PaymentMethodType.XFERS_PAYNOW -> XfersLocalFormDataSource()
            PaymentMethodType.RAPYD_FAST ->
                FastBankTransferLocalFormDataSource(primerTheme)

            PaymentMethodType.OMISE_PROMPTPAY,
            PaymentMethodType.RAPYD_PROMPTPAY,
            ->
                PromptPayLocalFormDataSource(primerTheme)

            PaymentMethodType.ADYEN_MULTIBANCO -> MultibancoLocalFormDataSource(primerTheme)
            PaymentMethodType.ADYEN_BANCONTACT_CARD ->
                BancontactCardLocalFormDataSource(
                    primerTheme,
                )

            else -> error("Invalid paymentMethodType $paymentMethodType")
        }
}
