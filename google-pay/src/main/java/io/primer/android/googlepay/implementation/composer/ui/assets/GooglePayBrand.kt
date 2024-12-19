package io.primer.android.googlepay.implementation.composer.ui.assets

import com.google.android.gms.wallet.button.ButtonOptions
import com.google.android.gms.wallet.button.PayButton
import io.primer.android.data.settings.GooglePayButtonStyle
import io.primer.android.assets.ui.model.Brand
import io.primer.android.assets.ui.model.ViewProvider
import io.primer.android.data.settings.PrimerGooglePayOptions
import io.primer.android.googlepay.R
import io.primer.android.googlepay.implementation.configuration.domain.GooglePayConfigurationRepository
import io.primer.android.googlepay.implementation.utils.GooglePayPayloadUtils
import io.primer.android.paymentmethods.core.configuration.domain.model.NoOpPaymentMethodConfigurationParams
import org.json.JSONArray

internal class GooglePayBrand(
    private val options: PrimerGooglePayOptions,
    private val configurationRepository: GooglePayConfigurationRepository
) : Brand {

    override val iconResId: Int
        get() = R.drawable.ic_logo_googlepay

    override val logoResId: Int
        get() = when (options.buttonStyle) {
            GooglePayButtonStyle.BLACK ->
                R.drawable.ic_logo_google_pay_black_square

            GooglePayButtonStyle.WHITE ->
                R.drawable.ic_logo_google_pay_square
        }

    override val iconLightResId: Int
        get() = R.drawable.ic_logo_googlepay_light

    override fun viewProvider(): ViewProvider {
        return { context ->
            val payButton = PayButton(context)
            val configuration =
                configurationRepository.getPaymentMethodConfiguration(NoOpPaymentMethodConfigurationParams).getOrThrow()
            val paymentMethods: JSONArray =
                JSONArray().put(
                    GooglePayPayloadUtils.baseCardPaymentMethod(
                        allowedCardNetworks = configuration.allowedCardNetworks,
                        allowedCardAuthMethods = configuration.allowedCardAuthMethods,
                        billingAddressRequired = options.captureBillingAddress
                    )
                )
            payButton.apply {
                initialize(
                    ButtonOptions.newBuilder()
                        .setButtonTheme(options.buttonOptions.buttonTheme)
                        .setButtonType(options.buttonOptions.buttonType)
                        .setCornerRadius(DEFAULT_CORNER_RADIUS)
                        .setAllowedPaymentMethods(paymentMethods.toString())
                        .build()
                )
            }
        }
    }

    private companion object {
        const val DEFAULT_CORNER_RADIUS = 4
    }
}
