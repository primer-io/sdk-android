package io.primer.android.threeds.domain.validation

import io.primer.android.PaymentMethodIntent
import io.primer.android.threeds.domain.models.ThreeDsConfigParams
import kotlinx.coroutines.flow.flow
import java.lang.IllegalArgumentException
import java.util.Currency

internal class ThreeDsConfigValidator {

    fun validate(threeDsConfigParams: ThreeDsConfigParams) =
        flow {
            val errors = mutableListOf<String>()
            if (threeDsConfigParams.paymentMethodIntent == PaymentMethodIntent.CHECKOUT &&
                threeDsConfigParams.amount == 0
            ) {
                errors.add(AMOUNT_MISSING_ERROR)
            }
            if (threeDsConfigParams.currency.isBlank()) {
                errors.add(CURRENCY_MISSING_ERROR)
            } else {
                try {
                    val currency = Currency.getInstance(threeDsConfigParams.currency)
                    if (currency == null) errors.add(CURRENCY_MISSING_ERROR)
                } catch (ignored: Exception) {
                    errors.add(CURRENCY_MISSING_ERROR)
                }
            }
            if (threeDsConfigParams.orderId.isBlank()) {
                errors.add(ORDER_ID_MISSING_ERROR)
            }
            if (threeDsConfigParams.userDetailsAvailable.not()) {
                errors.add(CUSTOMER_DETAILS_MISSING_ERROR)
            }
            if (threeDsConfigParams.customerFirstName.isBlank()) {
                errors.add(CUSTOMER_FIRST_NAME_MISSING_ERROR)
            }
            if (threeDsConfigParams.customerLastName.isBlank()) {
                errors.add(CUSTOMER_LAST_NAME_MISSING_ERROR)
            }
            if (threeDsConfigParams.customerEmail.isBlank()) {
                errors.add(CUSTOMER_EMAIL_MISSING_ERROR)
            }
            if (threeDsConfigParams.city.isBlank()) {
                errors.add(CUSTOMER_CITY_MISSING_ERROR)
            }
            if (threeDsConfigParams.addressLine1.isBlank()) {
                errors.add(CUSTOMER_ADDRESS_LINE_1_MISSING_ERROR)
            }
            if (threeDsConfigParams.postalCode.isBlank()) {
                errors.add(CUSTOMER_POSTAL_CODE_MISSING_ERROR)
            }
            if (threeDsConfigParams.countryCode.isBlank()) {
                errors.add(CUSTOMER_COUNTRY_CODE_MISSING_ERROR)
            }

            if (errors.isEmpty()) {
                emit(Unit)
            } else {
                throw IllegalArgumentException(getFormattedMessage(errors))
            }
        }

    internal fun getFormattedMessage(errors: List<String>) =
        "$CUSTOMER_DATA_MISSING_ERROR${errors.joinToString("\n")}"

    internal companion object {

        const val AMOUNT_MISSING_ERROR = "Amount is missing"
        const val CURRENCY_MISSING_ERROR = "Currency is missing"
        const val ORDER_ID_MISSING_ERROR = "Order ID is missing"
        const val CUSTOMER_DETAILS_MISSING_ERROR = "Customer details are missing"
        const val CUSTOMER_FIRST_NAME_MISSING_ERROR = "Customer first name is missing"
        const val CUSTOMER_LAST_NAME_MISSING_ERROR = "Customer last name is missing"
        const val CUSTOMER_EMAIL_MISSING_ERROR = "Customer email is missing"
        const val CUSTOMER_CITY_MISSING_ERROR = "Customer city is missing"
        const val CUSTOMER_ADDRESS_LINE_1_MISSING_ERROR =
            "Customer address line 1 is missing"
        const val CUSTOMER_POSTAL_CODE_MISSING_ERROR = "Customer postal code is missing"
        const val CUSTOMER_COUNTRY_CODE_MISSING_ERROR = "Customer country code is missing"
        private const val CUSTOMER_DATA_MISSING_ERROR = "Data are missing: "
    }
}
