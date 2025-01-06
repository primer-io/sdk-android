package io.primer.android.paymentMethods.core.domain

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.core.domain.BaseFlowInteractor
import io.primer.android.core.domain.None
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.exception.MissingPaymentMethodException
import io.primer.android.paymentMethods.core.domain.events.PrimerEvent
import io.primer.android.paymentMethods.core.domain.repository.PrimerHeadlessRepository
import io.primer.android.payments.core.helpers.CheckoutExitHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge

internal class PrimerEventsInteractor(
    private val headlessRepository: PrimerHeadlessRepository,
    private val exitHandler: CheckoutExitHandler,
    private val config: PrimerConfig,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseFlowInteractor<PrimerEvent, None>() {
    override fun execute(params: None): Flow<PrimerEvent> {
        return merge(
            headlessRepository.events,
            exitHandler.checkoutExited.mapLatest {
                PrimerEvent.Dismiss
            },
        ).map { event ->
            when (event) {
                is PrimerEvent.AvailablePaymentMethodsLoaded -> {
                    // we get the descriptor we need for standalone PM
                    if (config.isStandalonePaymentMethod) {
                        val paymentMethodType = requireNotNull(config.intent.paymentMethodType)
                        val paymentMethod =
                            event.paymentMethodsHolder.paymentMethods.find { paymentMethod ->
                                paymentMethod.paymentMethodType == paymentMethodType
                            }
                        paymentMethod?.let {
                            PrimerEvent.AvailablePaymentMethodsLoaded(
                                PaymentMethodsHolder(
                                    paymentMethods = event.paymentMethodsHolder.paymentMethods,
                                    selectedPaymentMethod = paymentMethod,
                                ),
                            )
                        } ?: throw MissingPaymentMethodException(paymentMethodType)
                    } else {
                        PrimerEvent.AvailablePaymentMethodsLoaded(
                            PaymentMethodsHolder(event.paymentMethodsHolder.paymentMethods),
                        )
                    }
                }

                else -> event
            }
        }
    }

    data class PaymentMethodsHolder(
        val paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>,
        val selectedPaymentMethod: PrimerHeadlessUniversalCheckoutPaymentMethod? = null,
    )
}
