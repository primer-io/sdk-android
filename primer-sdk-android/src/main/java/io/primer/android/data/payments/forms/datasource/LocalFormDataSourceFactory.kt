package io.primer.android.data.payments.forms.datasource

import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.token.model.ClientToken
import io.primer.android.domain.helper.CountriesRepository
import io.primer.android.ui.settings.PrimerTheme

internal class LocalFormDataSourceFactory(
    private val primerTheme: PrimerTheme,
    private val countriesRepository: CountriesRepository
) {

    fun getLocalFormDataSource(paymentMethodType: PaymentMethodType, clientToken: ClientToken) =
        when (paymentMethodType) {
            PaymentMethodType.ADYEN_BLIK -> BlikLocalFormDataSource(primerTheme)
            PaymentMethodType.ADYEN_MBWAY -> MbWayLocalFormDataSource(
                primerTheme,
                countriesRepository
            )
            PaymentMethodType.ADYEN_BANK_TRANSFER -> SepaLocalFormDataSource(primerTheme)
            PaymentMethodType.XFERS_PAYNOW -> XfersLocalFormDataSource(clientToken)
            PaymentMethodType.RAPYD_FAST ->
                FastBankTransferLocalFormDataSource(primerTheme, clientToken)
            PaymentMethodType.RAPYD_PROMPTPAY ->
                PromptPayLocalFormDataSource(primerTheme, clientToken)
            else -> throw IllegalStateException("Invalid paymentMethodType $paymentMethodType")
        }
}
