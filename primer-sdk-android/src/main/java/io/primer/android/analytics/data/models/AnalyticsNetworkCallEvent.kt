package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.core.serialization.json.extensions.optNullableInt
import io.primer.android.core.serialization.json.extensions.optNullableString
import org.json.JSONObject

internal data class AnalyticsNetworkCallEvent(
    override val device: DeviceData,
    override val properties: NetworkCallProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    override val eventType: AnalyticsEventType = AnalyticsEventType.NETWORK_CALL_EVENT,
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsNetworkCallEvent = copy(
        analyticsUrl = newAnalyticsUrl
    )

    companion object {

        @JvmField
        val serializer = object : JSONSerializer<AnalyticsNetworkCallEvent> {
            override fun serialize(t: AnalyticsNetworkCallEvent): JSONObject {
                return baseSerializer.serialize(t).apply {
                    put(
                        PROPERTIES_FIELD,
                        JSONSerializationUtils.getSerializer<NetworkCallProperties>()
                            .serialize(t.properties)
                    )
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<AnalyticsNetworkCallEvent> {
            override fun deserialize(t: JSONObject): AnalyticsNetworkCallEvent {
                return AnalyticsNetworkCallEvent(
                    JSONSerializationUtils.getDeserializer<DeviceData>().deserialize(
                        t.getJSONObject(DEVICE_FIELD)
                    ),
                    JSONSerializationUtils.getDeserializer<NetworkCallProperties>().deserialize(
                        t.getJSONObject(PROPERTIES_FIELD)
                    ),
                    t.getString(APP_IDENTIFIER_FIELD),
                    t.getString(SDK_SESSION_ID_FIELD),
                    SdkIntegrationType.valueOf(t.getString(SDK_INTEGRATION_TYPE_FIELD)),
                    t.getString(CHECKOUT_SESSION_ID_FIELD),
                    t.optNullableString(CLIENT_SESSION_ID_FIELD),
                    t.optNullableString(ORDER_ID_FIELD),
                    t.optNullableString(PRIMER_ACCOUNT_ID_FIELD),
                    t.optNullableString(ANALYTICS_URL_FIELD),
                )
            }
        }
    }
}

internal data class NetworkCallProperties(
    val networkCallType: NetworkCallType,
    val id: String,
    val url: String,
    val method: String,
    val responseCode: Int? = null,
    val errorBody: String? = null,
) : BaseAnalyticsProperties() {

    companion object {

        private const val NETWORK_CALL_TYPE_FIELD = "networkCallType"
        private const val ID_FIELD = "id"
        private const val URL_FIELD = "url"
        private const val METHOD_FIELD = "method"
        private const val RESPONSE_CODE_FIELD = "responseCode"
        private const val ERROR_BODY_FIELD = "errorBody"

        @JvmField
        val serializer = object : JSONSerializer<NetworkCallProperties> {
            override fun serialize(t: NetworkCallProperties): JSONObject {
                return JSONObject().apply {
                    put(NETWORK_CALL_TYPE_FIELD, t.networkCallType.name)
                    put(ID_FIELD, t.id)
                    put(URL_FIELD, t.url)
                    put(METHOD_FIELD, t.method)
                    putOpt(RESPONSE_CODE_FIELD, t.responseCode)
                    putOpt(ERROR_BODY_FIELD, t.errorBody)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<NetworkCallProperties> {
            override fun deserialize(t: JSONObject): NetworkCallProperties {
                return NetworkCallProperties(
                    NetworkCallType.valueOf(t.getString(NETWORK_CALL_TYPE_FIELD)),
                    t.getString(ID_FIELD),
                    t.getString(URL_FIELD),
                    t.getString(METHOD_FIELD),
                    t.optNullableInt(RESPONSE_CODE_FIELD),
                    t.optNullableString(ERROR_BODY_FIELD)
                )
            }
        }
    }
}

internal enum class NetworkCallType {
    REQUEST_START,
    REQUEST_END
}
