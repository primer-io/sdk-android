package io.primer.android.analytics.data.models

import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.payment.dummy.DummyDecisionType
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
internal data class AnalyticsUIEventRequest(
    override val device: DeviceData,
    override val properties: UIProperties,
    override val appIdentifier: String,
    override val sdkSessionId: String,
    override val checkoutSessionId: String,
    override val clientSessionId: String?,
    override val orderId: String?,
    override val primerAccountId: String?,
    override val analyticsUrl: String?,
    @EncodeDefault
    override val eventType: AnalyticsEventType = AnalyticsEventType.UI_EVENT,
) : BaseAnalyticsEventRequest() {

    override fun copy(newAnalyticsUrl: String?): AnalyticsUIEventRequest = copy(
        analyticsUrl = newAnalyticsUrl
    )
}

@Serializable
internal data class UIProperties(
    val action: AnalyticsAction,
    val objectType: ObjectType,
    val place: Place,
    val objectId: ObjectId?,
    val context: AnalyticsContext?
) : BaseAnalyticsProperties()

@Serializable
internal data class AnalyticsContext(
    val paymentMethodType: PaymentMethodType? = null,
    val issuerId: String? = null,
    val paymentMethodId: String? = null,
    val url: String? = null,
    val decision: DummyDecisionType? = null
) : BaseAnalyticsProperties()

@Serializable
internal enum class AnalyticsAction {
    CLICK, HOVER, VIEW, FOCUS, BLUR
}

@Serializable
internal enum class ObjectType {
    BUTTON, LABEL, INPUT, IMAGE, ALERT, LOADER, LIST_ITEM, WEB_PAGE, VIEW,
}

@Serializable
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

@Serializable
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
