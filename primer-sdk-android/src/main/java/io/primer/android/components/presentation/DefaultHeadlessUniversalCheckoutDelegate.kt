package io.primer.android.components.presentation

import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.domain.inputs.PaymentInputTypesInteractor
import io.primer.android.components.domain.payments.PaymentInputDataTypeValidateInteractor
import io.primer.android.components.domain.payments.PaymentTokenizationInteractor
import io.primer.android.components.domain.payments.PaymentsTypesInteractor
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.payments.async.AsyncPaymentMethodInteractor
import io.primer.android.domain.payments.create.CreatePaymentInteractor
import io.primer.android.domain.payments.create.model.CreatePaymentParams
import io.primer.android.domain.payments.displayMetadata.PaymentMethodsImplementationInteractor
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.domain.payments.resume.ResumePaymentInteractor
import io.primer.android.domain.payments.resume.models.ResumeParams
import io.primer.android.domain.rpc.retailOutlets.RetailOutletInteractor
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.payment.config.BaseDisplayMetadata
import io.primer.android.payment.config.toImageDisplayMetadata
import io.primer.android.payment.config.toTextDisplayMetadata
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

internal interface HeadlessUniversalCheckoutDelegate {
    fun start()

    fun getPaymentMethodsDisplayMetadata(isDarkMode: Boolean): List<BaseDisplayMetadata>

    fun createPayment(
        paymentMethodToken: String,
        resumeHandler: PrimerResumeDecisionHandler
    )

    fun resumePayment(resumeToken: String, resumeHandler: PrimerResumeDecisionHandler)

    fun clear()
}

internal class DefaultHeadlessUniversalCheckoutDelegate(
    tokenizationInteractor: TokenizationInteractor,
    paymentTokenizationInteractor: PaymentTokenizationInteractor,
    paymentInputDataTypeValidateInteractor: PaymentInputDataTypeValidateInteractor,
    paymentInputTypesInteractor: PaymentInputTypesInteractor,
    actionInteractor: ActionInteractor,
    asyncPaymentMethodInteractor: AsyncPaymentMethodInteractor,
    paymentMethodsRepository: PaymentMethodsRepository,
    retailOutletInteractor: RetailOutletInteractor,
    config: PrimerConfig,
    private val paymentsTypesInteractor: PaymentsTypesInteractor,
    private val paymentMethodsImplementationInteractor: PaymentMethodsImplementationInteractor,
    private val createPaymentInteractor: CreatePaymentInteractor,
    private val resumePaymentInteractor: ResumePaymentInteractor,
) : DefaultHeadlessDelegate(
    tokenizationInteractor,
    paymentInputTypesInteractor,
    paymentTokenizationInteractor,
    paymentInputDataTypeValidateInteractor,
    actionInteractor,
    asyncPaymentMethodInteractor,
    paymentMethodsRepository,
    retailOutletInteractor,
    config,
),
    HeadlessUniversalCheckoutDelegate {

    private var transactionId: String? = null

    override fun start() {
        scope.launch { paymentsTypesInteractor(None()).collect {} }
    }

    override fun getPaymentMethodsDisplayMetadata(isDarkMode: Boolean) =
        paymentMethodsImplementationInteractor.invoke(
            None()
        ).map {
            when (it.buttonMetadata?.text.isNullOrBlank()) {
                true -> it.toImageDisplayMetadata(isDarkMode)
                false -> it.toTextDisplayMetadata(isDarkMode)
            }
        }

    override fun createPayment(
        paymentMethodToken: String,
        resumeHandler: PrimerResumeDecisionHandler
    ) {
        scope.launch {
            createPaymentInteractor(
                CreatePaymentParams(
                    paymentMethodToken,
                    resumeHandler
                )
            ).collect {
                transactionId = it
            }
        }
    }

    override fun resumePayment(resumeToken: String, resumeHandler: PrimerResumeDecisionHandler) {
        scope.launch {
            resumePaymentInteractor(
                ResumeParams(
                    transactionId.orEmpty(),
                    resumeToken,
                    resumeHandler
                )
            ).collect { }
        }
    }

    override fun clear() = scope.coroutineContext.job.cancelChildren()
}
