package io.primer.android.threeds.domain.validation

import io.primer.android.UXMode
import io.primer.android.threeds.domain.models.ThreeDsConfigParams
import kotlinx.coroutines.flow.flow
import java.lang.IllegalArgumentException
import java.util.Currency

internal class ThreeDsConfigValidator {

    fun validate(threeDsConfigParams: ThreeDsConfigParams) =
        flow {
            val errors = mutableListOf<String>()
            if (threeDsConfigParams.uxMode == UXMode.CHECKOUT && threeDsConfigParams.amount == 0) {
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
                errors.add(USER_DETAILS_MISSING_ERROR)
            }
            if (threeDsConfigParams.customerFirstName.isBlank()) {
                errors.add(USER_DETAILS_FIRST_NAME_MISSING_ERROR)
            }
            if (threeDsConfigParams.customerLastName.isBlank()) {
                errors.add(USER_DETAILS_LAST_NAME_MISSING_ERROR)
            }
            if (threeDsConfigParams.customerEmail.isBlank()) {
                errors.add(USER_DETAILS_EMAIL_MISSING_ERROR)
            }
            if (threeDsConfigParams.city.isBlank()) {
                errors.add(USER_DETAILS_CITY_MISSING_ERROR)
            }
            if (threeDsConfigParams.addressLine1.isBlank()) {
                errors.add(USER_DETAILS_ADDRESS_LINE_1_MISSING_ERROR)
            }
            if (threeDsConfigParams.postalCode.isBlank()) {
                errors.add(USER_DETAILS_POSTAL_CODE_MISSING_ERROR)
            }
            if (threeDsConfigParams.countryCode.isBlank()) {
                errors.add(USER_DETAILS_COUNTRY_CODE_MISSING_ERROR)
            }

            if (errors.isEmpty()) {
                emit(Unit)
            } else {
                throw IllegalArgumentException(getFormattedMessage(errors))
            }
        }

    internal fun getFormattedMessage(errors: List<String>) =
        "$USER_DETAILS_DATA_MISSING_ERROR${errors.joinToString("\n")}"

    internal companion object {

        const val AMOUNT_MISSING_ERROR = "Amount is missing"
        const val CURRENCY_MISSING_ERROR = "Currency is missing"
        const val ORDER_ID_MISSING_ERROR = "Order ID is missing"
        const val USER_DETAILS_MISSING_ERROR = "User details are missing"
        const val USER_DETAILS_FIRST_NAME_MISSING_ERROR = "User details first name is missing"
        const val USER_DETAILS_LAST_NAME_MISSING_ERROR = "User details last name is missing"
        const val USER_DETAILS_EMAIL_MISSING_ERROR = "User details email is missing"
        const val USER_DETAILS_CITY_MISSING_ERROR = "User details city is missing"
        const val USER_DETAILS_ADDRESS_LINE_1_MISSING_ERROR =
            "User details address line 1 is missing"
        const val USER_DETAILS_POSTAL_CODE_MISSING_ERROR = "User details postal code is missing"
        const val USER_DETAILS_COUNTRY_CODE_MISSING_ERROR = "User details country code is missing"
        private const val USER_DETAILS_DATA_MISSING_ERROR = "Data are missing: "
    }
}
