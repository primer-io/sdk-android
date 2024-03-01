package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteFinalizeKlarnaSessionDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteKlarnaCheckoutPaymentSessionDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteKlarnaCustomerTokenDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteKlarnaVaultPaymentSessionDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.error.KlarnaErrorMapper
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.repository.FinalizeKlarnaSessionDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.repository.KlarnaCustomerTokenDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.repository.KlarnaSessionDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.FinalizeKlarnaSessionInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaCustomerTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaSessionInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.FinalizeKlarnaSessionRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaCustomerTokenRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaSessionRepository
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.GetKlarnaAuthorizationSessionDataDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.GetKlarnaDeeplinkDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.KlarnaSessionCreationDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.KlarnaTokenizationDelegate
import io.primer.android.data.deeplink.klarna.KlarnaDeeplinkDataRepository
import io.primer.android.domain.deeplink.klarna.KlarnaDeeplinkInteractor
import io.primer.android.domain.deeplink.klarna.repository.KlarnaDeeplinkRepository
import io.primer.android.domain.error.ErrorMapper

internal class KlarnaContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { RemoteKlarnaCheckoutPaymentSessionDataSource(sdk.resolve()) }

        registerSingleton { RemoteKlarnaVaultPaymentSessionDataSource(sdk.resolve()) }

        registerSingleton { RemoteKlarnaCustomerTokenDataSource(sdk.resolve()) }

        registerSingleton { RemoteFinalizeKlarnaSessionDataSource(sdk.resolve()) }

        registerSingleton<KlarnaSessionRepository> {
            KlarnaSessionDataRepository(
                klarnaCheckoutPaymentSessionDataSource = resolve(),
                klarnaVaultPaymentSessionDataSource = sdk.resolve(),
                localConfigurationDataSource = sdk.resolve(),
                config = sdk.resolve()
            )
        }

        registerSingleton<KlarnaCustomerTokenRepository> {
            KlarnaCustomerTokenDataRepository(resolve(), sdk.resolve(), sdk.resolve())
        }

        registerSingleton<FinalizeKlarnaSessionRepository> {
            FinalizeKlarnaSessionDataRepository(resolve(), sdk.resolve())
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
            FinalizeKlarnaSessionInteractor(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            KlarnaTokenizationDelegate(
                klarnaCustomerTokenInteractor = resolve(),
                finalizeKlarnaSessionInteractor = resolve(),
                tokenizationInteractor = sdk.resolve()
            )
        }

        registerSingleton {
            KlarnaSessionCreationDelegate(
                actionInteractor = sdk.resolve(),
                interactor = sdk.resolve(),
                primerSettings = sdk.resolve(),
                configurationInteractor = sdk.resolve()
            )
        }

        registerSingleton {
            GetKlarnaDeeplinkDelegate(
                interactor = sdk.resolve()
            )
        }

        registerSingleton {
            GetKlarnaAuthorizationSessionDataDelegate(
                configurationRepository = sdk.resolve()
            )
        }
    }

    internal companion object {

        const val KLARNA_ERROR_RESOLVER_NAME = "klarnaErrorResolver"
    }
}
