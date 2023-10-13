package io.primer.android.components.manager.nativeUi

import android.content.Context
import android.content.Intent
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.resolvers.PaymentMethodManagerSessionIntentRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.PaymentMethodManagerSessionIntentValidationData
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.ui.activity.HeadlessActivity
import io.primer.android.components.ui.activity.PaymentMethodLauncherParams
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.inject
import io.primer.android.domain.exception.UnsupportedPaymentMethodException

class PrimerHeadlessUniversalCheckoutNativeUiManager private constructor(
    private val paymentMethodType: String
) : PrimerHeadlessUniversalCheckoutNativeUiManagerInterface, DISdkComponent {

    private val delegate: DefaultHeadlessManagerDelegate by inject()

    private val sessionIntentRulesResolver: PaymentMethodManagerSessionIntentRulesResolver
        by inject()

    init {
        delegate.init(paymentMethodType, PrimerPaymentMethodManagerCategory.NATIVE_UI)
    }

    override fun showPaymentMethod(
        context: Context,
        sessionIntent: PrimerSessionIntent
    ) {
        PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                mapOf(
                    "category" to PrimerPaymentMethodManagerCategory.NATIVE_UI.name,
                    "paymentMethodType" to paymentMethodType,
                    "intent" to sessionIntent.name
                )
            )
        )
        val validationResults = sessionIntentRulesResolver.resolve().rules.map {
            it.validate(
                PaymentMethodManagerSessionIntentValidationData(
                    paymentMethodType,
                    sessionIntent
                )
            )
        }

        validationResults.filterIsInstance<ValidationResult.Failure>()
            .forEach { validationResult ->
                throw validationResult.exception
            }

        delegate.dispatchAction(paymentMethodType) { error ->
            if (error == null) {
                context.startActivity(
                    HeadlessActivity.getLaunchIntent(
                        context,
                        PaymentMethodLauncherParams(paymentMethodType, sessionIntent)
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    override fun cleanup() {
        delegate.cleanup()
    }

    companion object {
        /**
         * Creates Native UI manager tied to current session for a given payment method.
         *
         * @param paymentMethodType the payment method flow to be shown.
         * @throws SdkUninitializedException
         * @throws UnsupportedPaymentMethodException
         */

        @Throws(SdkUninitializedException::class, UnsupportedPaymentMethodException::class)
        @JvmStatic
        fun newInstance(
            paymentMethodType: String
        ): PrimerHeadlessUniversalCheckoutNativeUiManagerInterface {
            return PrimerHeadlessUniversalCheckoutNativeUiManager(paymentMethodType)
        }
    }
}
