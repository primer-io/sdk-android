package io.primer.android.analytics.data.models

import io.primer.android.analytics.data.helper.SdkTypeResolver
import io.primer.android.analytics.domain.models.BankIssuerContextParams
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.analytics.domain.models.IPay88PaymentMethodContextParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.models.PaymentInstrumentIdContextParams
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.ProcessorTestDecisionParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams
import io.primer.android.analytics.domain.models.ThreeDsProtocolFailureContextParams
import io.primer.android.analytics.domain.models.ThreeDsRuntimeFailureContextParams
import io.primer.android.analytics.domain.models.TimerAnalyticsParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.analytics.domain.models.UrlContextParams
import io.primer.android.core.BuildConfig
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONObject

@Suppress("UnusedPrivateMember")
internal sealed class BaseAnalyticsEventRequest : JSONObjectSerializable, JSONDeserializable {
    abstract val device: DeviceData?
    abstract val properties: BaseAnalyticsProperties
    abstract val appIdentifier: String?
    abstract val sdkSessionId: String
    abstract val sdkIntegrationType: SdkIntegrationType?
    abstract val sdkPaymentHandling: String?
    abstract val checkoutSessionId: String?
    abstract val clientSessionId: String?
    abstract val orderId: String?
    abstract val primerAccountId: String?
    abstract val analyticsUrl: String?
    abstract val eventType: AnalyticsEventType
    abstract val createdAt: Long
    protected val sdkType: SdkType = SdkTypeResolver().resolve()
    protected val sdkVersion: String = BuildConfig.SDK_VERSION_STRING

    abstract fun copy(newAnalyticsUrl: String?): BaseAnalyticsEventRequest

    protected companion object {
        const val DEVICE_FIELD = "device"
        const val PROPERTIES_FIELD = "properties"
        const val APP_IDENTIFIER_FIELD = "appIdentifier"
        const val SDK_SESSION_ID_FIELD = "sdkSessionId"
        const val CHECKOUT_SESSION_ID_FIELD = "checkoutSessionId"
        const val CLIENT_SESSION_ID_FIELD = "clientSessionId"
        const val ORDER_ID_FIELD = "orderId"
        const val PRIMER_ACCOUNT_ID_FIELD = "primerAccountId"
        const val ANALYTICS_URL_FIELD = "analyticsUrl"
        const val EVENT_TYPE_FIELD = "eventType"
        const val CREATED_AT_FIELD = "createdAt"
        const val SDK_TYPE_FIELD = "sdkType"
        const val SDK_VERSION_FIELD = "sdkVersion"
        const val SDK_INTEGRATION_TYPE_FIELD = "sdkIntegrationType"
        const val SDK_PAYMENT_HANDLING_FIELD = "sdkPaymentHandling"

        @JvmField
        val serializer = JSONObjectSerializer<BaseAnalyticsEventRequest> { t ->
            when (t.eventType) {
                AnalyticsEventType.UI_EVENT ->
                    AnalyticsUIEventRequest.serializer.serialize(t as AnalyticsUIEventRequest)

                AnalyticsEventType.APP_CRASHED_EVENT ->
                    AnalyticsCrashEventRequest
                        .serializer.serialize(t as AnalyticsCrashEventRequest)

                AnalyticsEventType.NETWORK_CONNECTIVITY_EVENT ->
                    AnalyticsNetworkConnectivityEventRequest
                        .serializer.serialize(t as AnalyticsNetworkConnectivityEventRequest)

                AnalyticsEventType.NETWORK_CALL_EVENT ->
                    AnalyticsNetworkCallEvent
                        .serializer.serialize(t as AnalyticsNetworkCallEvent)

                AnalyticsEventType.TIMER_EVENT ->
                    AnalyticsTimerEventRequest
                        .serializer.serialize(t as AnalyticsTimerEventRequest)

                AnalyticsEventType.MESSAGE_EVENT ->
                    AnalyticsMessageEventRequest.serializer.serialize(
                        t as AnalyticsMessageEventRequest
                    )

                AnalyticsEventType.SDK_FUNCTION_EVENT ->
                    AnalyticsSdkFunctionEventRequest.serializer.serialize(
                        t as AnalyticsSdkFunctionEventRequest
                    )
            }
        }

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            when (AnalyticsEventType.valueOf(t.getString(EVENT_TYPE_FIELD))) {
                AnalyticsEventType.UI_EVENT ->
                    AnalyticsUIEventRequest.deserializer.deserialize(t)

                AnalyticsEventType.APP_CRASHED_EVENT ->
                    AnalyticsCrashEventRequest.deserializer.deserialize(t)

                AnalyticsEventType.NETWORK_CONNECTIVITY_EVENT ->
                    AnalyticsNetworkConnectivityEventRequest.deserializer.deserialize(t)

                AnalyticsEventType.NETWORK_CALL_EVENT ->
                    AnalyticsNetworkCallEvent.deserializer.deserialize(t)

                AnalyticsEventType.TIMER_EVENT ->
                    AnalyticsTimerEventRequest.deserializer.deserialize(t)

                AnalyticsEventType.MESSAGE_EVENT ->
                    AnalyticsMessageEventRequest.deserializer.deserialize(t)

                AnalyticsEventType.SDK_FUNCTION_EVENT ->
                    AnalyticsSdkFunctionEventRequest.deserializer.deserialize(t)
            }
        }

