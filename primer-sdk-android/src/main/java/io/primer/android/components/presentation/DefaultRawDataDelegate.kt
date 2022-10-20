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
import io.primer.android.domain.payments.methods.repository.PaymentMethodsRepository
import io.primer.android.domain.rpc.retailOutlets.RetailOutletInteractor
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
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

    fun cleanup()
}

internal class DefaultRawDataDelegate(
    tokenizationInteractor: TokenizationInteractor,
    paymentInputTypesInteractor: PaymentInputTypesInteractor,
    paymentTokenizationInteractor: PaymentTokenizationInteractor,
    actionInteractor: ActionInteractor,
    asyncPaymentMethodInteractor: AsyncPaymentMethodInteractor,
    paymentMethodsRepository: PaymentMethodsRepository,
    retailOutletInteractor: RetailOutletInteractor,
    retailOutletRepository: RetailOutletRepository,
    private val paymentInputDataChangedInteractor: PaymentInputDataChangedInteractor,
    private val paymentInputDataTypeValidateInteractor: PaymentInputDataTypeValidateInteractor,
) : DefaultHeadlessDelegate(
    tokenizationInteractor,
    paymentInputTypesInteractor,
    paymentTokenizationInteractor,
    paymentInputDataTypeValidateInteractor,
    actionInteractor,
    asyncPaymentMethodInteractor,
    paymentMethodsRepository,
    retailOutletInteractor,
    retailOutletRepository,
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

    override fun cleanup() = scope.coroutineContext.cancelChildren()
}
