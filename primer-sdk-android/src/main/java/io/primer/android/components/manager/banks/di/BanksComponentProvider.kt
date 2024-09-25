package io.primer.android.components.manager.banks.di

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.manager.banks.component.DefaultBanksComponent
import io.primer.android.components.presentation.paymentMethods.componentWithRedirect.banks.delegate.BankIssuerTokenizationDelegate
import io.primer.android.components.presentation.paymentMethods.componentWithRedirect.banks.delegate.GetBanksDelegate
import io.primer.android.di.BanksContainer
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.resolve

internal object BanksComponentProvider : DISdkComponent {
    fun provideInstance(
        owner: ViewModelStoreOwner,
        paymentMethodType: String,
        onFinished: () -> Unit,
        onDisposed: () -> Unit
    ) = ViewModelProvider(
        owner = owner,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T = DefaultBanksComponent(
                paymentMethodType = paymentMethodType,
                getBanksDelegate = GetBanksDelegate(
                    paymentMethodType = paymentMethodType,
                    banksInteractor = resolve(),
                    banksFilterInteractor = resolve(),
                    paymentMethodModulesInteractor = resolve()
                ),
                bankIssuerTokenizationDelegate = BankIssuerTokenizationDelegate(
                    paymentMethodType = paymentMethodType,
                    actionInteractor = resolve(),
                    tokenizationInteractor = resolve(),
                    paymentMethodModulesInteractor = resolve(),
                    asyncPaymentMethodDeeplinkInteractor = resolve(),
                    primerConfig = resolve()
                ),
                eventLoggingDelegate = resolve(
                    PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT.name
                ),
                errorLoggingDelegate = resolve(),
                validationErrorLoggingDelegate = resolve(),
                errorMapper = resolve(BanksContainer.BANKS_ERROR_RESOLVER_NAME),
                savedStateHandle = runCatching {
                    extras.createSavedStateHandle()
                }.getOrDefault(SavedStateHandle()),
                onFinished = onFinished
            ).apply { addCloseable { onDisposed() } } as T
        }
    )[DefaultBanksComponent::class.java]
}