        val baseSerializer = JSONObjectSerializer<BaseAnalyticsEventRequest> { t ->
            JSONObject().apply {
                putOpt(
                    DEVICE_FIELD,
                    t.device?.let {
                        JSONSerializationUtils.getJsonObjectSerializer<DeviceData>()
                            .serialize(it)
                    }
                )
                put(APP_IDENTIFIER_FIELD, t.appIdentifier)
                put(SDK_SESSION_ID_FIELD, t.sdkSessionId)
                putOpt(CHECKOUT_SESSION_ID_FIELD, t.checkoutSessionId)
                putOpt(CLIENT_SESSION_ID_FIELD, t.clientSessionId)
                putOpt(ORDER_ID_FIELD, t.orderId)
                putOpt(PRIMER_ACCOUNT_ID_FIELD, t.primerAccountId)
                putOpt(ANALYTICS_URL_FIELD, t.analyticsUrl)
                putOpt(EVENT_TYPE_FIELD, t.eventType.name)
                put(CREATED_AT_FIELD, t.createdAt)
                put(SDK_TYPE_FIELD, t.sdkType.name)
                put(SDK_VERSION_FIELD, t.sdkVersion)
                putOpt(SDK_INTEGRATION_TYPE_FIELD, t.sdkIntegrationType?.name)
                putOpt(SDK_PAYMENT_HANDLING_FIELD, t.sdkPaymentHandling)
            }
        }
    }
}

abstract class BaseAnalyticsProperties : JSONObjectSerializable, JSONDeserializable

@Suppress("LongParameterList", "LongMethod")
internal fun BaseAnalyticsProperties.toAnalyticsEvent(
    batteryLevel: Int,
    batteryStatus: BatteryStatus,
    screenData: ScreenData,
    deviceId: String,
    appIdentifier: String,
    sdkSessionId: String,
    sdkIntegrationType: SdkIntegrationType?,
    sdkPaymentHandling: String?,
    checkoutSessionId: String,
    clientSessionId: String?,
    orderId: String?,
    primerAccountId: String?,
    analyticsUrl: String?
) = when (this) {
    is NetworkCallProperties -> AnalyticsNetworkCallEvent(
        DeviceData(
            batteryLevel,
            batteryStatus,
            screenData,
            deviceId
        ),
        this,
        appIdentifier,
        sdkSessionId,
        sdkIntegrationType,
        sdkPaymentHandling,
        checkoutSessionId,
        clientSessionId,
        orderId,
        primerAccountId,
        analyticsUrl
    )

    is CrashProperties -> AnalyticsCrashEventRequest(
        DeviceData(
            batteryLevel,
            batteryStatus,
            screenData,
            deviceId
        ),
        this,
        appIdentifier,
        sdkSessionId,
        sdkIntegrationType,
        sdkPaymentHandling,
        checkoutSessionId,
        clientSessionId,
        orderId,
        primerAccountId,
        analyticsUrl
    )

    is NetworkTypeProperties -> AnalyticsNetworkConnectivityEventRequest(
        DeviceData(
            batteryLevel,
            batteryStatus,
            screenData,
            deviceId
        ),
        this,
        appIdentifier,
        sdkSessionId,
        sdkIntegrationType,
        sdkPaymentHandling,
        checkoutSessionId,
        clientSessionId,
        orderId,
        primerAccountId,
        analyticsUrl
    )

    is MessageProperties -> AnalyticsMessageEventRequest(
        DeviceData(
            batteryLevel,
            batteryStatus,
            screenData,
            deviceId
        ),
        this,
        appIdentifier,
        sdkSessionId,
        sdkIntegrationType,
        sdkPaymentHandling,
        checkoutSessionId,
        clientSessionId,
        orderId,
        primerAccountId,
        analyticsUrl
    )

    is TimerProperties -> AnalyticsTimerEventRequest(
        DeviceData(
            batteryLevel,
            batteryStatus,
            screenData,
            deviceId
        ),
        this,
        appIdentifier,
        sdkSessionId,
        sdkIntegrationType,
        sdkPaymentHandling,
        checkoutSessionId,
        clientSessionId,
        orderId,
        primerAccountId,
        analyticsUrl
    )

    else -> throw IllegalStateException("Unsupported property params")
}

