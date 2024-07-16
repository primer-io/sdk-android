@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate

import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.components.data.payments.paymentMethods.nativeUi.async.redirect.exception.AsyncIllegalValueKey
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.stripe.ach.StripeAchPaymentInstrumentParams
import io.primer.android.extensions.runSuspendCatching
import io.primer.android.payment.PaymentMethodDescriptor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

internal class StripeAchTokenizationDelegate(
    private val actionInteractor: ActionInteractor,
    private val primerSettings: PrimerSettings,
    private val paymentMethodModulesInteractor: PaymentMethodModulesInteractor,
    private val tokenizationInteractor: TokenizationInteractor
) {
    suspend operator fun invoke(): Result<Unit> = runSuspendCatching {
        if (primerSettings.sdkIntegrationType == SdkIntegrationType.HEADLESS) {
            updateSelectedPaymentMethodParams()
        }
        val paymentMethodDescriptor = getPaymentMethodDescriptor()
        tokenizationInteractor.executeV2(
            params = TokenizationParamsV2(
                paymentInstrumentParams = StripeAchPaymentInstrumentParams(
                    requireNotNullCheck(
                        value = paymentMethodDescriptor.config.id,
                        key = AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID
                    ),
                    paymentMethodDescriptor.localConfig.settings.locale.toLanguageTag()
                ),
                paymentMethodIntent = null
            )
        ).collect()
    }

    private fun getPaymentMethodDescriptor(): PaymentMethodDescriptor {
        return paymentMethodModulesInteractor.getPaymentMethodDescriptors()
            .first { it.config.type == PaymentMethodType.STRIPE_ACH.name }
    }

    private suspend fun updateSelectedPaymentMethodParams() {
        actionInteractor(
            ActionUpdateSelectPaymentMethodParams(
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                cardNetwork = null
            )
        ).collect()
    }
}
