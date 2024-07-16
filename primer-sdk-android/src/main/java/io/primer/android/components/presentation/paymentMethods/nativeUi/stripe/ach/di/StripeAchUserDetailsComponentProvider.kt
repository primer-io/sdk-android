package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.di

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.StripeAchUserDetailsComponent
import io.primer.android.di.DISdkComponent
import io.primer.android.di.StripeContainer
import io.primer.android.di.extension.resolve

internal object StripeAchUserDetailsComponentProvider : DISdkComponent {
    fun provideInstance(
        owner: ViewModelStoreOwner
    ) = ViewModelProvider(
        owner = owner,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T = StripeAchUserDetailsComponent(
                getClientSessionCustomerDetailsDelegate = resolve(),
                stripeAchClientSessionPatchDelegate = resolve(),
                stripeAchTokenizationDelegate = resolve(),
                eventLoggingDelegate = resolve(
                    PrimerPaymentMethodManagerCategory.NATIVE_UI.name
                ),
                errorLoggingDelegate = resolve(),
                errorEventResolver = resolve(),
                savedStateHandle = runCatching {
                    extras.createSavedStateHandle()
                }.getOrDefault(SavedStateHandle()),
                primerSettings = resolve(),
                errorMapper = resolve(StripeContainer.STRIPE_ERROR_RESOLVER_NAME)
            ) as T
        }
    )[StripeAchUserDetailsComponent::class.java]
}
