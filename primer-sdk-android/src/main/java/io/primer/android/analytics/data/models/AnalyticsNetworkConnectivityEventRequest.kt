package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.core.serialization.json.extensions.optNullableString
import org.json.JSONObject

internal data class AnalyticsNetworkConnectivityEventRequest(
    override val device: DeviceData,
    override val properties: NetworkTypeProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    override val eventType: AnalyticsEventType = AnalyticsEventType.NETWORK_CONNECTIVITY_EVENT,
    override val createdAt: Long = System.currentTimeMillis()
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsNetworkConnectivityEventRequest = copy(
        analyticsUrl = newAnalyticsUrl
    )

    companion object {

        @JvmField
        val serializer = object : JSONSerializer<AnalyticsNetworkConnectivityEventRequest> {
            override fun serialize(t: AnalyticsNetworkConnectivityEventRequest): JSONObject {
                return baseSerializer.serialize(t).apply {
                    put(
                        PROPERTIES_FIELD,
                        JSONSerializationUtils.getSerializer<NetworkTypeProperties>()
                            .serialize(t.properties)
                    )
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<AnalyticsNetworkConnectivityEventRequest> {
            override fun deserialize(t: JSONObject): AnalyticsNetworkConnectivityEventRequest {
                return AnalyticsNetworkConnectivityEventRequest(
                    JSONSerializationUtils.getDeserializer<DeviceData>().deserialize(
                        t.getJSONObject(DEVICE_FIELD)
                    ),
                    JSONSerializationUtils.getDeserializer<NetworkTypeProperties>().deserialize(
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
                    createdAt = t.getLong(CREATED_AT_FIELD)
                )
            }
        }
    }
}

internal data class NetworkTypeProperties(
    val networkType: NetworkType,
) : BaseAnalyticsProperties() {

    companion object {

        private const val NETWORK_TYPE_FIELD = "networkType"

        @JvmField
        val serializer = object : JSONSerializer<NetworkTypeProperties> {
            override fun serialize(t: NetworkTypeProperties): JSONObject {
                return JSONObject().apply {
                    put(NETWORK_TYPE_FIELD, t.networkType.name)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<NetworkTypeProperties> {
            override fun deserialize(t: JSONObject): NetworkTypeProperties {
                return NetworkTypeProperties(NetworkType.valueOf(t.getString(NETWORK_TYPE_FIELD)))
            }
        }
    }
}

internal enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    OTHER,
    NONE
}
