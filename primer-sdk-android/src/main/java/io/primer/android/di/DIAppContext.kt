package io.primer.android.di

import android.content.Context
import io.primer.android.analytics.di.analyticsModule
import io.primer.android.components.di.componentsModule
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientToken
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

internal object DIAppContext {

    var app: KoinApplication? = null

    fun init(context: Context, config: PrimerConfig) {
        app = koinApplication {
            androidContext(context)
            modules(
                CheckoutConfigModule(
                    config,
                    ClientToken.fromString(config.clientTokenBase64.orEmpty())
                ),
                PaymentMethodsModule(),
                countriesModule(),
                formsModule(),
                apayaModule(),
                klarnaModule(),
                retailOutletsModule(),
                asyncPaymentMethodModule(),
                tokenizationModule(),
                rpcModule(),
                paypalModule(),
                googlePayModule(),
                dummyApmModule(),
                bancontactApmModule(),
                NetworkModule(),
                imageLoaderModule(),
                PaymentsModule(),
                analyticsModule(),
                componentsModule(),
                errorResolverModule()
            )
        }
    }
}
