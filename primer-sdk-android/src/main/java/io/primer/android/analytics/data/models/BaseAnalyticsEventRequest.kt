package io.primer.android.analytics.data.models

import io.primer.android.BuildConfig
import io.primer.android.analytics.domain.models.BankIssuerContextParams
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.models.PaymentInstrumentIdContextParams
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.models.TimerAnalyticsParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.analytics.domain.models.UrlContextParams
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
@Suppress("UnusedPrivateMember")
internal sealed class BaseAnalyticsEventRequest {
    abstract val device: DeviceData?
    abstract val properties: BaseAnalyticsProperties
    abstract val appIdentifier: String?
    abstract val sdkSessionId: String
    abstract val checkoutSessionId: String?
    abstract val clientSessionId: String?
    abstract val orderId: String?
    abstract val primerAccountId: String?
    abstract val analyticsUrl: String?
    abstract val eventType: AnalyticsEventType
    @EncodeDefault val createdAt: Long = System.currentTimeMillis()
    @EncodeDefault protected val sdkType: String = "Android"
    @EncodeDefault protected val sdkVersion: String = BuildConfig.SDK_VERSION_STRING

    abstract fun copy(newAnalyticsUrl: String?): BaseAnalyticsEventRequest
}

internal abstract class BaseAnalyticsProperties

internal fun BaseAnalyticsProperties.toAnalyticsEvent(
    batteryLevel: Int,
    batteryStatus: BatteryStatus,
    screenData: ScreenData,
    deviceId: String,
    appIdentifier: String,
    sdkSessionId: String,
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
    else -> throw IllegalStateException("Unsupported event params")
}
