package io.primer.android.components.ach

import android.content.Context
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.PaymentMethodManagerDelegate
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.inject
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.manager.component.ach.PrimerHeadlessAchComponent
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep
import io.primer.android.stripe.ach.api.component.StripeAchUserDetailsComponent
import io.primer.android.stripe.ach.implementation.validation.resolvers.StripeInitValidationRulesResolver

/**
 * The [PrimerHeadlessUniversalCheckoutAchManager] class provides a method to obtain
 * components related to ACH integrations in the Primer SDK. These components can be used to
 * handle ACH functionalities within your application.
 *
 * @param viewModelStoreOwner The [ViewModelStoreOwner] to associate with the component.
 */
class PrimerHeadlessUniversalCheckoutAchManager(
    private val viewModelStoreOwner: ViewModelStoreOwner,
) : DISdkComponent {
    private val primerSettings by inject<PrimerSettings>()
    private val paymentMethodInitializer: PaymentMethodManagerDelegate by inject()
    private val stripeInitValidationRulesResolver: StripeInitValidationRulesResolver by inject()

    /**
     * Provides an instance of the [PrimerHeadlessAchComponent] to handle ACH sessions.
     * @param paymentMethodType A unique string identifier for the payment method. Supported payment methods for current
     * client session are returned in the [PrimerHeadlessUniversalCheckoutListener.onAvailablePaymentMethodsLoaded]
     * callback.
     * @return An instance of [PrimerHeadlessAchComponent].
     */
    @Suppress("ThrowsCount")
    @Throws(UnsupportedPaymentMethodException::class)
    fun <
        T : PrimerHeadlessAchComponent<
            out PrimerCollectableData,
            out PrimerHeadlessStep,
            >,
        > provide(
        paymentMethodType: String,
    ): T {
        try {
            when (paymentMethodType) {
                PaymentMethodType.STRIPE_ACH.name -> {
                    val validationResults =
                        stripeInitValidationRulesResolver.resolve().rules.map {
                            it.validate(
                                primerSettings.paymentMethodOptions.stripeOptions,
                            )
                        }
                    validationResults.filterIsInstance<ValidationResult.Failure>()
                        .forEach { validationResult ->
                            throw validationResult.exception
                        }

                    val category = PrimerPaymentMethodManagerCategory.STRIPE_ACH
                    paymentMethodInitializer.apply {
                        init(
                            paymentMethodType = paymentMethodType,
                            category = category,
                        ).also {
                            start(
                                context = resolve<Context>(),
                                paymentMethodType = paymentMethodType,
                                sessionIntent = PrimerSessionIntent.CHECKOUT,
                                category = category,
                            )
                        }
                    }

                    @Suppress("UNCHECKED_CAST")
                    return StripeAchUserDetailsComponent.provideInstance(owner = viewModelStoreOwner) as T
                }

                else -> throw UnsupportedPaymentMethodException(paymentMethodType)
            }
        } catch (expected: Throwable) {
            throw UnsupportedPaymentMethodException(paymentMethodType = paymentMethodType, cause = expected)
        }
    }
}
