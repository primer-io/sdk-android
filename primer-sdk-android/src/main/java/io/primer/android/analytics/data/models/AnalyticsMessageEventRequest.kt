package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.core.serialization.json.extensions.optNullableString
import org.json.JSONObject

internal data class AnalyticsMessageEventRequest(
    override val device: DeviceData,
    override val properties: MessageProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    override val eventType: AnalyticsEventType = AnalyticsEventType.MESSAGE_EVENT,
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsMessageEventRequest = copy(
        analyticsUrl = newAnalyticsUrl
    )

    companion object {

        @JvmField
        val serializer = object : JSONSerializer<AnalyticsMessageEventRequest> {
            override fun serialize(t: AnalyticsMessageEventRequest): JSONObject {
                return baseSerializer.serialize(t).apply {
                    put(
                        PROPERTIES_FIELD,
                        JSONSerializationUtils.getSerializer<MessageProperties>()
                            .serialize(t.properties)
                    )
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<AnalyticsMessageEventRequest> {
            override fun deserialize(t: JSONObject): AnalyticsMessageEventRequest {
                return AnalyticsMessageEventRequest(
                    JSONSerializationUtils.getDeserializer<DeviceData>().deserialize(
                        t.getJSONObject(DEVICE_FIELD)
                    ),
                    JSONSerializationUtils.getDeserializer<MessageProperties>().deserialize(
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

internal data class MessageProperties(
    val messageType: MessageType,
    val message: String,
    val severity: Severity,
    val diagnosticsId: String? = null
) : BaseAnalyticsProperties() {
    companion object {

        private const val MESSAGE_TYPE_FIELD = "resumeToken"
        private const val MESSAGE_FIELD = "message"
        private const val SEVERITY_FIELD = "severity"
        private const val DIAGNOSTICS_ID_FIELD = "diagnosticsId"

        @JvmField
        val serializer = object : JSONSerializer<MessageProperties> {
            override fun serialize(t: MessageProperties): JSONObject {
                return JSONObject().apply {
                    put(MESSAGE_TYPE_FIELD, t.messageType.name)
                    put(MESSAGE_FIELD, t.message)
                    put(SEVERITY_FIELD, t.severity.name)
                    putOpt(DIAGNOSTICS_ID_FIELD, t.diagnosticsId)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<MessageProperties> {
            override fun deserialize(t: JSONObject): MessageProperties {
                return MessageProperties(
                    MessageType.valueOf(t.getString(MESSAGE_TYPE_FIELD)),
                    t.getString(MESSAGE_FIELD),
                    Severity.valueOf(t.getString(SEVERITY_FIELD)),
                    t.optNullableString(DIAGNOSTICS_ID_FIELD)
                )
            }
        }
    }
}

internal enum class MessageType {
    VALIDATION_FAILED, ERROR, PM_IMAGE_LOADING_FAILED
}

internal enum class Severity {
    INFO, WARN, ERROR
}
