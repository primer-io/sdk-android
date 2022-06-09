package io.primer.android.components.domain.inputs

import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.components.ui.widgets.elements.PrimerInputElementType
import io.primer.android.data.configuration.models.CheckoutModuleType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.logging.Logger
import io.primer.android.domain.error.ErrorMapperType

internal class PaymentInputTypesInteractor(
    private val checkoutModuleRepository: CheckoutModuleRepository,
    private val errorEventResolver: BaseErrorEventResolver,
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
            errorEventResolver.resolve(e, ErrorMapperType.HUC)
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
