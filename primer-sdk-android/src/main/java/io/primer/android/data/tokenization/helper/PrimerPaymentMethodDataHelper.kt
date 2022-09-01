package io.primer.android.data.tokenization.helper

import io.primer.android.data.payments.create.models.PaymentResponse
import io.primer.android.data.payments.create.models.RequiredActionName
import io.primer.android.data.payments.create.models.toPaymentResult
import io.primer.android.data.token.model.ClientToken
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.domain.payments.create.model.PaymentResult
import io.primer.android.domain.payments.additionalInfo.MultibancoCheckoutAdditionalInfo
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfo

internal class PrimerPaymentMethodDataHelper {

    fun preparePaymentResult(response: PaymentResponse): PaymentResult {
        return when (response.requiredAction?.name) {
            RequiredActionName.PAYMENT_METHOD_VOUCHER -> {
                val clientToken = response.requiredAction.clientToken
                val paymentMethodData = clientToken?.let {
                    val clientTokenData = ClientToken.fromString(clientToken)
                    prepareDataFromClientToken(clientTokenData)
                }
                response.toPaymentResult(paymentMethodData)
            }
            else -> response.toPaymentResult()
        }
    }

    fun prepareDataFromClientToken(
        clientTokenData: ClientToken
    ): PrimerCheckoutAdditionalInfo? {
        return when (clientTokenData.intent) {
            ClientTokenIntent.PAYMENT_METHOD_VOUCHER.name -> MultibancoCheckoutAdditionalInfo(
                clientTokenData.expiresAt.orEmpty(),
                clientTokenData.reference.orEmpty(),
                clientTokenData.entity.orEmpty()
            )
            else -> null
        }
    }
}