@Suppress("LongParameterList", "LongMethod")
internal fun BaseAnalyticsParams.toAnalyticsEvent(
    batteryLevel: Int,
    batteryStatus: BatteryStatus,
    screenData: ScreenData,
    deviceId: String,
    appIdentifier: String,
    sdkSessionId: String,
    sdkIntegrationType: SdkIntegrationType?,
    sdkPaymentHandling: String?,
    checkoutSessionId: String,
    clientSessionId: String?,
    orderId: String?,
    primerAccountId: String?,
    analyticsUrl: String?
) = when (this) {
    is UIAnalyticsParams -> AnalyticsUIEventRequest(
        DeviceData(
            batteryLevel,
            batteryStatus,
            screenData,
            deviceId
        ),
        UIProperties(action, objectType, place, objectId, context?.toAnalyticsContext()),
        appIdentifier,
        sdkSessionId,
        sdkIntegrationType,
        sdkPaymentHandling,
        checkoutSessionId,
        clientSessionId,
        orderId,
        primerAccountId,
        analyticsUrl
    )

    is TimerAnalyticsParams -> AnalyticsTimerEventRequest(
        DeviceData(
            batteryLevel,
            batteryStatus,
            screenData,
            deviceId
        ),
        TimerProperties(id = id, timerType = timerType, duration = duration, analyticsContext = context),
        appIdentifier,
        sdkSessionId,
        sdkIntegrationType,
        sdkPaymentHandling,
        checkoutSessionId,
        clientSessionId,
        orderId,
        primerAccountId,
        analyticsUrl
    )

    is MessageAnalyticsParams -> AnalyticsMessageEventRequest(
        DeviceData(
            batteryLevel,
            batteryStatus,
            screenData,
            deviceId
        ),
        MessageProperties(
            messageType,
            message,
            severity,
            diagnosticsId,
            context?.toAnalyticsContext()
        ),
        appIdentifier,
        sdkSessionId,
        sdkIntegrationType,
        sdkPaymentHandling,
        checkoutSessionId,
        clientSessionId,
        orderId,
        primerAccountId,
        analyticsUrl
    )

    is SdkFunctionParams -> AnalyticsSdkFunctionEventRequest(
        DeviceData(
            batteryLevel,
            batteryStatus,
            screenData,
            deviceId
        ),
        FunctionProperties(name, params),
        appIdentifier,
        sdkSessionId,
        sdkIntegrationType,
        sdkPaymentHandling,
        checkoutSessionId,
        clientSessionId,
        orderId,
        primerAccountId,
        analyticsUrl
    )

    else -> throw IllegalStateException("Unsupported event params")
}

internal fun BaseContextParams.toAnalyticsContext() = when (this) {
    is PaymentMethodContextParams -> PaymentMethodAnalyticsContext(
        paymentMethodType = paymentMethodType
    )

    is BankIssuerContextParams -> BankIssuerAnalyticsContext(
        issuerId = issuerId
    )

    is PaymentInstrumentIdContextParams -> PaymentInstrumentIdAnalyticsContext(
        paymentMethodId = id
    )

    is UrlContextParams -> UrlAnalyticsContext(
        url = url
    )

    is ProcessorTestDecisionParams -> ProcessorTestAnalyticsContext(
        decision = decision
    )

    is IPay88PaymentMethodContextParams -> IPay88AnalyticsContext(
        paymentMethodType = paymentMethodType,
        iPay88PaymentMethodId = iPay88PaymentMethodId,
        iPay88ActionType = iPay88ActionType
    )

    is ThreeDsRuntimeFailureContextParams -> ThreeDsRuntimeFailureAnalyticsContext(
        threeDsSdkVersion,
        initProtocolVersion,
        errorCode,
        threeDsWrapperSdkVersion,
        threeDsSdkProvider
    )

    is ThreeDsProtocolFailureContextParams -> ThreeDsProtocolFailureAnalyticsContext(
        errorDetails,
        description,
        errorCode,
        errorType,
        component,
        transactionId,
        version,
        threeDsSdkVersion,
        initProtocolVersion,
        threeDsWrapperSdkVersion,
        threeDsSdkProvider
    )

    is ThreeDsFailureContextParams -> ThreeDsFailureAnalyticsContext(
        threeDsSdkVersion,
        initProtocolVersion,
        threeDsWrapperSdkVersion,
        threeDsSdkProvider
    )

    is ErrorContextParams -> ErrorAnalyticsContext(errorId, paymentMethodType)
}
