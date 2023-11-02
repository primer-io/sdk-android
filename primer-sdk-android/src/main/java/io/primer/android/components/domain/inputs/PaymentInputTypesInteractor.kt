package io.primer.android.components.domain.inputs

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.inputs.models.via
import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.data.configuration.models.CheckoutModuleType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.core.logging.internal.LogReporter

internal class PaymentInputTypesInteractor(
    private val checkoutModuleRepository: CheckoutModuleRepository,
    private val errorEventResolver: BaseErrorEventResolver,
    private val logReporter: LogReporter
) {
    // MOVE TO FACTORY
    fun execute(paymentMethodType: String): List<PrimerInputElementType> {
        try {
            return when (paymentMethodType) {
                PaymentMethodType.PAYMENT_CARD.name -> {
                    val billingAddressesFields =
                        checkoutModuleRepository.getCheckoutModuleOptions(
                            CheckoutModuleType.BILLING_ADDRESS
                        ).let { configs ->
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

                    val cardName =
                        checkoutModuleRepository.getCheckoutModuleOptions(
                            CheckoutModuleType.CARD_INFORMATION
                        ).let { configs ->
                            val containsCardholders =
                                configs?.via(PrimerInputElementType.ALL) ?: configs?.via(
                                    PrimerInputElementType.CARDHOLDER_NAME
                                )
                            if (containsCardholders == null || containsCardholders) {
                                PrimerInputElementType.CARDHOLDER_NAME
                            } else { null }
                        }

                    listOf(
                        PrimerInputElementType.CARD_NUMBER,
                        PrimerInputElementType.EXPIRY_DATE,
                        PrimerInputElementType.CVV
                    )
                        .plus(cardName)
                        .plus(billingAddressesFields)
                        .filterNotNull()
                }
                PaymentMethodType.ADYEN_BANCONTACT_CARD.name -> {
                    listOf(
                        PrimerInputElementType.CARD_NUMBER,
                        PrimerInputElementType.EXPIRY_DATE,
                        PrimerInputElementType.CARDHOLDER_NAME
                    )
                }
                PaymentMethodType.ADYEN_MBWAY.name,
                PaymentMethodType.XENDIT_OVO.name -> listOf(PrimerInputElementType.PHONE_NUMBER)
                PaymentMethodType.ADYEN_BLIK.name -> listOf(PrimerInputElementType.OTP_CODE)
                else -> emptyList()
            }
        } catch (e: IllegalArgumentException) {
            logReporter.error(CONFIGURATION_ERROR, throwable = e)
            errorEventResolver.resolve(e, ErrorMapperType.HUC)
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
