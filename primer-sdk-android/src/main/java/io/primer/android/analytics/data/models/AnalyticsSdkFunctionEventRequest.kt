package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.core.serialization.json.extensions.toStringMap
import io.primer.android.data.settings.PrimerPaymentHandling
import org.json.JSONObject

internal data class AnalyticsSdkFunctionEventRequest(
    override val device: DeviceData? = null,
    override val properties: FunctionProperties,
    override val appIdentifier: String? = null,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType,
    override val sdkPaymentHandling: PrimerPaymentHandling,
    override val checkoutSessionId: String? = null,
    override val clientSessionId: String? = null,
    override val orderId: String? = null,
    override val primerAccountId: String? = null,
    override val analyticsUrl: String? = null,
    override val eventType: AnalyticsEventType = AnalyticsEventType.SDK_FUNCTION_EVENT,
    override val createdAt: Long = System.currentTimeMillis()
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsSdkFunctionEventRequest = copy(
        analyticsUrl = newAnalyticsUrl
    )

    companion object {

        @JvmField
        val serializer = object : JSONObjectSerializer<AnalyticsSdkFunctionEventRequest> {
            override fun serialize(t: AnalyticsSdkFunctionEventRequest): JSONObject {
                return baseSerializer.serialize(t).apply {
                    put(
                        PROPERTIES_FIELD,
                        JSONSerializationUtils.getJsonObjectSerializer<FunctionProperties>()
                            .serialize(t.properties)
                    )
                }
            }
        }

        @JvmField
        val deserializer = object : JSONObjectDeserializer<AnalyticsSdkFunctionEventRequest> {
            override fun deserialize(t: JSONObject): AnalyticsSdkFunctionEventRequest {
                return AnalyticsSdkFunctionEventRequest(
                    t.optJSONObject(DEVICE_FIELD)?.let {
                        JSONSerializationUtils.getJsonObjectDeserializer<DeviceData>().deserialize(
                            it
                        )
                    },
                    JSONSerializationUtils
                        .getJsonObjectDeserializer<FunctionProperties>().deserialize(
                            t.getJSONObject(PROPERTIES_FIELD)
                        ),
                    t.optNullableString(APP_IDENTIFIER_FIELD),
                    t.getString(SDK_SESSION_ID_FIELD),
                    SdkIntegrationType.valueOf(t.getString(SDK_INTEGRATION_TYPE_FIELD)),
                    PrimerPaymentHandling.valueOf(t.getString(SDK_PAYMENT_HANDLING_FIELD)),
                    t.optNullableString(CHECKOUT_SESSION_ID_FIELD),
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

internal data class FunctionProperties(
    val name: String,
    val params: Map<String, String>
) : BaseAnalyticsProperties() {
    companion object {

        private const val NAME_FIELD = "name"
        private const val PARAMS_FIELD = "params"

        @JvmField
        val serializer = object : JSONObjectSerializer<FunctionProperties> {
            override fun serialize(t: FunctionProperties): JSONObject {
                return JSONObject().apply {
                    put(NAME_FIELD, t.name)
                    put(PARAMS_FIELD, JSONObject(t.params))
                }
            }
        }

        @JvmField
        val deserializer = object : JSONObjectDeserializer<FunctionProperties> {
            override fun deserialize(t: JSONObject): FunctionProperties {
                return FunctionProperties(
                    t.getString(NAME_FIELD),
                    t.getJSONObject(PARAMS_FIELD).toStringMap()
                )
            }
        }
    }
}
