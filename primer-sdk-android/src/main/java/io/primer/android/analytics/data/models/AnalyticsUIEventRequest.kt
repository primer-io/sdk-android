package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.payment.dummy.DummyDecisionType
import org.json.JSONObject

internal data class AnalyticsUIEventRequest(
    override val device: DeviceData,
    override val properties: UIProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    override val eventType: AnalyticsEventType = AnalyticsEventType.UI_EVENT,
    override val createdAt: Long = System.currentTimeMillis()
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsUIEventRequest = copy(
        analyticsUrl = newAnalyticsUrl
    )

    companion object {

        @JvmField
        val serializer = object : JSONSerializer<AnalyticsUIEventRequest> {
            override fun serialize(t: AnalyticsUIEventRequest): JSONObject {
                return baseSerializer.serialize(t).apply {
                    put(
                        PROPERTIES_FIELD,
                        JSONSerializationUtils.getSerializer<UIProperties>()
                            .serialize(t.properties)
                    )
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<AnalyticsUIEventRequest> {
            override fun deserialize(t: JSONObject): AnalyticsUIEventRequest {
                return AnalyticsUIEventRequest(
                    JSONSerializationUtils.getDeserializer<DeviceData>().deserialize(
                        t.getJSONObject(DEVICE_FIELD)
                    ),
                    JSONSerializationUtils.getDeserializer<UIProperties>().deserialize(
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

internal data class UIProperties(
    val action: AnalyticsAction,
    val objectType: ObjectType,
    val place: Place,
    val objectId: ObjectId?,
    val context: AnalyticsContext?
) : BaseAnalyticsProperties() {

    companion object {

        private const val ACTION_FIELD = "action"
        private const val OBJECT_TYPE_FIELD = "objectType"
        private const val PLACE_FIELD = "place"
        private const val OBJECT_ID_FIELD = "objectId"
        private const val ANALYTICS_CONTEXT_FIELD = "context"

        @JvmField
        val serializer = object : JSONSerializer<UIProperties> {
            override fun serialize(t: UIProperties): JSONObject {
                return JSONObject().apply {
                    put(ACTION_FIELD, t.action.name)
                    put(OBJECT_TYPE_FIELD, t.objectType.name)
                    put(PLACE_FIELD, t.place)
                    putOpt(OBJECT_ID_FIELD, t.objectId?.name)
                    putOpt(
                        ANALYTICS_CONTEXT_FIELD,
                        t.context?.let {
                            JSONSerializationUtils.getSerializer<AnalyticsContext>()
                                .serialize(it)
                        }
                    )
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<UIProperties> {
            override fun deserialize(t: JSONObject): UIProperties {
                return UIProperties(
                    AnalyticsAction.valueOf(t.getString(ACTION_FIELD)),
                    ObjectType.valueOf(t.getString(OBJECT_TYPE_FIELD)),
                    Place.valueOf(t.getString(PLACE_FIELD)),
                    t.optNullableString(OBJECT_ID_FIELD)?.let { ObjectId.valueOf(it) },
                    t.optJSONObject(ANALYTICS_CONTEXT_FIELD)?.let {
                        JSONSerializationUtils.getDeserializer<AnalyticsContext>()
                            .deserialize(it)
                    }
                )
            }
        }
    }
}

internal data class AnalyticsContext(
    val paymentMethodType: String? = null,
    val issuerId: String? = null,
    val paymentMethodId: String? = null,
    val url: String? = null,
    val decision: DummyDecisionType? = null
) : BaseAnalyticsProperties() {

    companion object {

        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
        private const val ISSUER_ID_FIELD = "issuerId"
        private const val PAYMENT_METHOD_ID_FIELD = "paymentMethodId"
        private const val URL_FIELD = "url"
        private const val DECISION_FIELD = "decision"

        @JvmField
        val serializer = object : JSONSerializer<AnalyticsContext> {
            override fun serialize(t: AnalyticsContext): JSONObject {
                return JSONObject().apply {
                    putOpt(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                    putOpt(ISSUER_ID_FIELD, t.issuerId)
                    putOpt(PAYMENT_METHOD_ID_FIELD, t.paymentMethodId)
                    putOpt(URL_FIELD, t.url)
                    putOpt(DECISION_FIELD, t.decision?.name)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<AnalyticsContext> {
            override fun deserialize(t: JSONObject): AnalyticsContext {
                return AnalyticsContext(
                    t.optNullableString(PAYMENT_METHOD_TYPE_FIELD),
                    t.optNullableString(ISSUER_ID_FIELD),
                    t.optNullableString(PAYMENT_METHOD_ID_FIELD),
                    t.optNullableString(URL_FIELD),
                    t.optNullableString(DECISION_FIELD)?.let { DummyDecisionType.valueOf(it) }
                )
            }
        }
    }
}

internal enum class AnalyticsAction {
    CLICK, HOVER, VIEW, FOCUS, BLUR
}

internal enum class ObjectType {
    BUTTON, LABEL, INPUT, IMAGE, ALERT, LOADER, LIST_ITEM, WEB_PAGE, VIEW,
}

internal enum class ObjectId {
    BACK,
    SEE_ALL,
    SUBMIT,
    VIEW,
    CANCEL,
    RETRY,
    PAY,
    DONE,
    EDIT,
    SELECT,
    DELETE,
    CARD_NUMBER,
    CVC,
    MANAGE,
    ZIP_C0DE,
    CARD_HOLDER,
    EXPIRY
}

internal enum class Place {
    PAYMENT_METHODS_LIST, // The vaulted payment methods
    UNIVERSAL_CHECKOUT,
    VAULT_MANAGER,
    BANK_SELECTION_LIST,
    SDK_LOADING,
    CARD_FORM,
    DYNAMIC_FORM,
    PAYMENT_METHOD_LOADING,
    PAYMENT_METHOD_POPUP,
    SUCCESS_SCREEN,
    ERROR_SCREEN,
    PRIMER_TEST_PAYMENT_METHOD_DECISION_SCREEN,
    WEBVIEW,
    `3DS_VIEW`,
    DIRECT_CHECKOUT
}
