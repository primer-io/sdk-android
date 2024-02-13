package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.data.settings.PrimerPaymentHandling
import org.json.JSONObject

internal data class AnalyticsNetworkConnectivityEventRequest(
    override val device: DeviceData,
    override val properties: NetworkTypeProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType,
    override val sdkPaymentHandling: PrimerPaymentHandling,
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
        val serializer = JSONObjectSerializer<AnalyticsNetworkConnectivityEventRequest> { t ->
            baseSerializer.serialize(t).apply {
                put(
                    PROPERTIES_FIELD,
                    JSONSerializationUtils.getJsonObjectSerializer<NetworkTypeProperties>()
                        .serialize(t.properties)
                )
            }
        }

        @JvmField
        val deserializer = JSONObjectDeserializer<AnalyticsNetworkConnectivityEventRequest> { t ->
            AnalyticsNetworkConnectivityEventRequest(
                JSONSerializationUtils.getJsonObjectDeserializer<DeviceData>().deserialize(
                    t.getJSONObject(DEVICE_FIELD)
                ),
                JSONSerializationUtils.getJsonObjectDeserializer<NetworkTypeProperties>()
                    .deserialize(
                        t.getJSONObject(PROPERTIES_FIELD)
                    ),
                t.getString(APP_IDENTIFIER_FIELD),
                t.getString(SDK_SESSION_ID_FIELD),
                SdkIntegrationType.valueOf(t.getString(SDK_INTEGRATION_TYPE_FIELD)),
                PrimerPaymentHandling.valueOf(t.getString(SDK_PAYMENT_HANDLING_FIELD)),
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

internal data class NetworkTypeProperties(
    val networkType: NetworkType
) : BaseAnalyticsProperties() {

    companion object {

        private const val NETWORK_TYPE_FIELD = "networkType"

        @JvmField
        val serializer = JSONObjectSerializer<NetworkTypeProperties> { t ->
            JSONObject().apply {
                put(NETWORK_TYPE_FIELD, t.networkType.name)
            }
        }

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            NetworkTypeProperties(NetworkType.valueOf(t.getString(NETWORK_TYPE_FIELD)))
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
