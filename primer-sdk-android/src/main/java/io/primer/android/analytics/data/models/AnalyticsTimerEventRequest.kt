package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.core.serialization.json.extensions.optNullableString
import org.json.JSONObject

internal data class AnalyticsTimerEventRequest(
    override val device: DeviceData,
    override val properties: TimerProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType,
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
        val serializer = object : JSONSerializer<AnalyticsTimerEventRequest> {
            override fun serialize(t: AnalyticsTimerEventRequest): JSONObject {
                return baseSerializer.serialize(t).apply {
                    put(
                        PROPERTIES_FIELD,
                        JSONSerializationUtils.getSerializer<TimerProperties>()
                            .serialize(t.properties)
                    )
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<AnalyticsTimerEventRequest> {
            override fun deserialize(t: JSONObject): AnalyticsTimerEventRequest {
                return AnalyticsTimerEventRequest(
                    JSONSerializationUtils.getDeserializer<DeviceData>().deserialize(
                        t.getJSONObject(DEVICE_FIELD)
                    ),
                    JSONSerializationUtils.getDeserializer<TimerProperties>().deserialize(
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

internal data class TimerProperties(
    val id: TimerId,
    val timerType: TimerType,
    val analyticsContext: AnalyticsContext? = null
) : BaseAnalyticsProperties() {
    companion object {

        private const val ID_FIELD = "id"
        private const val TIMER_TYPE_FIELD = "timerType"
        private const val ANALYTICS_CONTEXT_FIELD = "analyticsContext"

        @JvmField
        val serializer = object : JSONSerializer<TimerProperties> {
            override fun serialize(t: TimerProperties): JSONObject {
                return JSONObject().apply {
                    put(ID_FIELD, t.id)
                    put(TIMER_TYPE_FIELD, t.timerType.name)
                    putOpt(
                        ANALYTICS_CONTEXT_FIELD,
                        t.analyticsContext?.let {
                            JSONSerializationUtils.getSerializer<AnalyticsContext>()
                                .serialize(it)
                        }
                    )
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<TimerProperties> {
            override fun deserialize(t: JSONObject): TimerProperties {
                return TimerProperties(
                    TimerId.valueOf(t.getString(ID_FIELD)),
                    TimerType.valueOf(t.getString(TIMER_TYPE_FIELD)),
                    t.optJSONObject(ANALYTICS_CONTEXT_FIELD)?.let {
                        JSONSerializationUtils.getDeserializer<AnalyticsContext>()
                            .deserialize(it)
                    }
                )
            }
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
    PM_IMAGE_LOADING_DURATION
}
