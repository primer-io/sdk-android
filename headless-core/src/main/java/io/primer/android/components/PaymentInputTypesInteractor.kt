package io.primer.android.components

import io.primer.android.checkoutModules.domain.repository.CheckoutModuleRepository
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.cardShared.extension.isCardHolderNameEnabled

internal class PaymentInputTypesInteractor(
    private val checkoutModuleRepository: CheckoutModuleRepository,
    private val logReporter: LogReporter,
) {
    // MOVE TO FACTORY
    fun execute(paymentMethodType: String): List<PrimerInputElementType> {
        try {
            return when (paymentMethodType) {
                PaymentMethodType.PAYMENT_CARD.name -> {
                    val billingAddressesFields =
                        checkoutModuleRepository.getBillingAddress().let {
                            val addressFields = mutableListOf<PrimerInputElementType>()
//                            configs?.needAdd(PrimerInputElementType.POSTAL_CODE)?.let { field ->
//                                addressFields.add(field)
//                            }
//                            configs?.needAdd(PrimerInputElementType.COUNTRY_CODE)?.let { field ->
//                                addressFields.add(field)
//                            }
//                            configs?.needAdd(PrimerInputElementType.CITY)?.let { field ->
//                                addressFields.add(field)
//                            }
//                            configs?.needAdd(PrimerInputElementType.STATE)?.let { field ->
//                                addressFields.add(field)
//                            }
//                            configs?.needAdd(PrimerInputElementType.ADDRESS_LINE_1)?.let { field ->
//                                addressFields.add(field)
//                            }
//                            configs?.needAdd(PrimerInputElementType.ADDRESS_LINE_2)?.let { field ->
//                                addressFields.add(field)
//                            }
//                            configs?.needAdd(PrimerInputElementType.PHONE_NUMBER)?.let { field ->
//                                addressFields.add(field)
//                            }
//                            configs?.needAdd(PrimerInputElementType.FIRST_NAME)?.let { field ->
//                                addressFields.add(field)
//                            }
//                            configs?.needAdd(PrimerInputElementType.LAST_NAME)?.let { field ->
//                                addressFields.add(field)
//                            }
                            addressFields
                        }

                    val cardHolderName =
                        if (checkoutModuleRepository.getCardInformation().isCardHolderNameEnabled()) {
                            PrimerInputElementType.CARDHOLDER_NAME
                        } else {
                            null
                        }

                    listOf(
                        PrimerInputElementType.CARD_NUMBER,
                        PrimerInputElementType.EXPIRY_DATE,
                        PrimerInputElementType.CVV,
                    )
                        .plus(cardHolderName)
                        .plus(billingAddressesFields)
                        .filterNotNull()
                }
                PaymentMethodType.ADYEN_BANCONTACT_CARD.name -> {
                    listOf(
                        PrimerInputElementType.CARD_NUMBER,
                        PrimerInputElementType.EXPIRY_DATE,
                        PrimerInputElementType.CARDHOLDER_NAME,
                    )
                }
                PaymentMethodType.ADYEN_MBWAY.name,
                PaymentMethodType.XENDIT_OVO.name,
                -> listOf(PrimerInputElementType.PHONE_NUMBER)
                PaymentMethodType.ADYEN_BLIK.name -> listOf(PrimerInputElementType.OTP_CODE)
                else -> emptyList()
            }
        } catch (e: IllegalArgumentException) {
            logReporter.error(CONFIGURATION_ERROR, throwable = e)
            return emptyList()
        }
    }

    private companion object {
        const val CONFIGURATION_ERROR =
            """
                Failed to initialise due to a configuration missing. Please ensure 
                that you have called PrimerHeadlessUniversalCheckout start method
                and you have received onAvailablePaymentMethodsLoaded callback before
                calling this method.
            """
    }
}
