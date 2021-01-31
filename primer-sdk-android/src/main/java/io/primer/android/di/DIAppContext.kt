package io.primer.android.di

import android.content.Context
import io.primer.android.PaymentMethod
import io.primer.android.model.dto.CheckoutConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

internal object DIAppContext {
  var app: KoinApplication? = null

  fun init(context: Context, config: CheckoutConfig, paymentMethods: List<PaymentMethod>) {
    app = koinApplication {
      androidContext(context)
      modules(
        CheckoutConfigModule(config, paymentMethods)
      )
    }
  }
}