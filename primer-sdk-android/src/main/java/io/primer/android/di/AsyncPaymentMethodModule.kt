package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nativeUi.async.redirect.repository.AsyncPaymentMethodDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.AsyncPaymentMethodConfigInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.repository.AsyncPaymentMethodRepository
import io.primer.android.data.deeplink.async.AsyncPaymentMethodDeeplinkDataRepository
import io.primer.android.data.payments.status.datasource.RemoteAsyncPaymentMethodStatusDataSource
import io.primer.android.data.payments.status.repository.AsyncPaymentMethodStatusDataRepository
import io.primer.android.domain.deeplink.async.AsyncPaymentMethodDeeplinkInteractor
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.domain.payments.async.AsyncPaymentMethodInteractor
import io.primer.android.domain.payments.async.repository.AsyncPaymentMethodStatusRepository
import io.primer.android.presentation.payment.async.AsyncPaymentMethodViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val asyncPaymentMethodModule = {
    module {
        single {
            RemoteAsyncPaymentMethodStatusDataSource(
                get(),
            )
        }
        single<AsyncPaymentMethodStatusRepository> {
            AsyncPaymentMethodStatusDataRepository(
                get(),
            )
        }
        single<AsyncPaymentMethodDeeplinkRepository> {
            AsyncPaymentMethodDeeplinkDataRepository(
                get(),
            )
        }
        single {
            AsyncPaymentMethodInteractor(
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }
        single {
            AsyncPaymentMethodDeeplinkInteractor(get(),)
        }

        single<AsyncPaymentMethodRepository> { AsyncPaymentMethodDataRepository(get(), get()) }
        single {
            AsyncPaymentMethodConfigInteractor(
                get(),
                get()
            )
        }

        viewModel { AsyncPaymentMethodViewModel(get(), get()) }
    }
}
