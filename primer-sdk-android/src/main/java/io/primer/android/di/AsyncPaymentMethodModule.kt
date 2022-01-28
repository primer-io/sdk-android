package io.primer.android.di

import io.primer.android.data.payments.async.datasource.RemoteAsyncPaymentMethodStatusDataSource
import io.primer.android.data.payments.async.repository.AsyncPaymentMethodStatusDataRepository
import io.primer.android.domain.payments.async.AsyncPaymentMethodInteractor
import io.primer.android.domain.payments.async.repository.AsyncPaymentMethodStatusRepository
import io.primer.android.presentation.payment.async.AsyncPaymentMethodViewModel
import org.koin.android.viewmodel.dsl.viewModel
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
        single {
            AsyncPaymentMethodInteractor(
                get(),
                get(),
                get(),
                get()
            )
        }

        viewModel { AsyncPaymentMethodViewModel(get()) }
    }
}
