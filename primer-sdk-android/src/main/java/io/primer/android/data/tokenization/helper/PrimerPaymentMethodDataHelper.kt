package io.primer.android.data.tokenization.helper

import io.primer.android.data.payments.create.models.PaymentDataResponse
import io.primer.android.data.payments.create.models.RequiredActionName
import io.primer.android.data.payments.create.models.toPaymentResult
import io.primer.android.data.token.model.ClientToken
import io.primer.android.domain.payments.additionalInfo.RetailOutletsCheckoutAdditionalInfoResolver
import io.primer.android.domain.payments.create.model.PaymentResult
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest

internal class PrimerPaymentMethodDataHelper(
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentMethodsRepository: PaymentMethodsRepository,
    private val retailerOutletsRepository: RetailOutletRepository
) {

    suspend fun preparePaymentResult(response: PaymentDataResponse): PaymentResult {
        return when (response.requiredAction?.name) {
            RequiredActionName.PAYMENT_METHOD_VOUCHER -> {
                val clientToken = response.requiredAction.clientToken.orEmpty()
                val clientTokenData = ClientToken.fromString(clientToken)
                val descriptor = paymentMethodsRepository.getPaymentMethodDescriptors()
                    .mapLatest { descriptors ->
                        descriptors.first { descriptor ->
                            descriptor.config.type ==
                                paymentMethodRepository.getPaymentMethod().paymentMethodType
                        }
                    }.first()
                val additionalInfo = when (val resolver = descriptor.additionalInfoResolver) {
                    is RetailOutletsCheckoutAdditionalInfoResolver -> {
                        resolver.retailerName = retailerOutletsRepository
                            .getSelectedRetailOutlet()?.name
                        resolver.resolve(clientTokenData)
                    }
                    else -> descriptor.additionalInfoResolver?.resolve(clientTokenData)
                }
                response.toPaymentResult(additionalInfo)
            }
            else -> response.toPaymentResult()
        }
    }
}
