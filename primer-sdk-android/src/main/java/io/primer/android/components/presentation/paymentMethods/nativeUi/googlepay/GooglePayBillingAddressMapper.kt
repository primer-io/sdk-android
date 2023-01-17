package io.primer.android.components.presentation.paymentMethods.nativeUi.googlepay

import com.google.android.gms.wallet.PaymentData
import io.primer.android.domain.action.models.ActionUpdateBillingAddressParams
import org.json.JSONObject

internal class GooglePayBillingAddressMapper {

    fun mapToClientSessionUpdateParams(paymentData: PaymentData?):
        ActionUpdateBillingAddressParams? {
        return paymentData?.toJson()?.let { paymentInformation ->
            val paymentMethodData =
                JSONObject(paymentInformation).getJSONObject(PAYMENT_METHOD_DATA_KEY)
            val userInfo = paymentMethodData.getJSONObject(INFO_KEY)
            val billingAddress = userInfo.optJSONObject(BILLING_ADDRESS_KEY)
            return billingAddress?.let {
                val name = billingAddress.optString(NAME_FIELD).split(" ")
                val firstName = name.firstOrNull()
                val lastName = name.filter { it != firstName }.joinToString(" ")
                ActionUpdateBillingAddressParams(
                    firstName,
                    lastName,
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

    private companion object {
        const val PAYMENT_METHOD_DATA_KEY = "paymentMethodData"
        const val INFO_KEY = "info"
        const val BILLING_ADDRESS_KEY = "billingAddress"
        const val NAME_FIELD = "name"
        const val ADDRESS_1_FIELD = "address1"
        const val ADDRESS_2_FIELD = "address2"
        const val LOCALITY_FIELD = "locality"
        const val POSTAL_CODE_FIELD = "postalCode"
        const val COUNTRY_CODE_FIELD = "countryCode"
        const val ADMINISTRATIVE_AREA_FIELD = "administrativeArea"
    }
}
