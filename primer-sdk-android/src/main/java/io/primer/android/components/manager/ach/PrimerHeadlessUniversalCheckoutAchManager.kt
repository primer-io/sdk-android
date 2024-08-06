package io.primer.android.components.manager.ach

import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.validation.resolvers.StripeInitValidationRulesResolver
import io.primer.android.components.manager.core.composable.PrimerCollectableData
import io.primer.android.components.manager.core.composable.PrimerHeadlessStep
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.StripeAchUserDetailsComponent
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.inject
import io.primer.android.domain.exception.UnsupportedPaymentMethodException

/**
 * The [PrimerHeadlessUniversalCheckoutAchManager] class provides a method to obtain
 * components related to ACH integrations in the Primer SDK. These components can be used to
 * handle ACH functionalities within your application.
 *
 * @param viewModelStoreOwner The [ViewModelStoreOwner] to associate with the component.
 */
class PrimerHeadlessUniversalCheckoutAchManager(
    private val viewModelStoreOwner: ViewModelStoreOwner
) : DISdkComponent {
    private val primerSettings by inject<PrimerSettings>()
    private val headlessManagerDelegate: DefaultHeadlessManagerDelegate by inject()
    private val stripeInitValidationRulesResolver: StripeInitValidationRulesResolver by inject()

    /**
     * Provides an instance of the [PrimerHeadlessAchComponent] to handle ACH sessions.
     * @param paymentMethodType A unique string identifier for the payment method. Supported payment methods for current
     * client session are returned in the [PrimerHeadlessUniversalCheckoutListener.onAvailablePaymentMethodsLoaded]
     * callback.
     * @return An instance of [PrimerHeadlessAchComponent].
     */
    @Suppress("ThrowsCount")
    fun <T : PrimerHeadlessAchComponent<
            out PrimerCollectableData,
            out PrimerHeadlessStep>> provide(
        paymentMethodType: String
    ): T {
        try {
            when (paymentMethodType) {
                PaymentMethodType.STRIPE_ACH.name -> {
                    val validationResults = stripeInitValidationRulesResolver.resolve().rules.map {
                        it.validate(
                            primerSettings.paymentMethodOptions.stripeOptions
                        )
                    }
                    validationResults.filterIsInstance<ValidationResult.Failure>()
                        .forEach { validationResult ->
                            throw validationResult.exception
                        }

                    headlessManagerDelegate.init(
                        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                        category = PrimerPaymentMethodManagerCategory.NATIVE_UI
                    )
                    @Suppress("UNCHECKED_CAST")
                    return StripeAchUserDetailsComponent.provideInstance(owner = viewModelStoreOwner) as T
                }

                else -> throw UnsupportedPaymentMethodException(paymentMethodType)
            }
        } catch (e: ClassCastException) {
            throw UnsupportedPaymentMethodException(paymentMethodType = paymentMethodType, cause = e)
        }
    }
}
