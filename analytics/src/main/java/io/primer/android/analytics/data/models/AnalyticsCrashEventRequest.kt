package io.primer.android.analytics.data.models

import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.core.data.serialization.json.extensions.sequence
import org.json.JSONArray
import org.json.JSONObject

internal data class AnalyticsCrashEventRequest(
    override val device: DeviceData,
    override val properties: CrashProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType?,
    override val sdkPaymentHandling: String?,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    override val eventType: AnalyticsEventType = AnalyticsEventType.APP_CRASHED_EVENT,
    override val createdAt: Long = System.currentTimeMillis(),
) : BaseAnalyticsEventRequest() {
    override fun copy(newAnalyticsUrl: String?): AnalyticsCrashEventRequest =
        copy(
            analyticsUrl = newAnalyticsUrl,
        )

    companion object {
        @JvmField
        val serializer =
            JSONObjectSerializer<AnalyticsCrashEventRequest> { t ->
                baseSerializer.serialize(t).apply {
                    put(
                        PROPERTIES_FIELD,
                        JSONSerializationUtils.getJsonObjectSerializer<CrashProperties>()
                            .serialize(t.properties),
                    )
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                AnalyticsCrashEventRequest(
                    device =
                    JSONSerializationUtils.getJsonObjectDeserializer<DeviceData>().deserialize(
                        t.getJSONObject(DEVICE_FIELD),
                    ),
                    properties =
                    JSONSerializationUtils.getJsonObjectDeserializer<CrashProperties>().deserialize(
                        t.getJSONObject(PROPERTIES_FIELD),
                    ),
                    appIdentifier = t.getString(APP_IDENTIFIER_FIELD),
                    sdkSessionId = t.getString(SDK_SESSION_ID_FIELD),
                    sdkIntegrationType =
                    t.optNullableString(SDK_INTEGRATION_TYPE_FIELD)
                        ?.let { SdkIntegrationType.valueOf(it) },
                    sdkPaymentHandling = t.optNullableString(SDK_PAYMENT_HANDLING_FIELD),
                    checkoutSessionId = t.getString(CHECKOUT_SESSION_ID_FIELD),
                    clientSessionId = t.optNullableString(CLIENT_SESSION_ID_FIELD),
                    orderId = t.optNullableString(ORDER_ID_FIELD),
                    primerAccountId = t.optNullableString(PRIMER_ACCOUNT_ID_FIELD),
                    analyticsUrl = t.optNullableString(ANALYTICS_URL_FIELD),
                    createdAt = t.getLong(CREATED_AT_FIELD),
                )
            }
    }
}

internal data class CrashProperties(val stacktrace: List<String>) : BaseAnalyticsProperties() {
    companion object {
        private const val STACKTRACE_FIELD = "stacktrace"

        @JvmField
        val serializer =
            JSONObjectSerializer<CrashProperties> { t ->
                JSONObject().apply {
                    put(STACKTRACE_FIELD, JSONArray(t.stacktrace))
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                CrashProperties(t.getJSONArray(STACKTRACE_FIELD).sequence<String>().toList())
            }
    }
}
