package io.primer.android.di

import io.primer.android.PaymentMethod
import io.primer.android.model.APIClient
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.ClientToken
import org.koin.dsl.module

internal val CheckoutConfigModule = { config: CheckoutConfig, pms: List<PaymentMethod> ->
  module {
    single { config }
    single { pms }
    single { config.theme }
    single { ClientToken.fromString(get<CheckoutConfig>().clientToken) }
    single { APIClient(get()) }
  }
}