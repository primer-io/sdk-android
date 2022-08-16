package io.primer.android.components.presentation

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.payments.PaymentInputDataChangedInteractor
import io.primer.android.components.domain.payments.PaymentInputDataTypeValidateInteractor
import io.primer.android.components.domain.payments.models.PaymentRawDataParams
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

internal interface RawDataDelegate {

    fun onInputDataChanged(
        paymentMethodType: String,
        inputData: PrimerRawData
    )

    fun cleanup()
}

internal class DefaultRawDataDelegate(
    private val paymentInputDataTypeValidateInteractor: PaymentInputDataTypeValidateInteractor,
    private val paymentInputDataChangedInteractor: PaymentInputDataChangedInteractor
) : RawDataDelegate {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

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

    override fun cleanup() = scope.coroutineContext.cancelChildren()
}
