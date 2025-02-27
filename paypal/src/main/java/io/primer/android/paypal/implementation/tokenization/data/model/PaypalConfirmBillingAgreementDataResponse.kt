package io.primer.android.paypal.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalConfirmBillingAgreement
import org.json.JSONObject

internal data class PaypalConfirmBillingAgreementDataResponse(
    val billingAgreementId: String,
    val externalPayerInfo: PaypalExternalPayerInfo,
    val shippingAddress: PaypalShippingAddressDataResponse?,
) : JSONDeserializable {
    companion object {
        private const val BILLING_AGREEMENT_FIELD = "billingAgreementId"
        private const val EXTERNAL_PAYER_INFO_FIELD = "externalPayerInfo"
        private const val SHIPPING_ADDRESS_FIELD = "shippingAddress"

        @JvmField
        val deserializer =
            JSONObjectDeserializer<PaypalConfirmBillingAgreementDataResponse> { t ->
                PaypalConfirmBillingAgreementDataResponse(
                    t.getString(BILLING_AGREEMENT_FIELD),
                    t.getJSONObject(EXTERNAL_PAYER_INFO_FIELD).let {
                        JSONSerializationUtils
                            .getJsonObjectDeserializer<PaypalExternalPayerInfo>()
                            .deserialize(it)
                    },
                    t.optJSONObject(SHIPPING_ADDRESS_FIELD)?.let {
                        JSONSerializationUtils
                            .getJsonObjectDeserializer<PaypalShippingAddressDataResponse>()
                            .deserialize(it)
                    },
                )
            }
    }
}

internal data class PaypalShippingAddressDataResponse(
    val firstName: String?,
    val lastName: String?,
    val addressLine1: String?,
    val addressLine2: String?,
    val city: String?,
    val state: String?,
    val countryCode: String?,
    val postalCode: String?,
) : JSONObjectSerializable, JSONDeserializable {
    companion object {
        private const val FIRST_NAME_FIELD = "firstName"
        private const val LAST_NAME_FIELD = "lastName"
        private const val ADDRESS_LINE_1_FIELD = "addressLine1"
        private const val ADDRESS_LINE_2_FIELD = "addressLine2"
        private const val CITY_FIELD = "city"
        private const val STATE_FIELD = "state"
        private const val COUNTRY_CODE_FIELD = "countryCode"
        private const val POSTAL_CODE_FIELD = "postalCode"

        @JvmField
        val serializer =
            JSONObjectSerializer<PaypalShippingAddressDataResponse> { t ->
                JSONObject().apply {
                    putOpt(FIRST_NAME_FIELD, t.firstName)
                    putOpt(LAST_NAME_FIELD, t.lastName)
                    putOpt(ADDRESS_LINE_1_FIELD, t.addressLine1)
                    putOpt(ADDRESS_LINE_2_FIELD, t.addressLine2)
                    putOpt(CITY_FIELD, t.city)
                    putOpt(STATE_FIELD, t.state)
                    putOpt(COUNTRY_CODE_FIELD, t.countryCode)
                    putOpt(POSTAL_CODE_FIELD, t.postalCode)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                PaypalShippingAddressDataResponse(
                    t.optNullableString(FIRST_NAME_FIELD),
                    t.optNullableString(LAST_NAME_FIELD),
                    t.optNullableString(ADDRESS_LINE_1_FIELD),
                    t.optNullableString(ADDRESS_LINE_2_FIELD),
                    t.optNullableString(CITY_FIELD),
                    t.optNullableString(STATE_FIELD),
                    t.optNullableString(COUNTRY_CODE_FIELD),
                    t.optNullableString(POSTAL_CODE_FIELD),
                )
            }
    }
}

internal fun PaypalConfirmBillingAgreementDataResponse.toPaypalConfirmBillingAgreement() =
    PaypalConfirmBillingAgreement(
        billingAgreementId,
        externalPayerInfo,
        shippingAddress,
    )
