package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nativeUi.async.redirect.repository.AsyncPaymentMethodDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.AsyncPaymentMethodConfigInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.repository.AsyncPaymentMethodRepository
import io.primer.android.data.deeplink.ipay88.IPay88DeeplinkDataRepository
import io.primer.android.data.payments.status.datasource.RemoteAsyncPaymentMethodStatusDataSource
import io.primer.android.data.payments.status.repository.AsyncPaymentMethodStatusDataRepository
import io.primer.android.domain.deeplink.async.AsyncPaymentMethodDeeplinkInteractor
import io.primer.android.domain.deeplink.ipay88.repository.IPay88DeeplinkRepository
import io.primer.android.domain.payments.async.AsyncPaymentMethodInteractor
import io.primer.android.domain.payments.async.repository.AsyncPaymentMethodStatusRepository
import io.primer.android.presentation.payment.async.AsyncPaymentMethodViewModelFactory
import io.primer.android.viewmodel.TokenizationViewModelFactory

internal class AsyncPaymentMethodContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton {
            RemoteAsyncPaymentMethodStatusDataSource(
                sdk.resolve()
            )
        }
        registerSingleton<AsyncPaymentMethodStatusRepository> {
            AsyncPaymentMethodStatusDataRepository(
                resolve()
            )
        }

        registerSingleton<IPay88DeeplinkRepository> {
            IPay88DeeplinkDataRepository(
                sdk.resolve()
            )
        }

        registerSingleton {
            AsyncPaymentMethodInteractor(
                resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            AsyncPaymentMethodDeeplinkInteractor(sdk.resolve())
        }

        registerSingleton<AsyncPaymentMethodRepository> {
            AsyncPaymentMethodDataRepository(
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            AsyncPaymentMethodConfigInteractor(
                resolve(),
                sdk.resolve()
            )
        }

        registerFactory { AsyncPaymentMethodViewModelFactory(resolve(), sdk.resolve()) }

        registerFactory {
            TokenizationViewModelFactory(
                sdk.resolve(),
                sdk.resolve(),
                resolve()
            )
        }
    }
}
