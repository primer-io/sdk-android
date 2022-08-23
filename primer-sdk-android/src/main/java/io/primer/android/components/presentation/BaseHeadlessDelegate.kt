package io.primer.android.components.presentation

import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.card.PrimerRawCardData
import io.primer.android.components.domain.inputs.PaymentInputTypesInteractor
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.PaymentInputDataTypeValidateInteractor
import io.primer.android.components.domain.payments.PaymentTokenizationInteractor
import io.primer.android.components.domain.payments.models.PaymentRawDataParams
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParams
import io.primer.android.ui.CardNetwork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

internal interface HeadlessDelegate {

    fun getRequiredInputElementTypes(paymentMethodType: String): List<PrimerInputElementType>?

    fun startTokenization(
        type: String,
        rawData: PrimerRawData
    )

    fun dispatchAction(
        type: String,
        rawData: PrimerRawData,
        submit: Boolean,
        completion: ((Error?) -> Unit) = {},
    )
}

internal open class DefaultHeadlessDelegate(
    private val tokenizationInteractor: TokenizationInteractor,
    private val paymentInputTypesInteractor: PaymentInputTypesInteractor,
    private val paymentTokenizationInteractor: PaymentTokenizationInteractor,
    private val paymentInputDataTypeValidateInteractor: PaymentInputDataTypeValidateInteractor,
    private val actionInteractor: ActionInteractor,
) : HeadlessDelegate {

    protected val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    override fun getRequiredInputElementTypes(paymentMethodType: String) =
        paymentInputTypesInteractor.execute(paymentMethodType)

    override fun startTokenization(
        type: String,
        rawData: PrimerRawData
    ) {
        scope.launch {
            paymentTokenizationInteractor.execute(
                PaymentTokenizationDescriptorParams(type, rawData)
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

    override fun dispatchAction(
        type: String,
        rawData: PrimerRawData,
        submit: Boolean,
        completion: ((Error?) -> Unit),
    ) {
        scope.launch {
            paymentInputDataTypeValidateInteractor(
                PaymentRawDataParams(
                    type,
                    rawData,
                    submit
                )
            )
                .flatMapLatest {
                    actionInteractor(getActionUpdateParams(type, rawData))
                }.catch {
                    completion(Error(it))
                }
                .collect {
                    completion(null)
                }
        }
    }

    private fun getActionUpdateParams(
        type: String,
        rawData: PrimerRawData
    ) =
        when (rawData) {
            is PrimerRawCardData -> {
                val cardType = CardNetwork.lookup(rawData.cardNumber).type
                if (cardType == CardNetwork.Type.UNKNOWN) {
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
