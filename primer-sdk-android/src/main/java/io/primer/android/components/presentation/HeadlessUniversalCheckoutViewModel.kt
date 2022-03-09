package io.primer.android.components.presentation

import io.primer.android.PaymentMethodIntent
import io.primer.android.components.domain.inputs.PaymentInputTypesInteractor
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutInputData
import io.primer.android.components.domain.payments.PaymentTokenizationInteractor
import io.primer.android.components.domain.payments.PaymentsTypesInteractor
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import io.primer.android.domain.base.None
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.model.dto.PrimerPaymentMethodType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

internal class HeadlessUniversalCheckoutViewModel(
    private val tokenizationInteractor: TokenizationInteractor,
    private val paymentsTypesInteractor: PaymentsTypesInteractor,
    private val paymentTokenizationInteractor: PaymentTokenizationInteractor,
    private val paymentInputTypesInteractor: PaymentInputTypesInteractor,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    fun start() {
        scope.launch { paymentsTypesInteractor(None()).collect {} }
    }

    fun listRequiredInputElementTypes(paymentMethodType: PrimerPaymentMethodType) =
        paymentInputTypesInteractor.execute(paymentMethodType)

    fun startTokenization(
        type: PrimerPaymentMethodType,
        inputData: PrimerHeadlessUniversalCheckoutInputData
    ) {
        scope.launch {
            paymentTokenizationInteractor.execute(
                PaymentTokenizationDescriptorParams(type, inputData)
            ).flatMapLatest {
                tokenizationInteractor(
                    TokenizationParams(
                        it,
                        PaymentMethodIntent.CHECKOUT,
                        false
                    )
                )
            }.catch { }.collect { }
        }
    }

    fun clear() = scope.coroutineContext.job.cancelChildren()
}
