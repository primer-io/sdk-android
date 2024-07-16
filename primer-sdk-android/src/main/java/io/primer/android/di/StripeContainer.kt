@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.di

import android.content.Context
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.error.StripeErrorMapper
import io.primer.android.components.data.payments.paymentMethods.stripe.ach.datasource.RemoteStripeAchCompletePaymentDataSource
import io.primer.android.components.data.payments.paymentMethods.stripe.ach.repository.StripeAchCompletePaymentDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.validation.resolvers.StripeInitValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.validation.rules.ValidStripeMandateDataRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.validation.rules.ValidStripePublishableKeyRule
import io.primer.android.components.domain.payments.paymentMethods.stripe.ach.StripeAchCompletePaymentInteractor
import io.primer.android.components.domain.payments.paymentMethods.stripe.ach.repository.StripeAchCompletePaymentRepository
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.GetClientSessionCustomerDetailsDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.GetStripeMandateDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.GetStripePublishableKeyDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchClientSessionPatchDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchTokenizationDelegate
import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.payments.helpers.StripeAchPostPaymentCreationEventResolver
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class StripeContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerFactory<ErrorMapper>(STRIPE_ERROR_RESOLVER_NAME) {
            StripeErrorMapper()
        }

        registerSingleton { GetClientSessionCustomerDetailsDelegate(sdk.resolve()) }

        registerSingleton { StripeAchClientSessionPatchDelegate(sdk.resolve(), sdk.resolve()) }

        registerSingleton { StripeAchTokenizationDelegate(sdk.resolve(), sdk.resolve(), sdk.resolve(), sdk.resolve()) }

        registerSingleton { RemoteStripeAchCompletePaymentDataSource(sdk.resolve()) }

        registerSingleton<StripeAchCompletePaymentRepository> {
            StripeAchCompletePaymentDataRepository(resolve())
        }

        registerSingleton { StripeAchCompletePaymentInteractor(resolve()) }

        registerSingleton { CompleteStripeAchPaymentSessionDelegate(resolve()) }

        registerSingleton { GetStripePublishableKeyDelegate(sdk.resolve()) }

        registerSingleton {
            StripeAchPostPaymentCreationEventResolver(
                contextProvider = { sdk.resolve() },
                eventDispatcher = sdk.resolve(),
                checkoutErrorEventResolver = sdk.resolve(),
                stripePublishableKeyDelegate = resolve(),
                getClientSessionCustomerDetailsDelegate = resolve(),
                mockConfigurationDelegate = sdk.resolve()
            )
        }

        registerSingleton {
            StripeAchMandateTimestampLoggingDelegate(sdk.resolve(), sdk.resolve())
        }

        registerSingleton {
            ValidStripePublishableKeyRule()
        }

        registerSingleton {
            ValidStripeMandateDataRule(sdk.resolve())
        }

        registerSingleton {
            GetStripeMandateDelegate(sdk.resolve<Context>().resources, sdk.resolve())
        }

        registerSingleton { StripeInitValidationRulesResolver(resolve(), resolve()) }
    }

    internal companion object {
        const val STRIPE_ERROR_RESOLVER_NAME = "stripeErrorResolver"
    }
}
