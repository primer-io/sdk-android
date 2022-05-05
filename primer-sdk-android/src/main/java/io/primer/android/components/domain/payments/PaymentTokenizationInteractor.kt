package io.primer.android.components.domain.payments

import io.primer.android.components.domain.core.models.card.CardInputData
import io.primer.android.components.domain.exception.InvalidTokenizationDataException
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.card.CreditCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class PaymentTokenizationInteractor(
    private val paymentMethodsRepository: PaymentMethodsRepository,
    private val eventDispatcher: EventDispatcher,
    private val errorEventResolver: BaseErrorEventResolver,
    private val logger: Logger
) : BaseInteractor<PaymentMethodDescriptor, PaymentTokenizationDescriptorParams>() {

    override fun execute(params: PaymentTokenizationDescriptorParams):
        Flow<PaymentMethodDescriptor> {
        return paymentMethodsRepository.getPaymentMethodDescriptors()
            .onStart {
                eventDispatcher.dispatchEvent(CheckoutEvent.PreparationStarted)
            }
            .mapLatest {
                val descriptor = it.first { it.config.type == params.paymentMethodType }
                when {
                    descriptor is CreditCard && params.inputData is CardInputData -> {
                        params.inputData.setTokenizableValues(descriptor)
                    }
                    else -> throw InvalidTokenizationDataException(
                        params.paymentMethodType,
                        params.inputData::class
                    )
                }
            }.catch {
                logger.error(CONFIGURATION_ERROR, it)
                errorEventResolver.resolve(it, ErrorMapperType.HUC)
            }
    }

    private companion object {
        const val CONFIGURATION_ERROR =
            "Failed to initialise due to a configuration missing. Please ensure" +
                " that you have called PrimerHeadlessUniversalCheckout start method" +
                " and you have received onClientSessionSetupSuccessfully callback before" +
                " calling this method. Please ensure the" +
                " requested payment method has been configured in Primer's dashboard."
    }
}
