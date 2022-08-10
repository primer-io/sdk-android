package io.primer.android.components.presentation

import io.primer.android.PrimerSessionIntent
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutInputData
import io.primer.android.components.domain.core.models.card.CardInputData
import io.primer.android.components.domain.inputs.PaymentInputTypesInteractor
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.PaymentInputDataValidateInteractor
import io.primer.android.components.domain.payments.PaymentTokenizationInteractor
import io.primer.android.components.domain.payments.PaymentsTypesInteractor
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.domain.base.None
import io.primer.android.domain.payments.create.CreatePaymentInteractor
import io.primer.android.domain.payments.create.model.CreatePaymentParams
import io.primer.android.domain.payments.displayMetadata.PaymentMethodsImplementationInteractor
import io.primer.android.domain.payments.resume.ResumePaymentInteractor
import io.primer.android.domain.payments.resume.models.ResumeParams
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.payment.config.BaseDisplayMetadata
import io.primer.android.payment.config.toImageDisplayMetadata
import io.primer.android.payment.config.toTextDisplayMetadata
import io.primer.android.ui.CardType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

internal interface HeadlessUniversalCheckoutDelegate {
    fun start()

    fun getPaymentMethodsDisplayMetadata(isDarkMode: Boolean): List<BaseDisplayMetadata>

    fun listRequiredInputElementTypes(paymentMethodType: String): List<PrimerInputElementType>?

    fun startTokenization(
        type: String,
        inputData: PrimerHeadlessUniversalCheckoutInputData
    )

    fun createPayment(
        paymentMethodToken: String,
        resumeHandler: PrimerResumeDecisionHandler
    )

    fun resumePayment(resumeToken: String, resumeHandler: PrimerResumeDecisionHandler)

    fun dispatchAction(
        type: String,
        inputData: PrimerHeadlessUniversalCheckoutInputData,
        completion: ((Error?) -> Unit) = {},
    )

    fun clear()
}

internal class DefaultHeadlessUniversalCheckoutDelegate(
    private val tokenizationInteractor: TokenizationInteractor,
    private val paymentsTypesInteractor: PaymentsTypesInteractor,
    private val paymentTokenizationInteractor: PaymentTokenizationInteractor,
    private val paymentInputDataValidateInteractor: PaymentInputDataValidateInteractor,
    private val paymentInputTypesInteractor: PaymentInputTypesInteractor,
    private val paymentMethodsImplementationInteractor: PaymentMethodsImplementationInteractor,
    private val createPaymentInteractor: CreatePaymentInteractor,
    private val resumePaymentInteractor: ResumePaymentInteractor,
    private val actionInteractor: ActionInteractor
) : HeadlessUniversalCheckoutDelegate {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())
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

    override fun listRequiredInputElementTypes(paymentMethodType: String) =
        paymentInputTypesInteractor.execute(paymentMethodType)

    override fun startTokenization(
        type: String,
        inputData: PrimerHeadlessUniversalCheckoutInputData
    ) {
        scope.launch {
            paymentTokenizationInteractor.execute(
                PaymentTokenizationDescriptorParams(type, inputData)
            ).flatMapLatest {
                tokenizationInteractor(
                    TokenizationParams(
                        it,
                        PrimerSessionIntent.CHECKOUT,
                        false
                    )
                )
            }.catch { }.collect { }
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

    override fun dispatchAction(
        type: String,
        inputData: PrimerHeadlessUniversalCheckoutInputData,
        completion: ((Error?) -> Unit),
    ) {
        scope.launch {
            paymentInputDataValidateInteractor(PaymentTokenizationDescriptorParams(type, inputData))
                .flatMapLatest {
                    actionInteractor(getActionUpdateParams(type, inputData))
                }.catch {
                    completion(Error(it))
                }
                .collect {
                    completion(null)
                }
        }
    }

    override fun clear() = scope.coroutineContext.job.cancelChildren()

    private fun getActionUpdateParams(
        type: String,
        inputData: PrimerHeadlessUniversalCheckoutInputData
    ) =
        when (inputData) {
            is CardInputData -> {
                val cardType = CardType.lookup(inputData.number).type
                if (cardType == CardType.Type.UNKNOWN) {
                    ActionUpdateUnselectPaymentMethodParams
                } else {
                    ActionUpdateSelectPaymentMethodParams(
                        type,
                        cardType.name
                    )
                }
            }
            else -> ActionUpdateSelectPaymentMethodParams(type)
        }
}
