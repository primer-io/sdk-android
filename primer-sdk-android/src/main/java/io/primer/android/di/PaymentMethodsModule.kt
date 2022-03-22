package io.primer.android.di

import io.primer.android.domain.action.ActionInteractor
import io.primer.android.data.action.repository.ActionDataRepository
import io.primer.android.data.payments.methods.datasource.RemoteVaultedPaymentMethodsDataSource
import io.primer.android.data.payments.methods.datasource.RemoteVaultedPaymentMethodDeleteDataSource
import io.primer.android.data.payments.methods.datasource.RemoteVaultedPaymentMethodsExchangeDataSource
import io.primer.android.data.payments.methods.repository.PaymentMethodsDataRepository
import io.primer.android.data.payments.methods.repository.VaultedPaymentMethodsDataRepository
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsInteractor
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.logging.DefaultLogger
import io.primer.android.logging.Logger
import io.primer.android.data.payments.methods.mapping.DefaultPaymentMethodMapping
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.payment.PaymentMethodListFactory
import io.primer.android.data.payments.methods.mapping.PaymentMethodMapping
import io.primer.android.domain.action.ActionRepository
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsDeleteInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsExchangeInteractor
import io.primer.android.domain.payments.methods.repository.VaultedPaymentMethodsRepository
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import io.primer.android.viewmodel.PrimerPaymentMethodCheckerRegistry
import io.primer.android.viewmodel.PrimerViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val PRIMER_VIEW_MODEL_HANDLER_LOGGER_NAME = "PRIMER_VIEW_MODEL"
private const val VAULT_HANDLER_LOGGER_NAME = "VAULT_INTERACTOR"

internal val PaymentMethodsModule = {
    module {
        factory<Logger>(named(PRIMER_VIEW_MODEL_HANDLER_LOGGER_NAME)) {
            DefaultLogger(
                PRIMER_VIEW_MODEL_HANDLER_LOGGER_NAME
            )
        }

        factory<Logger>(named(VAULT_HANDLER_LOGGER_NAME)) {
            DefaultLogger(
                VAULT_HANDLER_LOGGER_NAME
            )
        }

        single { PrimerPaymentMethodCheckerRegistry }
        single<PaymentMethodCheckerRegistry> { PrimerPaymentMethodCheckerRegistry }
        single { PaymentMethodDescriptorFactoryRegistry(get()) }
        single { PaymentMethodListFactory(get()) }
        single<PaymentMethodMapping> { DefaultPaymentMethodMapping(get()) }

        single<PaymentMethodsRepository> {
            PaymentMethodsDataRepository(
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }

        single {
            PaymentMethodModulesInteractor(
                get(),
                get(),
                get(),
                get(named(PRIMER_VIEW_MODEL_HANDLER_LOGGER_NAME))
            )
        }

        single { RemoteVaultedPaymentMethodsDataSource(get()) }
        single { RemoteVaultedPaymentMethodDeleteDataSource(get()) }
        single { RemoteVaultedPaymentMethodsExchangeDataSource(get()) }
        single<VaultedPaymentMethodsRepository> {
            VaultedPaymentMethodsDataRepository(
                get(),
                get(),
                get(),
                get()
            )
        }

        single { VaultedPaymentMethodsInteractor(get(), get()) }
        single {
            VaultedPaymentMethodsDeleteInteractor(
                get(),
                get(named(VAULT_HANDLER_LOGGER_NAME)),
            )
        }
        single { VaultedPaymentMethodsExchangeInteractor(get(), get(), get(), get()) }

        single<ActionRepository> { ActionDataRepository(get()) }

        single { ActionInteractor(get(), get(), get(), get(), get()) }

        viewModel { PrimerViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    }
}
