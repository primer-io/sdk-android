package io.primer.android.di

import android.content.Context
import io.primer.android.data.token.model.ClientToken
import io.primer.android.model.dto.PrimerConfig
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
                apayaModule(),
                asyncPaymentMethodModule(),
                tokenizationModule(),
                NetworkModule()
            )
        }
    }
}
