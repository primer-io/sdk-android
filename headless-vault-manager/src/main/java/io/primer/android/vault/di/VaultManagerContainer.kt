package io.primer.android.vault.di

import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.vault.implementation.errors.data.mapper.VaultErrorMapper
import io.primer.android.vault.implementation.vaultedMethods.data.datasource.LocalVaultedPaymentMethodsDataSource
import io.primer.android.vault.implementation.vaultedMethods.data.datasource.RemoteVaultedPaymentMethodDeleteDataSource
import io.primer.android.vault.implementation.vaultedMethods.data.datasource.RemoteVaultedPaymentMethodsDataSource
import io.primer.android.vault.implementation.vaultedMethods.data.datasource.VaultedPaymentMethodExchangeDataSourceRegistry
import io.primer.android.vault.implementation.vaultedMethods.data.mapping.VaultedPaymentMethodAdditionalDataMapperRegistry
import io.primer.android.vault.implementation.vaultedMethods.data.repository.VaultedPaymentMethodExchangeDataRepository
import io.primer.android.vault.implementation.vaultedMethods.data.repository.VaultedPaymentMethodsDataRepository
import io.primer.android.vault.implementation.vaultedMethods.domain.FetchVaultedPaymentMethodsInteractor
import io.primer.android.vault.implementation.vaultedMethods.domain.FindVaultedPaymentMethodInteractor
import io.primer.android.vault.implementation.vaultedMethods.domain.VaultedPaymentMethodsDeleteInteractor
import io.primer.android.vault.implementation.vaultedMethods.domain.VaultedPaymentMethodsExchangeInteractor
import io.primer.android.vault.implementation.vaultedMethods.domain.repository.VaultedPaymentMethodExchangeRepository
import io.primer.android.vault.implementation.vaultedMethods.domain.repository.VaultedPaymentMethodsRepository
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.additionalData.VaultedPaymentMethodAdditionalDataValidatorRegistry
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.resolvers.VaultManagerInitValidationRulesResolver
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.rules.SdkInitializedRule
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.rules.ValidClientSessionCustomerIdRule
import io.primer.android.vault.implementation.vaultedMethods.presentation.delegate.DefaultVaultedPaymentDelegate
import io.primer.android.vault.implementation.vaultedMethods.presentation.delegate.VaultManagerComposerDelegate
import io.primer.android.vault.implementation.vaultedMethods.presentation.delegate.VaultManagerDelegate

class VaultManagerContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerFactory { SdkInitializedRule(configurationRepository = sdk().resolve()) }

        registerFactory { ValidClientSessionCustomerIdRule(configurationRepository = sdk().resolve()) }

        registerFactory {
            VaultManagerInitValidationRulesResolver(
                sdkInitializedRule = resolve(),
                validClientSessionCustomerIdRule = resolve()
            )
        }

        registerSingleton { RemoteVaultedPaymentMethodsDataSource(primerHttpClient = sdk().resolve()) }

        registerFactory { LocalVaultedPaymentMethodsDataSource() }

        registerSingleton { RemoteVaultedPaymentMethodDeleteDataSource(primerHttpClient = sdk().resolve()) }

        registerSingleton { VaultedPaymentMethodExchangeDataSourceRegistry(httpClient = sdk().resolve()) }

        registerFactory {
            FetchVaultedPaymentMethodsInteractor(
                vaultedPaymentMethodsRepository = sdk().resolve()
            )
        }

        registerFactory {
            FindVaultedPaymentMethodInteractor(
                vaultedPaymentMethodsRepository = sdk().resolve()
            )
        }

        registerFactory {
            VaultedPaymentMethodAdditionalDataValidatorRegistry()
        }

        registerSingleton { VaultedPaymentMethodAdditionalDataMapperRegistry() }

        registerSingleton<VaultedPaymentMethodExchangeRepository> {
            VaultedPaymentMethodExchangeDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                vaultedPaymentMethodExchangeDataSourceRegistry = resolve(),
                vaultedPaymentMethodAdditionalDataMapperRegistry = resolve()
            )
        }

        registerSingleton<VaultedPaymentMethodsRepository> {
            VaultedPaymentMethodsDataRepository(
                remoteVaultedPaymentMethodsDataSource = resolve(),
                localVaultedPaymentMethodsDataSource = resolve(),
                vaultedPaymentMethodDeleteDataSource = resolve(),
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY)
            )
        }

        registerSingleton {
            VaultedPaymentMethodsDeleteInteractor(
                vaultedPaymentMethodsRepository = resolve(),
                logReporter = sdk().resolve()
            )
        }

        registerSingleton {
            VaultedPaymentMethodsExchangeInteractor(
                vaultedPaymentMethodExchangeRepository = resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                preTokenizationHandler = sdk().resolve(),
                logReporter = sdk().resolve()
            )
        }

        registerFactory<PaymentMethodPaymentDelegate>(name = DEFAULT_COMPOSER_DI_KEY) {
            DefaultVaultedPaymentDelegate(
                paymentMethodTokenHandler = sdk().resolve(),
                resumePaymentHandler = sdk().resolve(),
                successHandler = sdk().resolve(),
                errorHandler = sdk().resolve(),
                baseErrorResolver = sdk().resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve()
            )
        }

        registerSingleton {
            VaultManagerComposerDelegate(
                paymentMethodNavigationFactoryRegistry = sdk().resolve(),
                composerRegistry = sdk().resolve(),
                providerFactoryRegistry = sdk().resolve(),
                context = sdk().resolve(),
                paymentDelegateProvider = { paymentMethod: String? ->
                    paymentMethod?.let {
                        runCatching { sdk().resolve<PaymentMethodPaymentDelegate>(it) }.getOrNull()
                    } ?: sdk().resolve(DEFAULT_COMPOSER_DI_KEY)
                }
            )
        }

        registerSingleton {
            VaultManagerDelegate(
                initValidationRulesResolver = resolve(),
                vaultedPaymentMethodsInteractor = resolve(),
                vaultedPaymentMethodsDeleteInteractor = sdk().resolve(),
                vaultedPaymentMethodsExchangeInteractor = resolve(),
                findVaultedPaymentMethodInteractor = resolve(),
                analyticsInteractor = sdk().resolve(),
                errorMapperRegistry = sdk().resolve(),
                vaultedPaymentMethodAdditionalDataValidatorRegistry = sdk().resolve()
            )
        }

        sdk().resolve<ErrorMapperRegistry>().register(VaultErrorMapper())
    }

    private companion object {

        const val DEFAULT_COMPOSER_DI_KEY = "DEFAULT_COMPOSER"
    }
}
