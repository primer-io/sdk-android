package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteKlarnaCustomerTokenDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteKlarnaSessionDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.repository.KlarnaCustomerTokenDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.repository.KlarnaSessionDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaCustomerTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaSessionInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaCustomerTokenRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaSessionRepository
import io.primer.android.data.deeplink.klarna.KlarnaDeeplinkDataRepository
import io.primer.android.domain.deeplink.klarna.KlarnaDeeplinkInteractor
import io.primer.android.domain.deeplink.klarna.repository.KlarnaDeeplinkRepository
import org.koin.dsl.module

internal val klarnaModule = {
    module {
        single { RemoteKlarnaSessionDataSource(get()) }
        single { RemoteKlarnaCustomerTokenDataSource(get()) }
        single<KlarnaSessionRepository> { KlarnaSessionDataRepository(get(), get(), get()) }
        single<KlarnaCustomerTokenRepository> {
            KlarnaCustomerTokenDataRepository(get(), get(), get())
        }
        single<KlarnaDeeplinkRepository> { KlarnaDeeplinkDataRepository(get()) }
        single { KlarnaDeeplinkInteractor(get()) }
        single {
            KlarnaSessionInteractor(
                get(),
                get()
            )
        }
        single {
            KlarnaCustomerTokenInteractor(
                get(),
                get()
            )
        }
    }
}
