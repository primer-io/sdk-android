package io.primer.android.components.presentation

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.inputs.PaymentInputTypesInteractor
import io.primer.android.components.domain.payments.PaymentInputDataChangedInteractor
import io.primer.android.components.domain.payments.PaymentInputDataTypeValidateInteractor
import io.primer.android.components.domain.payments.PaymentTokenizationInteractor
import io.primer.android.components.domain.payments.models.PaymentRawDataParams
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.payments.async.AsyncPaymentMethodInteractor
import io.primer.android.domain.payments.async.models.AsyncMethodParams
import io.primer.android.domain.tokenization.TokenizationInteractor
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

internal interface RawDataDelegate : HeadlessDelegate {

    fun onInputDataChanged(
        paymentMethodType: String,
        inputData: PrimerRawData
    )

    fun startAsyncFlow(url: String, paymentMethodType: String)

    fun cleanup()
}

internal class DefaultRawDataDelegate(
    tokenizationInteractor: TokenizationInteractor,
    paymentInputTypesInteractor: PaymentInputTypesInteractor,
    paymentTokenizationInteractor: PaymentTokenizationInteractor,
    actionInteractor: ActionInteractor,
    private val paymentInputDataChangedInteractor: PaymentInputDataChangedInteractor,
    private val paymentInputDataTypeValidateInteractor: PaymentInputDataTypeValidateInteractor,
    private val asyncPaymentMethodInteractor: AsyncPaymentMethodInteractor,
) : DefaultHeadlessDelegate(
    tokenizationInteractor,
    paymentInputTypesInteractor,
    paymentTokenizationInteractor,
    paymentInputDataTypeValidateInteractor,
    actionInteractor
),
    RawDataDelegate {

    override fun onInputDataChanged(
        paymentMethodType: String,
        inputData: PrimerRawData
    ) {
        scope.launch {
            paymentInputDataTypeValidateInteractor(
                PaymentRawDataParams(
                    paymentMethodType,
                    inputData,
                    false
                )
            ).flatMapLatest {
                paymentInputDataChangedInteractor(
                    PaymentTokenizationDescriptorParams(
                        paymentMethodType,
                        inputData
                    )
                )
            }.catch { it.printStackTrace() }.collect {}
        }
    }

    override fun startAsyncFlow(url: String, paymentMethodType: String) {
        scope.launch {
            asyncPaymentMethodInteractor(AsyncMethodParams(url, paymentMethodType)).catch { }
                .collect {}
        }
    }

    override fun cleanup() = scope.coroutineContext.cancelChildren()
}
