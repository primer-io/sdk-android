package io.primer.android.googlepay.implementation.clientSessionActions.presentation.mapper

import com.google.android.gms.wallet.PaymentData
import io.primer.android.clientSessionActions.domain.models.ActionUpdateBillingAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateEmailAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateMobileNumberParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingOptionIdParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import org.json.JSONObject

private const val PAYMENT_METHOD_DATA_KEY = "paymentMethodData"
private const val INFO_KEY = "info"
private const val BILLING_ADDRESS_KEY = "billingAddress"
private const val NAME_FIELD = "name"
private const val ADDRESS_1_FIELD = "address1"
private const val ADDRESS_2_FIELD = "address2"
private const val ADDRESS_3_FIELD = "address3"
private const val LOCALITY_FIELD = "locality"
private const val POSTAL_CODE_FIELD = "postalCode"
private const val COUNTRY_CODE_FIELD = "countryCode"
private const val ADMINISTRATIVE_AREA_FIELD = "administrativeArea"

private const val SHIPPING_OPTION_DATA_KEY = "shippingOptionData"
private const val ID_KEY = "id"

private const val SHIPPING_ADDRESS_KEY = "shippingAddress"
private const val PHONE_NUMBER_KEY = "phoneNumber"

private const val EMAIL_KEY = "email"

internal fun PaymentData?.mapToClientSessionUpdateParams():
    ActionUpdateBillingAddressParams? {
    return this?.toJson()?.let { paymentInformation ->
        val paymentMethodData =
            JSONObject(paymentInformation).getJSONObject(PAYMENT_METHOD_DATA_KEY)
        val userInfo = paymentMethodData.getJSONObject(INFO_KEY)
        val billingAddress = userInfo.optJSONObject(BILLING_ADDRESS_KEY)
        return billingAddress?.let {
            val name = splitName(billingAddress.optString(NAME_FIELD))
            ActionUpdateBillingAddressParams(
                name.first,
                name.second,
                billingAddress.optString(ADDRESS_1_FIELD),
                billingAddress.optString(ADDRESS_2_FIELD),
                billingAddress.optString(LOCALITY_FIELD),
                billingAddress.optString(POSTAL_CODE_FIELD),
                billingAddress.optString(COUNTRY_CODE_FIELD),
                billingAddress.optString(ADMINISTRATIVE_AREA_FIELD)
            )
        }
    }
}

internal fun PaymentData?.mapToShippingOptionIdParams(): ActionUpdateShippingOptionIdParams? {
    return this?.toJson()?.let { paymentData ->
        JSONObject(paymentData).optJSONObject(SHIPPING_OPTION_DATA_KEY)?.optNullableString(ID_KEY)
            ?.let { shippingOptionId ->
                ActionUpdateShippingOptionIdParams(id = shippingOptionId)
            }
    }
}

internal fun PaymentData?.mapToMobileNumberParams(): ActionUpdateMobileNumberParams? {
    return this?.toJson()?.let { paymentData ->
        JSONObject(paymentData).optJSONObject(SHIPPING_ADDRESS_KEY)?.optNullableString(PHONE_NUMBER_KEY)
            ?.let { mobileNumber ->
                ActionUpdateMobileNumberParams(mobileNumber = mobileNumber)
            }
    }
}

internal fun PaymentData?.mapToShippingAddressParams(): ActionUpdateShippingAddressParams? {
    return this?.toJson()?.let {
        val address = JSONObject(it).optJSONObject(SHIPPING_ADDRESS_KEY)
        address?.let {
            val name = splitName(address.optString(NAME_FIELD))
            ActionUpdateShippingAddressParams(
                firstName = name.first,
                lastName = name.second,
                addressLine1 = address.optString(ADDRESS_1_FIELD),
                addressLine2 = address.optString(ADDRESS_2_FIELD),
                addressLine3 = address.optString(ADDRESS_3_FIELD),
                city = address.optString(LOCALITY_FIELD),
                postalCode = address.optString(POSTAL_CODE_FIELD),
                countryCode = address.optString(COUNTRY_CODE_FIELD),
                state = address.optString(ADMINISTRATIVE_AREA_FIELD)
            )
        }
    }
}

internal fun PaymentData?.mapToEmailAddressParams(): ActionUpdateEmailAddressParams? {
    return this?.toJson()?.let { paymentData ->
        JSONObject(paymentData).optNullableString(EMAIL_KEY)
            ?.let { email ->
                ActionUpdateEmailAddressParams(email = email)
            }
    }
}

private fun splitName(name: String): Pair<String?, String?> {
    val split = name.split(" ")
    val firstName = split.firstOrNull()
    val lastName = split.filter { it != firstName }.joinToString(" ")
    return firstName to lastName
}

internal fun PaymentData?.mapToMultipleActionUpdateParams(): MultipleActionUpdateParams? {
    val actions = listOfNotNull(
        mapToClientSessionUpdateParams(),
        mapToMobileNumberParams(),
        mapToShippingAddressParams(),
        mapToEmailAddressParams()
    )
    return if (actions.isEmpty()) {
        null
    } else {
        MultipleActionUpdateParams(actions)
    }
}
