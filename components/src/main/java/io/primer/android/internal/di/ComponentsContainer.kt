package io.primer.android.internal.di

import android.content.Context
import io.primer.android.clientSessionActions.di.ActionsContainer
import io.primer.android.components.PrimerCardFormComponents
import io.primer.android.components.PrimerCheckoutComponents
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.PrimerKlarnaComponents
import io.primer.android.components.PrimerPaymentMethodSelectionComponents
import io.primer.android.components.PrimerSelectCountryComponents
import io.primer.android.components.PrimerVaultedComponents
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
import io.primer.android.internal.domain.usecase.CardNetworkUseCase
import io.primer.android.internal.domain.usecase.StartNativeUiPaymentUseCase
import io.primer.android.internal.domain.usecase.SubmitCardPaymentUseCase
import io.primer.android.internal.domain.usecase.vault.FetchVaultedPaymentMethodsUseCase
import io.primer.android.internal.domain.usecase.vault.ShouldCaptureVaultedCvvUseCase
import io.primer.android.internal.domain.usecase.vault.SubmitVaultedPaymentUseCase
import io.primer.android.internal.domain.usecase.vault.ValidateVaultedCVVUseCase
import io.primer.android.internal.domain.usecase.vault.VaultedCvvFieldsUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.CheckoutViewModelFactory
import io.primer.android.internal.presentation.screens.card.CardFormViewModelFactory
import io.primer.android.internal.presentation.screens.country.SelectCountryViewModelFactory
import io.primer.android.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionViewModelFactory
import io.primer.android.internal.presentation.screens.paymentMethodSelection.VaultedPaymentMethodSelectionViewModelFactory
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
        registerComponents()
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
                context = sdk().resolve(),
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

        registerSingleton<PrimerVaultManagerRepository> {
            PrimerVaultManagerRepositoryImpl()
        }

        registerSingleton {
            FetchVaultedPaymentMethodsUseCase(vaultManagerRepository = resolve())
        }

        registerSingleton {
            SubmitVaultedPaymentUseCase(vaultManagerRepository = resolve())
        }

        registerSingleton {
            ValidateVaultedCVVUseCase(vaultManagerRepository = resolve())
        }

        registerSingleton {
            ShouldCaptureVaultedCvvUseCase(
                configurationInteractor = sdk().resolve(ConfigurationCoreContainer.CONFIGURATION_INTERACTOR_DI_KEY),
            )
        }

        registerSingleton {
            VaultedCvvFieldsUseCase()
        }
    }

    private fun registerComponents() {
        registerCheckoutComponents()
        registerCardFormComponents()
        registerPaymentMethodSelectionComponents()
        registerVaultedComponents()
        registerCountrySelectionComponents()
        registerKlarnaComponents()
    }

    private fun registerCheckoutComponents() {
        registerSingleton {
            CheckoutNavigator()
        }

        registerFactory {
            CheckoutViewModelFactory()
        }

        registerSingleton {
            PrimerCheckoutComponents()
        }
    }

    private fun registerCardFormComponents() {
        registerFactory {
            CardFormViewModelFactory()
        }

        registerSingleton {
            PrimerCardFormComponents()
        }
    }

    private fun registerPaymentMethodSelectionComponents() {
        registerFactory {
            PaymentMethodSelectionViewModelFactory()
        }

        registerSingleton {
            PrimerPaymentMethodSelectionComponents()
        }
    }

    private fun registerVaultedComponents() {
        registerFactory {
            VaultedPaymentMethodSelectionViewModelFactory()
        }

        registerSingleton {
            PrimerVaultedComponents()
        }
    }

    private fun registerCountrySelectionComponents() {
        registerFactory {
            SelectCountryViewModelFactory()
        }

        registerSingleton {
            PrimerSelectCountryComponents()
        }
    }

    private fun registerKlarnaComponents() {
        registerSingleton {
            PrimerKlarnaComponents()
        }
    }

    companion object {
        const val CARD_RAW_DATA_MANAGER_REPOSITORY_DI_KEY = "CARD_RAW_DATA_MANAGER_REPOSITORY"
    }
}
