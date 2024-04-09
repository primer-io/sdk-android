package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.data.settings.PrimerPaymentHandling
import org.json.JSONObject

internal data class AnalyticsUIEventRequest(
    override val device: DeviceData,
    override val properties: UIProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val sdkIntegrationType: SdkIntegrationType,
    override val sdkPaymentHandling: PrimerPaymentHandling,
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
        val serializer = JSONObjectSerializer<AnalyticsUIEventRequest> { t ->
            baseSerializer.serialize(t).apply {
                put(
                    PROPERTIES_FIELD,
                    JSONSerializationUtils.getJsonObjectSerializer<UIProperties>()
                        .serialize(t.properties)
                )
            }
        }

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            AnalyticsUIEventRequest(
                JSONSerializationUtils.getJsonObjectDeserializer<DeviceData>().deserialize(
                    t.getJSONObject(DEVICE_FIELD)
                ),
                JSONSerializationUtils.getJsonObjectDeserializer<UIProperties>().deserialize(
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
        val serializer = JSONObjectSerializer<UIProperties> { t ->
            JSONObject().apply {
                put(ACTION_FIELD, t.action.name)
                put(OBJECT_TYPE_FIELD, t.objectType.name)
                put(PLACE_FIELD, t.place)
                putOpt(OBJECT_ID_FIELD, t.objectId?.name)
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
        val deserializer = JSONObjectDeserializer { t ->
            UIProperties(
                AnalyticsAction.valueOf(t.getString(ACTION_FIELD)),
                ObjectType.valueOf(t.getString(OBJECT_TYPE_FIELD)),
                Place.valueOf(t.getString(PLACE_FIELD)),
                t.optNullableString(OBJECT_ID_FIELD)?.let { ObjectId.valueOf(it) },
                t.optJSONObject(ANALYTICS_CONTEXT_FIELD)?.let {
                    JSONSerializationUtils.getJsonObjectDeserializer<AnalyticsContext>()
                        .deserialize(it)
                }
            )
        }
    }
}

internal enum class AnalyticsAction {
    CLICK, HOVER, VIEW, FOCUS, BLUR, PRESENT, DISMISS
}

internal enum class ObjectType {
    BUTTON, LABEL, INPUT, IMAGE, ALERT, LOADER, LIST_ITEM, WEB_PAGE, VIEW, `3RD_PARTY_VIEW`
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

@Suppress("EnumNaming")
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
    DIRECT_CHECKOUT,
    IPAY88_VIEW,
    CVV_RECAPTURE_VIEW
}
