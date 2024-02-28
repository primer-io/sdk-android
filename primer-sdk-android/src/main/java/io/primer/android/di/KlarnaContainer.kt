package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteKlarnaCustomerTokenDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteKlarnaSessionDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.error.KlarnaErrorMapper
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.repository.KlarnaCustomerTokenDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.repository.KlarnaSessionDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaCustomerTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaSessionInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaCustomerTokenRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaSessionRepository
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.GetKlarnaDeeplinkDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.KlarnaSessionCreationDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.KlarnaTokenizationDelegate
import io.primer.android.data.deeplink.klarna.KlarnaDeeplinkDataRepository
import io.primer.android.domain.deeplink.klarna.KlarnaDeeplinkInteractor
import io.primer.android.domain.deeplink.klarna.repository.KlarnaDeeplinkRepository
import io.primer.android.domain.error.ErrorMapper

internal class KlarnaContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { RemoteKlarnaSessionDataSource(sdk.resolve()) }

        registerSingleton { RemoteKlarnaCustomerTokenDataSource(sdk.resolve()) }

        registerSingleton<KlarnaSessionRepository> {
            KlarnaSessionDataRepository(
                resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton<KlarnaCustomerTokenRepository> {
            KlarnaCustomerTokenDataRepository(resolve(), sdk.resolve(), sdk.resolve())
        }

        registerFactory<ErrorMapper>(KLARNA_ERROR_RESOLVER_NAME) {
            KlarnaErrorMapper()
        }

        registerSingleton<KlarnaDeeplinkRepository> { KlarnaDeeplinkDataRepository(sdk.resolve()) }

        registerSingleton { KlarnaDeeplinkInteractor(resolve()) }

        registerSingleton {
            KlarnaSessionInteractor(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            KlarnaCustomerTokenInteractor(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            KlarnaTokenizationDelegate(
                actionInteractor = sdk.resolve(),
                klarnaCustomerTokenInteractor = resolve(),
                tokenizationInteractor = sdk.resolve(),
                primerConfig = sdk.resolve()
            )
        }

        registerSingleton {
            KlarnaSessionCreationDelegate(
                interactor = sdk.resolve()
            )
        }

        registerSingleton {
            GetKlarnaDeeplinkDelegate(
                interactor = sdk.resolve()
            )
        }
    }

    internal companion object {

        const val KLARNA_ERROR_RESOLVER_NAME = "klarnaErrorResolver"
    }
}
