package io.primer.android.di

import io.primer.android.data.configuration.datasource.RemoteConfigurationDataSource
import io.primer.android.data.configuration.datasource.RemoteConfigurationResourcesDataSource
import io.primer.android.data.configuration.repository.ConfigurationDataRepository
import io.primer.android.data.payments.create.datasource.LocalPaymentDataSource
import io.primer.android.data.payments.create.repository.PaymentResultDataRepository
import io.primer.android.data.payments.displayMetadata.repository.PaymentMethodImplementationDataRepository
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.repository.ClientTokenDataRepository
import io.primer.android.data.token.repository.ValidateTokenDataRepository
import io.primer.android.data.token.validation.ValidationTokenDataSource
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.domain.payments.displayMetadata.PaymentMethodsImplementationInteractor
import io.primer.android.domain.payments.displayMetadata.repository.PaymentMethodImplementationRepository
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.domain.session.repository.ConfigurationRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.token.repository.ValidateTokenRepository
import io.primer.android.infrastructure.files.ImagesFileProvider
import io.primer.android.presentation.base.BaseViewModelFactory
import io.primer.android.threeds.data.datasource.Remote3DSAuthDataSource
import io.primer.android.threeds.data.repository.PaymentMethodDataRepository
import io.primer.android.threeds.data.repository.ThreeDsDataRepository
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.domain.respository.ThreeDsRepository
import io.primer.android.threeds.helpers.ThreeDsLibraryVersionValidator
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator

internal class CheckoutConfigContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<PaymentMethodRepository> { PaymentMethodDataRepository() }

        registerSingleton { LocalPaymentDataSource() }

        registerSingleton<PaymentResultRepository> { PaymentResultDataRepository(resolve()) }

        registerSingleton { sdk.resolve<PrimerConfig>().settings.uiOptions.theme }

        registerSingleton { ValidationTokenDataSource(sdk.resolve()) }

        registerSingleton<ClientTokenRepository> { ClientTokenDataRepository(sdk.resolve()) }

        registerSingleton {
            RemoteConfigurationDataSource(sdk.resolve())
        }

        registerSingleton {
            RemoteConfigurationResourcesDataSource(
                sdk.resolve(ImageLoaderContainer.IMAGE_LOADING_CLIENT_NAME),
                ImagesFileProvider(sdk.resolve()),
                sdk.resolve()
            )
        }

        registerSingleton<ConfigurationRepository> {
            ConfigurationDataRepository(
                resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton<ValidateTokenRepository> {
            ValidateTokenDataRepository(
                sdk.resolve(),
                resolve()
            )
        }

        registerSingleton {
            ConfigurationInteractor(
                resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton<PaymentMethodImplementationRepository> {
            PaymentMethodImplementationDataRepository(sdk.resolve())
        }

        registerSingleton {
            PaymentMethodsImplementationInteractor(resolve(), sdk.resolve())
        }

        registerFactory {
            BaseViewModelFactory(sdk.resolve())
        }

        registerSingleton { Remote3DSAuthDataSource(sdk.resolve()) }

        registerSingleton<ThreeDsRepository> { ThreeDsDataRepository(resolve(), sdk.resolve()) }

        registerSingleton { ThreeDsSdkClassValidator() }

        registerSingleton { ThreeDsLibraryVersionValidator(resolve()) }
    }
}
