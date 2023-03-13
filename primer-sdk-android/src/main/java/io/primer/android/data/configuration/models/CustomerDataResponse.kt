package io.primer.android.data.configuration.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.domain.action.models.PrimerCustomer
import org.json.JSONObject

internal data class CustomerDataResponse(
    val customerId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val emailAddress: String? = null,
    val homePhone: String? = null,
    val mobileNumber: String? = null,
    val workPhone: String? = null,
    val nationalDocumentId: String? = null,
    val billingAddress: AddressDataResponse? = null,
    val shippingAddress: AddressDataResponse? = null,
) : JSONDeserializable {

    fun toCustomer() = PrimerCustomer(
        emailAddress,
        mobileNumber,
        firstName,
        lastName,
        billingAddress?.toAddress(),
        shippingAddress?.toAddress()
    )

    fun getFullName() = "${firstName.orEmpty()} ${lastName.orEmpty()}"

    companion object {
        private const val CUSTOMER_ID_FIELD = "customerId"
        private const val FIRST_NAME_FIELD = "firstName"
        private const val LAST_NAME_FIELD = "lastName"
        private const val EMAIL_FIELD = "emailAddress"
        private const val HOME_PHONE_FIELD = "homePhone"
        private const val MOBILE_PHONE_FIELD = "mobileNumber"
        private const val WORK_PHONE_FIELD = "workPhone"
        private const val NATIONAL_DOCUMENT_ID_FIELD = "nationalDocumentId"
        private const val BILLING_ADDRESS_FIELD = "billingAddress"
        private const val SHIPPING_ADDRESS_FIELD = "shippingAddress"

        @JvmField
        val deserializer = object : JSONDeserializer<CustomerDataResponse> {

            override fun deserialize(t: JSONObject): CustomerDataResponse {
                return CustomerDataResponse(
                    t.optNullableString(CUSTOMER_ID_FIELD),
                    t.optNullableString(FIRST_NAME_FIELD),
                    t.optNullableString(LAST_NAME_FIELD),
                    t.optNullableString(EMAIL_FIELD),
                    t.optNullableString(HOME_PHONE_FIELD),
                    t.optNullableString(MOBILE_PHONE_FIELD),
                    t.optNullableString(WORK_PHONE_FIELD),
                    t.optNullableString(NATIONAL_DOCUMENT_ID_FIELD),
                    t.optJSONObject(BILLING_ADDRESS_FIELD)?.let {
                        JSONSerializationUtils.getDeserializer<AddressDataResponse>()
                            .deserialize(
                                it
                            )
                    },
                    t.optJSONObject(SHIPPING_ADDRESS_FIELD)?.let {
                        JSONSerializationUtils.getDeserializer<AddressDataResponse>()
                            .deserialize(
                                it
                            )
                    }
                )
            }
        }
    }
}
