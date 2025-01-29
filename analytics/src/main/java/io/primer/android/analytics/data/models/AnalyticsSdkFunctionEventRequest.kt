package io.primer.android.analytics.data.models

import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.core.data.serialization.json.extensions.toMap
import org.json.JSONObject

internal data class AnalyticsSdkFunctionEventRequest(
    override val device: DeviceData? = null,
    override val properties: FunctionProperties,
    override val appIdentifier: String? = null,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType?,
    override val sdkPaymentHandling: String?,
    override val checkoutSessionId: String? = null,
    override val clientSessionId: String? = null,
    override val orderId: String? = null,
    override val primerAccountId: String? = null,
    override val analyticsUrl: String? = null,
    override val eventType: AnalyticsEventType = AnalyticsEventType.SDK_FUNCTION_EVENT,
    override val createdAt: Long = System.currentTimeMillis(),
) : BaseAnalyticsEventRequest() {
    override fun copy(newAnalyticsUrl: String?): AnalyticsSdkFunctionEventRequest =
        copy(
            analyticsUrl = newAnalyticsUrl,
        )

    companion object {
        @JvmField
        val serializer =
            JSONObjectSerializer<AnalyticsSdkFunctionEventRequest> { t ->
                baseSerializer.serialize(t).apply {
                    put(
                        PROPERTIES_FIELD,
                        JSONSerializationUtils.getJsonObjectSerializer<FunctionProperties>()
                            .serialize(t.properties),
                    )
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                AnalyticsSdkFunctionEventRequest(
                    device =
                    t.optJSONObject(DEVICE_FIELD)?.let {
                        JSONSerializationUtils.getJsonObjectDeserializer<DeviceData>().deserialize(
                            it,
                        )
                    },
                    properties =
                    JSONSerializationUtils
                        .getJsonObjectDeserializer<FunctionProperties>().deserialize(
                            t.getJSONObject(PROPERTIES_FIELD),
                        ),
                    appIdentifier = t.optNullableString(APP_IDENTIFIER_FIELD),
                    sdkSessionId = t.getString(SDK_SESSION_ID_FIELD),
                    sdkIntegrationType =
                    t.optNullableString(SDK_INTEGRATION_TYPE_FIELD)
                        ?.let { SdkIntegrationType.valueOf(it) },
                    sdkPaymentHandling = t.optNullableString(SDK_PAYMENT_HANDLING_FIELD),
                    checkoutSessionId = t.optNullableString(CHECKOUT_SESSION_ID_FIELD),
                    clientSessionId = t.optNullableString(CLIENT_SESSION_ID_FIELD),
                    orderId = t.optNullableString(ORDER_ID_FIELD),
                    primerAccountId = t.optNullableString(PRIMER_ACCOUNT_ID_FIELD),
                    analyticsUrl = t.optNullableString(ANALYTICS_URL_FIELD),
                    createdAt = t.getLong(CREATED_AT_FIELD),
                )
            }
    }
}

internal data class FunctionProperties(
    val name: String,
    val params: Map<String, Any?>,
) : BaseAnalyticsProperties() {
    companion object {
        private const val NAME_FIELD = "name"
        private const val PARAMS_FIELD = "params"

        @JvmField
        val serializer =
            JSONObjectSerializer<FunctionProperties> { t ->
                JSONObject().apply {
                    put(NAME_FIELD, t.name)
                    put(PARAMS_FIELD, JSONObject(t.params))
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                FunctionProperties(
                    t.getString(NAME_FIELD),
                    t.getJSONObject(PARAMS_FIELD).toMap(),
                )
            }
    }
}
