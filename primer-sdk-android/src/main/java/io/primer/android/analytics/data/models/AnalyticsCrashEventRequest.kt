package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.core.serialization.json.extensions.sequence
import io.primer.android.data.settings.PrimerPaymentHandling
import org.json.JSONArray
import org.json.JSONObject

internal data class AnalyticsCrashEventRequest(
    override val device: DeviceData,
    override val properties: CrashProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType,
    override val sdkPaymentHandling: PrimerPaymentHandling,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    override val eventType: AnalyticsEventType = AnalyticsEventType.APP_CRASHED_EVENT,
    override val createdAt: Long = System.currentTimeMillis()
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsCrashEventRequest = copy(
        analyticsUrl = newAnalyticsUrl
    )

    companion object {

        @JvmField
        val serializer = object : JSONSerializer<AnalyticsCrashEventRequest> {
            override fun serialize(t: AnalyticsCrashEventRequest): JSONObject {
                return baseSerializer.serialize(t).apply {
                    put(
                        PROPERTIES_FIELD,
                        JSONSerializationUtils.getSerializer<CrashProperties>()
                            .serialize(t.properties)
                    )
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<AnalyticsCrashEventRequest> {
            override fun deserialize(t: JSONObject): AnalyticsCrashEventRequest {
                return AnalyticsCrashEventRequest(
                    JSONSerializationUtils.getDeserializer<DeviceData>().deserialize(
                        t.getJSONObject(DEVICE_FIELD)
                    ),
                    JSONSerializationUtils.getDeserializer<CrashProperties>().deserialize(
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
}

internal data class CrashProperties(val stacktrace: List<String>) : BaseAnalyticsProperties() {

    companion object {

        private const val STACKTRACE_FIELD = "stacktrace"

        @JvmField
        val serializer = object : JSONSerializer<CrashProperties> {
            override fun serialize(t: CrashProperties): JSONObject {
                return JSONObject().apply {
                    put(STACKTRACE_FIELD, JSONArray(t.stacktrace))
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<CrashProperties> {
            override fun deserialize(t: JSONObject): CrashProperties {
                return CrashProperties(t.getJSONArray(STACKTRACE_FIELD).sequence<String>().toList())
            }
        }
    }
}
