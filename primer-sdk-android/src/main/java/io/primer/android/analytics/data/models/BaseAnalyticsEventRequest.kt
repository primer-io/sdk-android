package io.primer.android.analytics.data.models

import io.primer.android.BuildConfig
import io.primer.android.analytics.domain.models.BankIssuerContextParams
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.DummyApmDecisionParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.models.PaymentInstrumentIdContextParams
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.models.TimerAnalyticsParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.analytics.domain.models.UrlContextParams
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import org.json.JSONObject

@Suppress("UnusedPrivateMember")
internal sealed class BaseAnalyticsEventRequest : JSONSerializable, JSONDeserializable {
    abstract val device: DeviceData?
    abstract val properties: BaseAnalyticsProperties
    abstract val appIdentifier: String?
    abstract val sdkSessionId: String
    abstract val sdkIntegrationType: SdkIntegrationType
    abstract val checkoutSessionId: String?
    abstract val clientSessionId: String?
    abstract val orderId: String?
    abstract val primerAccountId: String?
    abstract val analyticsUrl: String?
    abstract val eventType: AnalyticsEventType
    protected val createdAt: Long = System.currentTimeMillis()
    protected val sdkType: String = "ANDROID_NATIVE"
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

        @JvmField
        val serializer = object : JSONSerializer<BaseAnalyticsEventRequest> {
            override fun serialize(t: BaseAnalyticsEventRequest): JSONObject {
                return when (t.eventType) {
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
        }

        @JvmField
        val deserializer = object : JSONDeserializer<BaseAnalyticsEventRequest> {
            override fun deserialize(t: JSONObject): BaseAnalyticsEventRequest {
                return when (AnalyticsEventType.valueOf(t.getString(EVENT_TYPE_FIELD))) {
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
        }

        val baseSerializer = object : JSONSerializer<BaseAnalyticsEventRequest> {
            override fun serialize(t: BaseAnalyticsEventRequest): JSONObject {
                return JSONObject().apply {
                    putOpt(
                        DEVICE_FIELD,
                        t.device?.let {
                            JSONSerializationUtils.getSerializer<DeviceData>()
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
                    put(SDK_TYPE_FIELD, t.sdkType)
                    put(SDK_VERSION_FIELD, t.sdkVersion)
                    put(SDK_INTEGRATION_TYPE_FIELD, t.sdkIntegrationType.name)
                }
            }
        }
    }
}

internal abstract class BaseAnalyticsProperties : JSONSerializable, JSONDeserializable

internal fun BaseAnalyticsProperties.toAnalyticsEvent(
    batteryLevel: Int,
    batteryStatus: BatteryStatus,
    screenData: ScreenData,
    deviceId: String,
    appIdentifier: String,
    sdkSessionId: String,
    sdkIntegrationType: SdkIntegrationType,
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
        sdkSessionId,
        appIdentifier,
        sdkIntegrationType,
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
        checkoutSessionId,
        clientSessionId,
        orderId,
        primerAccountId,
        analyticsUrl
    )
    else -> throw IllegalStateException("Unsupported property params")
}

internal fun BaseAnalyticsParams.toAnalyticsEvent(
    batteryLevel: Int,
    batteryStatus: BatteryStatus,
    screenData: ScreenData,
    deviceId: String,
    appIdentifier: String,
    sdkSessionId: String,
    sdkIntegrationType: SdkIntegrationType,
    checkoutSessionId: String,
    clientSessionId: String?,
    orderId: String?,
    primerAccountId: String?,
    analyticsUrl: String?,
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
        TimerProperties(id, timerType),
        appIdentifier,
        sdkSessionId,
        sdkIntegrationType,
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
        MessageProperties(messageType, message, severity, diagnosticsId),
        appIdentifier,
        sdkSessionId,
        sdkIntegrationType,
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
        checkoutSessionId,
        clientSessionId,
        orderId,
        primerAccountId,
        analyticsUrl
    )
    else -> throw IllegalStateException("Unsupported event params")
}

internal fun BaseContextParams.toAnalyticsContext() = when (this) {
    is PaymentMethodContextParams -> AnalyticsContext(
        paymentMethodType = paymentMethodType
    )
    is BankIssuerContextParams -> AnalyticsContext(
        issuerId = issuerId
    )
    is PaymentInstrumentIdContextParams -> AnalyticsContext(
        paymentMethodId = id
    )
    is UrlContextParams -> AnalyticsContext(
        url = url
    )
    is DummyApmDecisionParams -> AnalyticsContext(
        decision = decision
    )
    else -> throw IllegalStateException("Unsupported event params")
}
