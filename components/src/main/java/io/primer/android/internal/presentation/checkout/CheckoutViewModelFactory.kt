package io.primer.android.internal.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.internal.di.ComponentsContainer
import io.primer.android.internal.domain.usecase.AvailablePaymentMethodsUseCase
import io.primer.android.internal.domain.usecase.HeadlessCleanupUseCase
import io.primer.android.internal.domain.usecase.vault.CheckCvvRecaptureRequiredUseCase
import io.primer.android.internal.domain.usecase.vault.DeleteVaultedPaymentMethodUseCase
import io.primer.android.internal.domain.usecase.vault.FetchVaultedPaymentMethodsUseCase
import io.primer.android.internal.domain.usecase.vault.SubmitVaultedPaymentUseCase
import io.primer.android.internal.navigation.CheckoutNavigator
import io.primer.android.internal.navigation.CountryNavigator
import io.primer.android.ui.core.domain.FormatAmountToCurrencyInteractor

/**
 * Factory for creating [CheckoutViewModel].
 */
internal class CheckoutViewModelFactory : ViewModelProvider.Factory, DISdkComponent {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val navigator = CheckoutNavigator()

        // Register navigator in DI so other ViewModels can resolve it
        DISdkContext.componentsSdkContainer?.containers?.values
            ?.filterIsInstance<ComponentsContainer>()
            ?.firstOrNull()
            ?.registerNavigator(navigator)

        return CheckoutViewModel(
            configurationInteractor = resolve<ConfigurationInteractor>(
                ConfigurationCoreContainer.CONFIGURATION_INTERACTOR_DI_KEY,
            ),
            configurationRepository = resolve<ConfigurationRepository>(),
            availablePaymentMethodsUseCase = resolve<AvailablePaymentMethodsUseCase>(),
            cleanupUseCase = resolve<HeadlessCleanupUseCase>(),
            fetchVaultedPaymentMethodsUseCaseProvider = { resolve<FetchVaultedPaymentMethodsUseCase>() },
            checkCvvRecaptureRequiredUseCaseProvider = { resolve<CheckCvvRecaptureRequiredUseCase>() },
            submitVaultedPaymentUseCaseProvider = { resolve<SubmitVaultedPaymentUseCase>() },
            deleteVaultedPaymentMethodUseCaseProvider = { resolve<DeleteVaultedPaymentMethodUseCase>() },
            formatAmountInteractor = resolve<FormatAmountToCurrencyInteractor>(),
            settings = resolve<PrimerSettings>(),
            componentsEventsRepository = runCatching {
                resolve<ComponentsEventsRepository>()
            }.getOrNull(),
            logReporter = resolve<LogReporter>(),
            navigator = navigator,
            countryNavigator = resolve<CountryNavigator>(),
        ) as T
    }
}
