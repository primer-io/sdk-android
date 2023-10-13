package io.primer.android.di

import io.primer.android.data.action.datasource.RemoteActionDataSource
import io.primer.android.data.action.repository.ActionDataRepository
import io.primer.android.data.payments.methods.datasource.LocalVaultedPaymentMethodsDataSource
import io.primer.android.data.payments.methods.datasource.RemoteVaultedPaymentMethodDeleteDataSource
import io.primer.android.data.payments.methods.datasource.RemoteVaultedPaymentMethodsDataSource
import io.primer.android.data.payments.methods.datasource.VaultedPaymentMethodExchangeDataSourceRegistry
import io.primer.android.data.payments.methods.mapping.vault.VaultedPaymentMethodAdditionalDataMapperRegistry
import io.primer.android.data.payments.methods.repository.VaultedPaymentMethodExchangeDataRepository
import io.primer.android.data.payments.methods.repository.VaultedPaymentMethodsDataRepository
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.repository.ActionRepository
import io.primer.android.domain.action.validator.ActionUpdateFilter
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsDeleteInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsExchangeInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsInteractor
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodExchangeRepository
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.logging.DefaultLogger
import io.primer.android.logging.Logger
import io.primer.android.viewmodel.PrimerPaymentMethodCheckerRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal class PaymentMethodsContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<Logger>(PRIMER_VIEW_MODEL_HANDLER_LOGGER_NAME) {
            DefaultLogger(
                PRIMER_VIEW_MODEL_HANDLER_LOGGER_NAME
            )
        }

        registerSingleton<Logger>(VAULT_HANDLER_LOGGER_NAME) {
            DefaultLogger(VAULT_HANDLER_LOGGER_NAME)
        }

        registerSingleton { PrimerPaymentMethodCheckerRegistry }

        registerSingleton {
            PaymentMethodModulesInteractor(
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                resolve(PRIMER_VIEW_MODEL_HANDLER_LOGGER_NAME)
            )
        }

        registerSingleton { RemoteVaultedPaymentMethodsDataSource(sdk.resolve()) }

        registerFactory { LocalVaultedPaymentMethodsDataSource() }

        registerSingleton { RemoteVaultedPaymentMethodDeleteDataSource(sdk.resolve()) }

        registerSingleton { VaultedPaymentMethodExchangeDataSourceRegistry(sdk.resolve()) }

        registerSingleton { VaultedPaymentMethodAdditionalDataMapperRegistry() }

        registerSingleton<VaultedPaymentMethodExchangeRepository> {
            VaultedPaymentMethodExchangeDataRepository(
                sdk.resolve(),
                resolve(),
                resolve()
            )
        }

        registerSingleton<VaultedPaymentMethodsRepository> {
            VaultedPaymentMethodsDataRepository(
                resolve(),
                resolve(),
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton { VaultedPaymentMethodsInteractor(resolve(), sdk.resolve()) }

        registerSingleton {
            VaultedPaymentMethodsDeleteInteractor(
                resolve(),
                resolve(VAULT_HANDLER_LOGGER_NAME)
            )
        }

        registerSingleton {
            VaultedPaymentMethodsExchangeInteractor(
                resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton { RemoteActionDataSource(sdk.resolve()) }

        registerSingleton<ActionRepository> { ActionDataRepository(sdk.resolve(), resolve()) }

        registerFactory { ActionUpdateFilter(sdk.resolve(), sdk.resolve()) }

        registerSingleton {
            ActionInteractor(
                resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }
    }

    companion object {
        private const val PRIMER_VIEW_MODEL_HANDLER_LOGGER_NAME = "PRIMER_VIEW_MODEL"
        private const val VAULT_HANDLER_LOGGER_NAME = "VAULT_INTERACTOR"
    }
}
