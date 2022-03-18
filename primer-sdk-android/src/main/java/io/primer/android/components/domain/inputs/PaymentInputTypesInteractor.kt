package io.primer.android.components.domain.inputs

import io.primer.android.components.domain.payments.repository.CheckoutModuleRepository
import io.primer.android.model.dto.PrimerInputFieldType
import io.primer.android.model.dto.needAdd
import io.primer.android.model.dto.via
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
    fun execute(paymentMethodType: PaymentMethodType): List<PrimerInputFieldType>? {
        try {
            return when (paymentMethodType) {
                PaymentMethodType.PAYMENT_CARD -> {
                    val billingAddressesFields =
                        checkoutModuleRepository.getCheckoutModuleOptions(
                            CheckoutModuleType.BILLING_ADDRESS
                        ).let { configs ->
                            val addressFields = mutableListOf<PrimerInputFieldType>()
                            configs?.needAdd(PrimerInputFieldType.POSTAL_CODE)?.let { field ->
                                addressFields.add(field)
                            }
                            configs?.needAdd(PrimerInputFieldType.COUNTRY_CODE)?.let { field ->
                                addressFields.add(field)
                            }
                            configs?.needAdd(PrimerInputFieldType.CITY)?.let { field ->
                                addressFields.add(field)
                            }
                            configs?.needAdd(PrimerInputFieldType.STATE)?.let { field ->
                                addressFields.add(field)
                            }
                            configs?.needAdd(PrimerInputFieldType.ADDRESS_LINE_1)?.let { field ->
                                addressFields.add(field)
                            }
                            configs?.needAdd(PrimerInputFieldType.ADDRESS_LINE_2)?.let { field ->
                                addressFields.add(field)
                            }
                            configs?.needAdd(PrimerInputFieldType.PHONE_NUMBER)?.let { field ->
                                addressFields.add(field)
                            }
                            configs?.needAdd(PrimerInputFieldType.FIRST_NAME)?.let { field ->
                                addressFields.add(field)
                            }
                            configs?.needAdd(PrimerInputFieldType.LAST_NAME)?.let { field ->
                                addressFields.add(field)
                            }
                            addressFields
                        }

                    val cardName =
                        checkoutModuleRepository.getCheckoutModuleOptions(
                            CheckoutModuleType.CARD_INFORMATION
                        ).let { configs ->
                            if (configs?.via(PrimerInputFieldType.ALL)
                                ?: configs?.via(PrimerInputFieldType.CARDHOLDER_NAME) != false)
                                PrimerInputFieldType.CARDHOLDER_NAME else null
                        }

                    listOf(
                        PrimerInputFieldType.CARD_NUMBER,
                        PrimerInputFieldType.EXPIRY_DATE,
                        PrimerInputFieldType.CVV
                    )
                        .plus(cardName)
                        .plus(billingAddressesFields)
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
