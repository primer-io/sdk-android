package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.extensions.optNullableLong
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.data.settings.PrimerPaymentHandling
import org.json.JSONObject

internal data class AnalyticsTimerEventRequest(
    override val device: DeviceData,
    override val properties: TimerProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType,
    override val sdkPaymentHandling: PrimerPaymentHandling,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    override val eventType: AnalyticsEventType = AnalyticsEventType.TIMER_EVENT,
    override val createdAt: Long = System.currentTimeMillis()
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsTimerEventRequest = copy(
        analyticsUrl = newAnalyticsUrl
    )

    companion object {

        @JvmField
        val serializer = JSONObjectSerializer<AnalyticsTimerEventRequest> { t ->
            baseSerializer.serialize(t).apply {
                put(
                    PROPERTIES_FIELD,
                    JSONSerializationUtils.getJsonObjectSerializer<TimerProperties>()
                        .serialize(t.properties)
                )
            }
        }

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            AnalyticsTimerEventRequest(
                JSONSerializationUtils.getJsonObjectDeserializer<DeviceData>().deserialize(
                    t.getJSONObject(DEVICE_FIELD)
                ),
                JSONSerializationUtils.getJsonObjectDeserializer<TimerProperties>().deserialize(
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

internal data class TimerProperties(
    val id: TimerId,
    val timerType: TimerType,
    val duration: Long? = null,
    val analyticsContext: AnalyticsContext? = null
) : BaseAnalyticsProperties() {
    companion object {

        private const val ID_FIELD = "id"
        private const val TIMER_TYPE_FIELD = "timerType"
        private const val DURATION_FIELD = "duration"
        private const val ANALYTICS_CONTEXT_FIELD = "context"

        @JvmField
        val serializer = JSONObjectSerializer<TimerProperties> { t ->
            JSONObject().apply {
                put(ID_FIELD, t.id)
                put(TIMER_TYPE_FIELD, t.timerType.name)
                put(DURATION_FIELD, t.duration)
                putOpt(
                    ANALYTICS_CONTEXT_FIELD,
                    t.analyticsContext?.let {
                        JSONSerializationUtils.getJsonObjectSerializer<AnalyticsContext>()
                            .serialize(it)
                    }
                )
            }
        }

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            TimerProperties(
                id = TimerId.valueOf(t.getString(ID_FIELD)),
                timerType = TimerType.valueOf(t.getString(TIMER_TYPE_FIELD)),
                duration = t.optNullableLong(DURATION_FIELD),
                analyticsContext = t.optJSONObject(ANALYTICS_CONTEXT_FIELD)?.let {
                    JSONSerializationUtils.getJsonObjectDeserializer<AnalyticsContext>()
                        .deserialize(it)
                }
            )
        }
    }
}

internal enum class TimerType {
    START,
    END
}

internal enum class TimerId {
    CHECKOUT_DURATION,
    PM_ALL_IMAGES_LOADING_DURATION,
    PM_IMAGE_LOADING_DURATION,
    HEADLESS_LOADING,
    CONFIGURATION_LOADING,
    DROP_IN_LOADING
}
