@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.components.presentation.paymentMethods.componentWithRedirect.banks.delegate

import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.base.None
import io.primer.android.domain.deeplink.async.AsyncPaymentMethodDeeplinkInteractor
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.async.bankIssuer.BankIssuerPaymentInstrumentParams
import io.primer.android.extensions.runSuspendCatching
import io.primer.android.payment.PaymentMethodDescriptor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

internal class BankIssuerTokenizationDelegate(
    private val paymentMethodType: String,
    private val actionInteractor: ActionInteractor,
    private val tokenizationInteractor: TokenizationInteractor,
    private val paymentMethodModulesInteractor: PaymentMethodModulesInteractor,
    private val asyncPaymentMethodDeeplinkInteractor: AsyncPaymentMethodDeeplinkInteractor,
    private val primerConfig: PrimerConfig
) {
    private var paymentMethodDescriptor: PaymentMethodDescriptor? = null

    suspend fun tokenize(issuerBankId: String): Result<Unit> =
        runSuspendCatching {
            updateSelectedPaymentMethodParams()
            val paymentMethodDescriptor = getPaymentMethodDescriptor()
            tokenizationInteractor.executeV2(
                TokenizationParamsV2(
                    paymentInstrumentParams = BankIssuerPaymentInstrumentParams(
                        paymentMethodType = paymentMethodType,
                        paymentMethodConfigId = requireNotNull(paymentMethodDescriptor.config.id),
                        locale =
                        paymentMethodDescriptor.localConfig.settings.locale.toLanguageTag(),
                        redirectionUrl = getRedirectionUrl(),
                        bankIssuer = issuerBankId
                    ),
                    paymentMethodIntent = primerConfig.paymentMethodIntent
                )
            ).collect()
        }

    private suspend fun updateSelectedPaymentMethodParams() {
        actionInteractor(
            ActionUpdateSelectPaymentMethodParams(
                paymentMethodType = paymentMethodType,
                cardNetwork = null
            )
        ).collect()
    }

    private fun getPaymentMethodDescriptor(): PaymentMethodDescriptor {
        return paymentMethodDescriptor
            ?: paymentMethodModulesInteractor.getPaymentMethodDescriptors()
                .first { it.config.type == paymentMethodType }.also { paymentMethodDescriptor = it }
    }

    private fun getRedirectionUrl() = asyncPaymentMethodDeeplinkInteractor(None())
}
