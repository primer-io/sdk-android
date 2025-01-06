package io.primer.android.nolpay.implementation.paymentCard.payment.delegate

import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.nolpay.api.manager.payment.composable.NolPayPaymentCollectableData
import io.primer.android.nolpay.api.manager.payment.composable.NolPayPaymentStep
import io.primer.android.nolpay.implementation.paymentCard.completion.domain.NolPayCompletePaymentInteractor
import io.primer.android.nolpay.implementation.paymentCard.completion.domain.NolPayRequestPaymentInteractor
import io.primer.android.nolpay.implementation.paymentCard.completion.domain.model.NolPayCompletePaymentParams
import io.primer.android.nolpay.implementation.paymentCard.completion.domain.model.NolPayRequestPaymentParams
import io.primer.android.nolpay.implementation.paymentCard.payment.resume.handler.NolPayResumeDecision
import io.primer.android.nolpay.implementation.paymentCard.payment.resume.handler.NolPayResumeHandler
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessSteppable
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.payments.core.status.domain.AsyncPaymentMethodPollingInteractor
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest

@Suppress("LongParameterList")
internal class NolPayPaymentDelegate(
    private val requestPaymentInteractor: NolPayRequestPaymentInteractor,
    private val completePaymentInteractor: NolPayCompletePaymentInteractor,
    private val pollingInteractor: AsyncPaymentMethodPollingInteractor,
    paymentMethodTokenHandler: PaymentMethodTokenHandler,
    resumePaymentHandler: PaymentResumeHandler,
    successHandler: CheckoutSuccessHandler,
    errorHandler: CheckoutErrorHandler,
    baseErrorResolver: BaseErrorResolver,
    private val resumeHandler: NolPayResumeHandler,
) : PaymentMethodPaymentDelegate(
        paymentMethodTokenHandler,
        resumePaymentHandler,
        successHandler,
        errorHandler,
        baseErrorResolver,
    ),
    PrimerHeadlessSteppable<NolPayPaymentStep> {
    private lateinit var resumeDecision: NolPayResumeDecision

    private val _componentStep: MutableSharedFlow<NolPayPaymentStep> = MutableSharedFlow()
    override val componentStep: Flow<NolPayPaymentStep> = _componentStep

    override suspend fun handleNewClientToken(
        clientToken: String,
        payment: Payment?,
    ): Result<Unit> {
        return resumeHandler.continueWithNewClientToken(clientToken)
            .mapSuspendCatching { decision ->
                resumeDecision = decision
                _componentStep.emit(NolPayPaymentStep.CollectTagData)
            }
    }

    internal suspend fun requestPayment(collectedData: NolPayPaymentCollectableData.NolPayTagData) =
        requestPaymentInteractor(
            NolPayRequestPaymentParams(
                tag = collectedData.tag,
                transactionNo = resumeDecision.transactionNumber,
            ),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    internal suspend fun completePayment() =
        completePaymentInteractor(NolPayCompletePaymentParams(resumeDecision.completeUrl))
            .mapSuspendCatching {
                pollingInteractor(
                    AsyncStatusParams(url = resumeDecision.statusUrl, PaymentMethodType.NOL_PAY.name),
                ).mapLatest { status ->
                    resumePayment(status.resumeToken)
                }.catch { throwable ->
                    handleError(throwable)
                }.collect {
                    _componentStep.emit(NolPayPaymentStep.PaymentRequested)
                }
            }
}
