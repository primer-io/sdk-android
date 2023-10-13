package io.primer.android.di

import android.content.Context
import io.primer.android.analytics.di.AnalyticsContainer
import io.primer.android.components.di.ComponentsContainer
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientToken

internal object DISdkContext {
    var sdkContainer: SdkContainer? = null

    fun init(config: PrimerConfig, context: Context) {
        sdkContainer = SdkContainer().apply {
            registerContainer(
                SharedContainer(
                    context,
                    config,
                    ClientToken.fromString(config.clientTokenBase64.orEmpty())
                )
            )

            registerContainer(ImageLoaderContainer(this))

            registerContainer(NetworkContainer(this))

            registerContainer(AnalyticsContainer(this))

            registerContainer(ErrorResolverContainer(this))

            registerContainer(CheckoutConfigContainer(this))

            registerContainer(PaymentMethodsMockContainer(this))

            registerContainer(RetailOutletsContainer(this))

            registerContainer(PaymentMethodDescriptorContainer(this))

            registerContainer(ResumeEventContainer(this))

            registerContainer(TokenizationContainer(this))

            registerContainer(PaymentMethodsContainer(this))

            registerContainer(PaymentsContainer(this))

            registerContainer(AsyncPaymentMethodContainer(this))

            registerContainer(ApayaContainer(this))

            registerContainer(KlarnaContainer(this))

            registerContainer(GooglePayContainer(this))

            registerContainer(BancontactApmContainer(this))

            registerContainer(DummyApmContainer(this))

            registerContainer(CountriesDataStorageContainer(this))

            registerContainer(FormsContainer(this))

            registerContainer(IPay88Container(this))

            registerContainer(PaypalContainer(this))

            registerContainer(ComponentsContainer(this))
        }
    }
}
