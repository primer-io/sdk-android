package io.primer.android.ui.core.payment.domain.interactor

import io.primer.android.configuration.domain.model.Surcharge
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod

class SurchargeCalculationInteractor {

    fun getSurchargeForSavedPaymentMethod(
        token: PrimerVaultedPaymentMethod?,
        surcharges: Map<String, Surcharge>,
    ): Int {
        if (token == null) return 0
        val type = token.paymentMethodType
        return getSurchargeForPaymentMethodType(
            type = type,
            network = token.paymentInstrumentData.binData?.network,
            surcharges = surcharges,
        )
    }

    fun getSurchargeForPaymentMethodType(
        type: String,
        network: String? = null,
        surcharges: Map<String, Surcharge>,
    ): Int {
        return when (val surcharge = surcharges[type]) {
            is Surcharge.CardNetworksSurcharge -> surcharge.surcharges[network] ?: 0
            is Surcharge.PaymentMethodSurcharge -> surcharge.amount
            null -> 0
        }
    }
}
