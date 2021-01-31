package io.primer.android.di

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.CheckoutConfig
import org.koin.dsl.module

internal val CheckoutConfigModule = { config: CheckoutConfig, pms: List<PaymentMethod> ->
  module {
    single { config }
    single { pms }
  }
}