package io.primer.android.internal.di

import android.content.Context
import io.primer.android.clientSessionActions.di.ActionsContainer
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.internal.data.mappers.KlarnaMapper
import io.primer.android.internal.data.mappers.PaymentMethodMapper
import io.primer.android.internal.data.mappers.PaymentMethodMapperImpl
import io.primer.android.internal.data.repositories.CardRawDataManagerRepository
import io.primer.android.internal.data.repositories.HeadlessRepositoryImpl
import io.primer.android.internal.data.repositories.KlarnaRepositoryImpl
import io.primer.android.internal.data.repositories.NativeUiRepositoryImpl
import io.primer.android.internal.data.repositories.PrimerVaultManagerRepositoryImpl
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.internal.domain.repositories.KlarnaRepository
import io.primer.android.internal.domain.repositories.NativeUiRepository
import io.primer.android.internal.domain.repositories.PrimerVaultManagerRepository
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import io.primer.android.internal.domain.usecase.AvailablePaymentMethodsUseCase
import io.primer.android.internal.domain.usecase.CardFieldsUseCase
import io.primer.android.internal.domain.usecase.CardFormCleanupUseCase
import io.primer.android.internal.domain.usecase.CardNetworkUseCase
import io.primer.android.internal.domain.usecase.GetCountriesUseCase
import io.primer.android.internal.domain.usecase.HeadlessCleanupUseCase
import io.primer.android.internal.domain.usecase.KlarnaCleanupUseCase
import io.primer.android.internal.domain.usecase.SetVaultOnSuccessUseCase
import io.primer.android.internal.domain.usecase.StartNativeUiPaymentUseCase
import io.primer.android.internal.domain.usecase.SubmitCardPaymentUseCase
import io.primer.android.internal.domain.usecase.vault.CheckCvvRecaptureRequiredUseCase
import io.primer.android.internal.domain.usecase.vault.DeleteVaultedPaymentMethodUseCase
import io.primer.android.internal.domain.usecase.vault.FetchVaultedPaymentMethodsUseCase
import io.primer.android.internal.domain.usecase.vault.SubmitVaultedPaymentUseCase
import io.primer.android.internal.navigation.CheckoutNavigator
import io.primer.android.internal.navigation.CheckoutResultHandler
import io.primer.android.internal.navigation.CountryNavigator
import io.primer.android.internal.navigation.DefaultCountryNavigator
import io.primer.android.internal.navigation.ScreenNavigator
import io.primer.android.ui.core.configuration.domain.model.BasicOrderInfoInteractor
import io.primer.android.ui.core.data.repository.CountriesDataRepository
import io.primer.android.ui.core.domain.FormatAmountToCurrencyInteractor
import io.primer.android.ui.core.payment.domain.interactor.SurchargeInteractor
import java.lang.ref.WeakReference

