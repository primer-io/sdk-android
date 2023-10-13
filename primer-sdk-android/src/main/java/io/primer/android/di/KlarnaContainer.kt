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
    }
}
