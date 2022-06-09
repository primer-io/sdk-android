package io.primer.android.data.payments.forms.datasource

import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.data.token.model.ClientToken
import java.lang.IllegalStateException

internal class LocalFormDataSourceFactory(private val primerTheme: PrimerTheme) {

    fun getLocalFormDataSource(paymentMethodType: PaymentMethodType, clientToken: ClientToken) =
        when (paymentMethodType) {
            PaymentMethodType.ADYEN_BLIK -> BlikLocalFormDataSource(primerTheme)
            PaymentMethodType.ADYEN_MBWAY -> MbWayLocalFormDataSource()
            PaymentMethodType.ADYEN_BANK_TRANSFER -> SepaLocalFormDataSource(primerTheme)
            PaymentMethodType.XFERS_PAYNOW -> XfersLocalFormDataSource(clientToken)
            else -> throw IllegalStateException("Invalid paymentMethodType $paymentMethodType")
        }
}