@Suppress("TooManyFunctions")
internal class ComponentsContainer(
    private val sdk: () -> SdkContainer,
) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerRepositories()
        registerInteractors()
        registerUseCases()
        registerNavigators()
    }

    private fun registerNavigators() {
        // CountryNavigator - standalone, no external dependencies
        registerSingleton<CountryNavigator> { DefaultCountryNavigator() }
    }

    private fun registerRepositories() {
        registerSingleton<PaymentMethodMapper> {
            PaymentMethodMapperImpl()
        }

        registerSingleton<HeadlessRepository> {
            HeadlessRepositoryImpl(
                headless = PrimerHeadlessUniversalCheckout.current,
                context = WeakReference<Context>(sdk().resolve()),
                primerConfig = sdk().resolve(),
            )
        }

        registerSingleton<RawDataManagerRepository>(CARD_RAW_DATA_MANAGER_REPOSITORY_DI_KEY) {
            CardRawDataManagerRepository()
        }

        registerSingleton<NativeUiRepository> {
            NativeUiRepositoryImpl(
                context = WeakReference<Context>(sdk().resolve()),
            )
        }

        registerSingleton {
            CountriesDataRepository(sdk().resolve())
        }

        registerSingleton {
            KlarnaMapper()
        }

        registerSingleton<KlarnaRepository> {
            KlarnaRepositoryImpl(
                mapper = resolve(),
                primerConfig = sdk().resolve(),
            )
        }
    }

    private fun registerInteractors() {
        registerSingleton {
            SurchargeInteractor(sdk().resolve())
        }

        registerSingleton {
            BasicOrderInfoInteractor(sdk().resolve())
        }

        registerSingleton {
            FormatAmountToCurrencyInteractor(
                currencyFormatRepository = sdk().resolve(),
                settings = sdk().resolve(),
            )
        }
    }

    private fun registerUseCases() {
        registerPaymentUseCases()
        registerVaultUseCases()
        registerCleanupUseCases()
    }

    private fun registerPaymentUseCases() {
        registerSingleton {
            AvailablePaymentMethodsUseCase(
                headlessRepository = resolve(),
                paymentMethodMapper = resolve(),
                surchargeInteractor = sdk().resolve(),
            )
        }

        registerSingleton {
            CardFieldsUseCase(
                rawDataManagerRepository = resolve(CARD_RAW_DATA_MANAGER_REPOSITORY_DI_KEY),
                configurationInteractor = sdk().resolve(ConfigurationCoreContainer.CONFIGURATION_INTERACTOR_DI_KEY),
                logReporter = sdk().resolve(),
            )
        }

        registerSingleton {
            CardNetworkUseCase(rawDataManagerRepository = resolve(CARD_RAW_DATA_MANAGER_REPOSITORY_DI_KEY))
        }

        registerSingleton {
            SubmitCardPaymentUseCase(
                rawDataManagerRepository = resolve(CARD_RAW_DATA_MANAGER_REPOSITORY_DI_KEY),
                headlessRepository = resolve(),
                actionInteractor = sdk().resolve(ActionsContainer.ACTION_INTERACTOR_DI_KEY),
            )
        }

        registerSingleton {
            StartNativeUiPaymentUseCase(
                nativeUiRepository = resolve(),
                headlessRepository = resolve(),
            )
        }

        registerSingleton {
            SetVaultOnSuccessUseCase(
                actionInteractor = sdk().resolve(ActionsContainer.ACTION_INTERACTOR_DI_KEY),
            )
        }

        registerSingleton {
            GetCountriesUseCase(
                countriesRepository = resolve(),
            )
        }
    }

    private fun registerVaultUseCases() {
        registerSingleton<PrimerVaultManagerRepository> {
            PrimerVaultManagerRepositoryImpl()
        }

        registerSingleton {
            FetchVaultedPaymentMethodsUseCase(vaultManagerRepository = resolve())
        }

        registerSingleton {
            SubmitVaultedPaymentUseCase(
                vaultManagerRepository = resolve(),
                headlessRepository = resolve(),
            )
        }

        registerSingleton {
            CheckCvvRecaptureRequiredUseCase(
                configurationInteractor = sdk().resolve(ConfigurationCoreContainer.CONFIGURATION_INTERACTOR_DI_KEY),
            )
        }

        registerSingleton {
            DeleteVaultedPaymentMethodUseCase(repository = resolve())
        }
    }

    private fun registerCleanupUseCases() {
        registerSingleton {
            HeadlessCleanupUseCase(
                headlessRepository = resolve(),
            )
        }

        registerSingleton {
            CardFormCleanupUseCase(
                rawDataManagerRepository = resolve(CARD_RAW_DATA_MANAGER_REPOSITORY_DI_KEY),
            )
        }

        registerSingleton {
            KlarnaCleanupUseCase(
                klarnaRepository = resolve(),
            )
        }
    }

    /**
     * Register the checkout navigator instance.
     * Called from PrimerCheckoutViewModelFactory after creating the navigator.
     *
     * Registers as:
     * - [CheckoutNavigator] - for direct access to navigation events
     * - [ScreenNavigator] - for screen navigation
     * - [CheckoutResultHandler] - for result delivery
     */
    fun registerNavigator(navigator: CheckoutNavigator) {
        registerSingleton { navigator }
        registerSingleton<ScreenNavigator> { navigator }
        registerSingleton<CheckoutResultHandler> { navigator }
    }

    companion object {
        const val CARD_RAW_DATA_MANAGER_REPOSITORY_DI_KEY = "CARD_RAW_DATA_MANAGER_REPOSITORY"
    }
}
