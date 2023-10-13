package io.primer.android.threeds.data.models.auth

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.threeds.domain.models.BaseThreeDsParams
import io.primer.android.threeds.domain.models.ThreeDsCheckoutParams
import io.primer.android.threeds.domain.models.ThreeDsVaultParams
import org.json.JSONObject

private const val SDK_TIMEOUT_IN_SECONDS = 60

internal data class BeginAuthDataRequest(
    val maxProtocolVersion: String,
    val amount: Int? = null,
    val currencyCode: String? = null,
    val orderId: String? = null,
    val customer: ThreeDsCustomerDataRequest? = null,
    val device: SDKAuthDataRequest,
    val billingAddress: Address? = null,
    val shippingAddress: Address? = null
) : JSONObjectSerializable {

    companion object {
        private const val MAX_PROTOCOL_VERSION_FIELD = "maxProtocolVersion"
        private const val AMOUNT_FIELD = "amount"
        private const val DEVICE_FIELD = "device"
        private const val CURRENCY_CODE_FIELD = "currencyCode"
        private const val ORDER_ID_FIELD = "orderId"
        private const val CUSTOMER_FIELD = "customer"
        private const val BILLING_ADDRESS_FIELD = "billingAddress"
        private const val SHIPPING_ADDRESS_FIELD = "shippingAddress"

        @JvmField
        val serializer = object : JSONObjectSerializer<BeginAuthDataRequest> {
            override fun serialize(t: BeginAuthDataRequest): JSONObject {
                return JSONObject().apply {
                    put(MAX_PROTOCOL_VERSION_FIELD, t.maxProtocolVersion)
                    putOpt(AMOUNT_FIELD, t.amount)
                    putOpt(CURRENCY_CODE_FIELD, t.currencyCode)
                    putOpt(ORDER_ID_FIELD, t.orderId)
                    put(
                        DEVICE_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<SDKAuthDataRequest>()
                            .serialize(t.device)
                    )
                    t.customer?.let {
                        put(
                            CUSTOMER_FIELD,
                            JSONSerializationUtils
                                .getJsonObjectSerializer<ThreeDsCustomerDataRequest>()
                                .serialize(it)
                        )
                    }
                    t.billingAddress?.let {
                        put(
                            BILLING_ADDRESS_FIELD,
                            JSONSerializationUtils
                                .getJsonObjectSerializer<Address>()
                                .serialize(it)
                        )
                    }
                    t.shippingAddress?.let {
                        put(
                            SHIPPING_ADDRESS_FIELD,
                            JSONSerializationUtils
                                .getJsonObjectSerializer<Address>()
                                .serialize(it)
                        )
                    }
                }
            }
        }
    }
}

internal fun BaseThreeDsParams.toBeginAuthRequest(): BeginAuthDataRequest {
    return when (this) {
        is ThreeDsCheckoutParams -> BeginAuthDataRequest(
            maxProtocolVersion.versionNumber,
            device = SDKAuthDataRequest(
                sdkAppId,
                sdkTransactionId,
                SDK_TIMEOUT_IN_SECONDS,
                sdkEncData,
                sdkEphemPubKey,
                sdkReferenceNumber
            )
        )
        is ThreeDsVaultParams -> BeginAuthDataRequest(
            maxProtocolVersion.versionNumber,
            amount,
            currency,
            orderId,
            ThreeDsCustomerDataRequest(customerName, customerEmail),
            SDKAuthDataRequest(
                sdkAppId,
                sdkTransactionId,
                SDK_TIMEOUT_IN_SECONDS,
                sdkEncData,
                sdkEphemPubKey,
                sdkReferenceNumber
            ),
            Address(
                addressLine1 = addressLine1,
                city = city,
                postalCode = postalCode,
                countryCode = countryCode
            )
        )
    }
}
