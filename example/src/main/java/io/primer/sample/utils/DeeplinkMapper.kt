package io.primer.sample.utils

import com.google.gson.GsonBuilder
import io.primer.android.data.settings.GooglePayButtonOptions
import io.primer.android.data.settings.PrimerDebugOptions
import io.primer.android.data.settings.PrimerGooglePayOptions
import io.primer.android.data.settings.PrimerGoogleShippingAddressParameters
import io.primer.android.data.settings.PrimerKlarnaOptions
import io.primer.android.data.settings.PrimerPaymentMethodOptions
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.PrimerStripeOptions
import io.primer.android.data.settings.PrimerThreeDsOptions
import io.primer.android.ui.settings.PrimerUIOptions
import io.primer.sample.datamodels.DeeplinkSettings
import java.util.Base64

object DeeplinkMapper {

    private val gson = GsonBuilder()
        .registerTypeAdapter(
            PrimerStripeOptions.MandateData::class.java,
            DeeplinkSettings.PaymentMethodOptions.StripeOptions.MandateDataTypeAdapter()
        )
        .create()

    private val base64 = Base64.getDecoder()

    private fun parseDeeplink(deeplink: String?) = try {
        val json = String(base64.decode(deeplink))
        gson.fromJson(json, DeeplinkSettings::class.java)
    } catch (e: Exception) {
        null
    }

    fun PrimerSettings.overrideWith(deeplink: String?): PrimerSettings =
        parseDeeplink(deeplink)?.let {
            copy(
                paymentHandling = it.paymentHandling ?: paymentHandling,
                paymentMethodOptions = paymentMethodOptions.override(it.paymentMethodOptions),
                uiOptions = uiOptions.override(it.uiOptions),
                debugOptions = debugOptions.override(it.debugOptions),
                clientSessionCachingEnabled = it.clientSessionCachingEnabled ?: clientSessionCachingEnabled,
                apiVersion = it.apiVersion ?: apiVersion
            )
        } ?: run {
            this
        }

    private fun PrimerPaymentMethodOptions.override(
        deeplink: DeeplinkSettings.PaymentMethodOptions?
    ) = PrimerPaymentMethodOptions(
        redirectScheme = deeplink?.android?.redirectScheme ?: redirectScheme,
        klarnaOptions = klarnaOptions.override(deeplink?.klarnaOptions),
        googlePayOptions = googlePayOptions.override(deeplink?.googlePayOptions),
        threeDsOptions = threeDsOptions.override(deeplink?.threeDsOptions),
        stripeOptions = stripeOptions.override(deeplink?.stripeOptions)
    )

    private fun PrimerKlarnaOptions.override(
        deeplink: DeeplinkSettings.PaymentMethodOptions.KlarnaOptions?
    ) = PrimerKlarnaOptions(
        recurringPaymentDescription = deeplink?.recurringPaymentDescription ?: recurringPaymentDescription,
        returnIntentUrl = deeplink?.returnIntentUrl ?: returnIntentUrl
    )

    private fun PrimerThreeDsOptions.override(
        deeplink: DeeplinkSettings.PaymentMethodOptions.ThreeDsOptions?
    ) = PrimerThreeDsOptions(
        threeDsAppRequestorUrl = deeplink?.android?.threeDsAppRequestorUrl ?: threeDsAppRequestorUrl
    )

    private fun PrimerStripeOptions.override(
        deeplink: DeeplinkSettings.PaymentMethodOptions.StripeOptions?
    ) = PrimerStripeOptions(
        mandateData = mandateData.override(deeplink?.mandateData),
        publishableKey = deeplink?.publishableKey ?: publishableKey
    )

    fun PrimerStripeOptions.MandateData?.override(
        deeplink: PrimerStripeOptions.MandateData?
    ) = when (deeplink) {
        is PrimerStripeOptions.MandateData.TemplateMandateData ->
            PrimerStripeOptions.MandateData.TemplateMandateData(deeplink.merchantName)
        is PrimerStripeOptions.MandateData.FullMandateStringData ->
            PrimerStripeOptions.MandateData.FullMandateStringData(deeplink.value)
        is PrimerStripeOptions.MandateData.FullMandateData ->
            PrimerStripeOptions.MandateData.FullMandateData(deeplink.value)
        null -> this
    }

    private fun PrimerGooglePayOptions.override(
        deeplink: DeeplinkSettings.PaymentMethodOptions.GooglePayOptions?
    ) = PrimerGooglePayOptions(
        merchantName = deeplink?.merchantName ?: merchantName,
        allowedCardNetworks = deeplink?.allowedCardNetworks ?: allowedCardNetworks,
        buttonStyle = deeplink?.buttonStyle ?: buttonStyle,
        captureBillingAddress = deeplink?.captureBillingAddress ?: captureBillingAddress,
        existingPaymentMethodRequired = deeplink?.existingPaymentMethodRequired ?: existingPaymentMethodRequired,
        shippingAddressParameters = shippingAddressParameters?.override(deeplink?.shippingAddressParameters),
        requireShippingMethod = deeplink?.requireShippingMethod ?: requireShippingMethod,
        emailAddressRequired = deeplink?.emailAddressRequired ?: emailAddressRequired,
        buttonOptions = buttonOptions.override(deeplink?.buttonOptions)
    )

    private fun PrimerGoogleShippingAddressParameters.override(
        deeplink: DeeplinkSettings.PaymentMethodOptions.GooglePayOptions.GoogleShippingAddressParameters?
    ) = PrimerGoogleShippingAddressParameters(
        phoneNumberRequired = deeplink?.phoneNumberRequired ?: phoneNumberRequired
    )

    private fun GooglePayButtonOptions.override(
        deeplink: DeeplinkSettings.PaymentMethodOptions.GooglePayOptions.GooglePayButtonOptions?
    ) = GooglePayButtonOptions(
        buttonTheme = deeplink?.buttonTheme ?: buttonTheme,
        buttonType = deeplink?.buttonType ?: buttonType
    )

    private fun PrimerUIOptions.override(
        deeplink: DeeplinkSettings.UiOptions?
    ) = PrimerUIOptions(
        isInitScreenEnabled = deeplink?.isInitScreenEnabled ?: isInitScreenEnabled,
        isSuccessScreenEnabled = deeplink?.isSuccessScreenEnabled ?: isSuccessScreenEnabled,
        isErrorScreenEnabled = deeplink?.isErrorScreenEnabled ?: isErrorScreenEnabled,
        dismissalMechanism = deeplink?.dismissalMechanism ?: dismissalMechanism
    )

    private fun PrimerDebugOptions.override(
        deeplink: DeeplinkSettings.DebugOptions?
    ) = PrimerDebugOptions(
        is3DSSanityCheckEnabled = deeplink?.is3DSSanityCheckEnabled ?: is3DSSanityCheckEnabled
    )
}
