package io.primer.android.analytics.data.models

import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableInt
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import org.json.JSONObject

internal data class AnalyticsNetworkCallEvent(
    override val device: DeviceData,
    override val properties: NetworkCallProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType?,
    override val sdkPaymentHandling: String?,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    override val eventType: AnalyticsEventType = AnalyticsEventType.NETWORK_CALL_EVENT,
    override val createdAt: Long = System.currentTimeMillis()
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsNetworkCallEvent = copy(
        analyticsUrl = newAnalyticsUrl
    )

    companion object {

        @JvmField
        val serializer = JSONObjectSerializer<AnalyticsNetworkCallEvent> { t ->
            baseSerializer.serialize(t).apply {
                put(
                    PROPERTIES_FIELD,
                    JSONSerializationUtils.getJsonObjectSerializer<NetworkCallProperties>()
                        .serialize(t.properties)
                )
            }
        }

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            AnalyticsNetworkCallEvent(
                device = JSONSerializationUtils.getJsonObjectDeserializer<DeviceData>().deserialize(
                    t.getJSONObject(DEVICE_FIELD)
                ),
                properties = JSONSerializationUtils.getJsonObjectDeserializer<NetworkCallProperties>()
                    .deserialize(
                        t.getJSONObject(PROPERTIES_FIELD)
                    ),
                appIdentifier = t.getString(APP_IDENTIFIER_FIELD),
                sdkSessionId = t.getString(SDK_SESSION_ID_FIELD),
                sdkIntegrationType = t.optNullableString(SDK_INTEGRATION_TYPE_FIELD)
                    ?.let { SdkIntegrationType.valueOf(it) },
                sdkPaymentHandling = t.optNullableString(SDK_PAYMENT_HANDLING_FIELD),
                checkoutSessionId = t.getString(CHECKOUT_SESSION_ID_FIELD),
                clientSessionId = t.optNullableString(CLIENT_SESSION_ID_FIELD),
                orderId = t.optNullableString(ORDER_ID_FIELD),
                primerAccountId = t.optNullableString(PRIMER_ACCOUNT_ID_FIELD),
                analyticsUrl = t.optNullableString(ANALYTICS_URL_FIELD),
                createdAt = t.getLong(CREATED_AT_FIELD)
            )
        }
    }
}

internal data class NetworkCallProperties(
    val callType: NetworkCallType,
    val id: String,
    val url: String,
    val method: String,
    val responseCode: Int? = null,
    val errorBody: String? = null,
    val duration: Long? = null
) : BaseAnalyticsProperties() {

    companion object {

        private const val NETWORK_CALL_TYPE_FIELD = "callType"
        private const val ID_FIELD = "id"
        private const val URL_FIELD = "url"
        private const val METHOD_FIELD = "method"
        private const val RESPONSE_CODE_FIELD = "responseCode"
        private const val ERROR_BODY_FIELD = "errorBody"
        private const val DURATION = "duration"

        @JvmField
        val serializer = JSONObjectSerializer<NetworkCallProperties> { t ->
            JSONObject().apply {
                put(NETWORK_CALL_TYPE_FIELD, t.callType.name)
                put(ID_FIELD, t.id)
                put(URL_FIELD, t.url)
                put(METHOD_FIELD, t.method)
                putOpt(RESPONSE_CODE_FIELD, t.responseCode)
                putOpt(ERROR_BODY_FIELD, t.errorBody)
                putOpt(DURATION, t.duration)
            }
        }

        @JvmField
        val deserializer = JSONObjectDeserializer<NetworkCallProperties> { t ->
            NetworkCallProperties(
                NetworkCallType.valueOf(t.getString(NETWORK_CALL_TYPE_FIELD)),
                t.getString(ID_FIELD),
                t.getString(URL_FIELD),
                t.getString(METHOD_FIELD),
                t.optNullableInt(RESPONSE_CODE_FIELD),
                t.optNullableString(ERROR_BODY_FIELD),
                t.optNullableInt(DURATION)?.toLong()
            )
        }
    }
}

internal enum class NetworkCallType {
    REQUEST_START,
    REQUEST_END
}
