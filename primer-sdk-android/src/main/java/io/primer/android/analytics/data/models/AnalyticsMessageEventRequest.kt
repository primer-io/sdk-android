package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.data.settings.PrimerPaymentHandling
import org.json.JSONObject

internal data class AnalyticsMessageEventRequest(
    override val device: DeviceData,
    override val properties: MessageProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType,
    override val sdkPaymentHandling: PrimerPaymentHandling,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    override val eventType: AnalyticsEventType = AnalyticsEventType.MESSAGE_EVENT,
    override val createdAt: Long = System.currentTimeMillis()
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsMessageEventRequest = copy(
        analyticsUrl = newAnalyticsUrl
    )

    companion object {

        @JvmField
        val serializer = JSONObjectSerializer<AnalyticsMessageEventRequest> { t ->
            baseSerializer.serialize(t).apply {
                put(
                    PROPERTIES_FIELD,
                    JSONSerializationUtils.getJsonObjectSerializer<MessageProperties>()
                        .serialize(t.properties)
                )
            }
        }

        @JvmField
        val deserializer = JSONObjectDeserializer<AnalyticsMessageEventRequest> { t ->
            AnalyticsMessageEventRequest(
                JSONSerializationUtils.getJsonObjectDeserializer<DeviceData>().deserialize(
                    t.getJSONObject(DEVICE_FIELD)
                ),
                JSONSerializationUtils.getJsonObjectDeserializer<MessageProperties>()
                    .deserialize(t.getJSONObject(PROPERTIES_FIELD)),
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

internal data class MessageProperties(
    val messageType: MessageType,
    val message: String,
    val severity: Severity,
    val diagnosticsId: String? = null,
    val context: AnalyticsContext? = null
) : BaseAnalyticsProperties() {
    companion object {

        private const val MESSAGE_TYPE_FIELD = "messageType"
        private const val MESSAGE_FIELD = "message"
        private const val SEVERITY_FIELD = "severity"
        private const val DIAGNOSTICS_ID_FIELD = "diagnosticsId"
        private const val ANALYTICS_CONTEXT_FIELD = "context"

        @JvmField
        val serializer = JSONObjectSerializer<MessageProperties> { t ->
            JSONObject().apply {
                put(MESSAGE_TYPE_FIELD, t.messageType.name)
                put(MESSAGE_FIELD, t.message)
                put(SEVERITY_FIELD, t.severity.name)
                putOpt(DIAGNOSTICS_ID_FIELD, t.diagnosticsId)
                putOpt(
                    ANALYTICS_CONTEXT_FIELD,
                    t.context?.let {
                        JSONSerializationUtils.getJsonObjectSerializer<AnalyticsContext>()
                            .serialize(it)
                    }
                )
            }
        }

        @JvmField
        val deserializer = JSONObjectDeserializer<MessageProperties> { t ->
            MessageProperties(
                MessageType.valueOf(t.getString(MESSAGE_TYPE_FIELD)),
                t.getString(MESSAGE_FIELD),
                Severity.valueOf(t.getString(SEVERITY_FIELD)),
                t.optNullableString(DIAGNOSTICS_ID_FIELD),
                t.optJSONObject(ANALYTICS_CONTEXT_FIELD)?.let {
                    JSONSerializationUtils.getJsonObjectDeserializer<AnalyticsContext>()
                        .deserialize(it)
                }
            )
        }
    }
}

internal enum class MessageType {
    INFO, VALIDATION_FAILED, ERROR, PM_IMAGE_LOADING_FAILED, RETRY, RETRY_FAILED, RETRY_SUCCESS
}

internal enum class Severity {
    INFO, WARN, ERROR
}
