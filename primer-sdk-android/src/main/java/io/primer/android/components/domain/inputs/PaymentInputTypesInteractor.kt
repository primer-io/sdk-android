package io.primer.android.components.domain.inputs

import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.components.ui.widgets.elements.PrimerInputElementType
import io.primer.android.data.configuration.model.CheckoutModuleType
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.PaymentMethodType

internal class PaymentInputTypesInteractor(
    private val checkoutModuleRepository: CheckoutModuleRepository,
    private val eventDispatcher: EventDispatcher,
    private val logger: Logger,
) {
    // MOVE TO FACTORY
    fun execute(paymentMethodType: PaymentMethodType): List<PrimerInputElementType>? {
        try {
            return when (paymentMethodType) {
                PaymentMethodType.PAYMENT_CARD -> {
                    val postalCode =
                        checkoutModuleRepository.getCheckoutModuleOptions(
                            CheckoutModuleType.BILLING_ADDRESS
                        ).let {
                            if (it?.get("all") ?: it?.get("postalCode") == true)
                                PrimerInputElementType.POSTAL_CODE else null
                        }

                    val cardName =
                        checkoutModuleRepository.getCheckoutModuleOptions(
                            CheckoutModuleType.CARD_INFORMATION
                        ).let {
                            if (it?.get("all") ?: it?.get("cardHolderName") != false)
                                PrimerInputElementType.CARDHOLDER_NAME else null
                        }

                    listOf(
                        PrimerInputElementType.CARD_NUMBER,
                        PrimerInputElementType.EXPIRY_DATE,
                        PrimerInputElementType.CVV
                    )
                        .plus(postalCode)
                        .plus(cardName)
                        .filterNotNull()
                }
                else -> emptyList()
            }
        } catch (e: IllegalArgumentException) {
            logger.error(CONFIGURATION_ERROR, e)
            eventDispatcher.dispatchEvent(CheckoutEvent.ApiError(APIError(CONFIGURATION_ERROR)))
            return null
        }
    }

    private companion object {
        const val CONFIGURATION_ERROR =
            "Failed to initialise due to a configuration missing. Please ensure" +
                "that you have called PrimerHeadlessUniversalCheckout start method" +
                " and you have received onClientSessionSetupSuccessfully callback before" +
                " calling this method."
    }
}
