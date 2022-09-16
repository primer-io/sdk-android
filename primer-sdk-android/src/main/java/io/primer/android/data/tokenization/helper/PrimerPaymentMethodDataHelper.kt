package io.primer.android.data.tokenization.helper

import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.create.models.PaymentDataResponse
import io.primer.android.data.payments.create.models.RequiredActionName
import io.primer.android.data.payments.create.models.toPaymentResult
import io.primer.android.data.token.model.ClientToken
import io.primer.android.domain.payments.create.model.PaymentResult
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest

internal class PrimerPaymentMethodDataHelper(
    private val paymentMethodsRepository: PaymentMethodsRepository
) {

    suspend fun preparePaymentResult(response: PaymentDataResponse): PaymentResult {
        return when (response.requiredAction?.name) {
            RequiredActionName.PAYMENT_METHOD_VOUCHER -> {
                val clientToken = response.requiredAction.clientToken.orEmpty()
                val clientTokenData = ClientToken.fromString(clientToken)
                val descriptor =
                    paymentMethodsRepository.getPaymentMethodDescriptors()
                        .mapLatest { descriptors ->
                            descriptors.first { descriptor ->
                                PaymentMethodType.safeValueOf(descriptor.config.type).intents
                                    ?.map { it.name }?.contains(clientTokenData.intent) == true
                            }
                        }.first()
                val additionalInfo = descriptor.additionalInfoResolver?.resolve(clientTokenData)
                response.toPaymentResult(additionalInfo)
            }
            else -> response.toPaymentResult()
        }
    }
}
