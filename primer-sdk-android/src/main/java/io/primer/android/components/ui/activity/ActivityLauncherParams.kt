package io.primer.android.components.ui.activity

import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.State
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.ui.base.webview.WebViewClientType
import java.io.Serializable

internal sealed class ActivityLauncherParams(
    open val paymentMethodType: String,
    open val sessionIntent: PrimerSessionIntent,
) : Serializable

internal sealed class PaymentMethodRedirectLauncherParams(
    paymentMethodType: String,
    sessionIntent: PrimerSessionIntent
) : ActivityLauncherParams(paymentMethodType, sessionIntent)

internal data class KlarnaActivityLauncherParams(
    val webViewTitle: String,
    val clientToken: String,
    val redirectUrl: String,
    val paymentCategory: String,
    val errorCode: Int,
    override val sessionIntent: PrimerSessionIntent
) : PaymentMethodRedirectLauncherParams(
    PaymentMethodType.KLARNA.name,
    sessionIntent,
)

internal data class KlarnaMockActivityLauncherParams(
    val errorCode: Int,
    override val sessionIntent: PrimerSessionIntent
) : PaymentMethodRedirectLauncherParams(
    PaymentMethodType.KLARNA.name,
    sessionIntent,
)

internal data class WebRedirectActivityLauncherParams(
    val statusUrl: String,
    val paymentUrl: String,
    val title: String,
    override val paymentMethodType: String,
    val returnUrl: String,
    val webViewClientType: WebViewClientType,
) : PaymentMethodRedirectLauncherParams(
    paymentMethodType,
    PrimerSessionIntent.CHECKOUT,
)

internal data class ApayaActivityLauncherParams(
    val webViewTitle: String,
    val redirectUrl: String,
    val returnUrl: String,
    override val sessionIntent: PrimerSessionIntent
) : PaymentMethodRedirectLauncherParams(
    PaymentMethodType.APAYA.name,
    sessionIntent,
)

internal data class BrowserLauncherParams(
    val url: String,
    val host: String,
    override val paymentMethodType: String,
    override val sessionIntent: PrimerSessionIntent
) : PaymentMethodRedirectLauncherParams(
    paymentMethodType,
    sessionIntent,
)

internal class GooglePayActivityLauncherParams : PaymentMethodRedirectLauncherParams(
    PaymentMethodType.GOOGLE_PAY.name,
    PrimerSessionIntent.CHECKOUT,
)

internal class PaymentMethodLauncherParams(
    override val paymentMethodType: String,
    override val sessionIntent: PrimerSessionIntent,
    val initialState: State? = null
) : ActivityLauncherParams(
    paymentMethodType,
    sessionIntent,
)

internal class IPay88ActivityLauncherParams(
    val iPayPaymentId: String,
    val iPayMethod: Int,
    val merchantCode: String,
    val amount: String,
    val referenceNumber: String,
    val prodDesc: String,
    val currencyCode: String?,
    val countryCode: String?,
    val customerName: String?,
    val customerEmail: String?,
    val backendCallbackUrl: String,
    val deeplinkUrl: String,
    val errorCode: Int,
    override val paymentMethodType: String,
    override val sessionIntent: PrimerSessionIntent
) : PaymentMethodRedirectLauncherParams(paymentMethodType, sessionIntent)
