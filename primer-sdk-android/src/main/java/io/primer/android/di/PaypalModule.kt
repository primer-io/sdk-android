package io.primer.android.di

import io.primer.android.data.payments.paypal.datasource.RemotePaypalOrderInfoDataSource
import io.primer.android.data.payments.paypal.repository.PaypalOrderInfoDataRepository
import io.primer.android.domain.payments.paypal.PaypalOrderInfoInteractor
import io.primer.android.domain.payments.paypal.repository.PaypalInfoRepository
import org.koin.dsl.module

internal val paypalModule = {
    module {
        single { RemotePaypalOrderInfoDataSource(get()) }
        single<PaypalInfoRepository> { PaypalOrderInfoDataRepository(get(), get()) }
        single { PaypalOrderInfoInteractor(get()) }
    }
}
