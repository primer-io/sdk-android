package io.primer.android.stripe.ach.di

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.stripe.ach.api.component.StripeAchUserDetailsComponent

internal object StripeAchUserDetailsComponentProvider : DISdkComponent {
    fun provideInstance(owner: ViewModelStoreOwner) =
        ViewModelProvider(
            owner = owner,
            factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras,
                ): T =
                    StripeAchUserDetailsComponent(
                        getClientSessionCustomerDetailsDelegate = resolve(),
                        stripeAchClientSessionPatchDelegate = resolve(),
                        stripeAchTokenizationDelegate = resolve(),
                        stripeAchPaymentDelegate = resolve(),
                        eventLoggingDelegate = resolve(PaymentMethodType.STRIPE_ACH.name),
                        stripeAchBankFlowDelegate = resolve(),
                        errorLoggingDelegate = resolve(),
                        validationErrorLoggingDelegate = resolve(PaymentMethodType.STRIPE_ACH.name),
                        errorMapperRegistry = resolve(),
                        successHandler = resolve(),
                        pendingResumeHandler = resolve(),
                        manualFlowSuccessHandler = resolve(),
                        primerSettings = resolve(),
                        config = resolve(),
                        savedStateHandle =
                        runCatching {
                            extras.createSavedStateHandle()
                        }.getOrDefault(SavedStateHandle()),
                    ) as T
            },
        )[StripeAchUserDetailsComponent::class.java]
}
