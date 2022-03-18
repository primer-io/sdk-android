package io.primer.android.components.manager

import io.primer.android.ExperimentalPrimerApi
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.domain.core.models.card.CardInputData
import io.primer.android.components.ui.widgets.PrimerCardNumberEditText
import io.primer.android.components.ui.widgets.PrimerCvvEditText
import io.primer.android.components.ui.widgets.PrimerEditText
import io.primer.android.components.ui.widgets.PrimerInputElementCardNumberListener
import io.primer.android.components.ui.widgets.PrimerTextChangedListener
import io.primer.android.components.ui.widgets.elements.PrimerInputElement
import io.primer.android.model.dto.PrimerInputFieldType
import io.primer.android.data.configuration.models.PrimerPaymentMethodType

@ExperimentalPrimerApi
interface PrimerUniversalCheckoutCardManagerInterface {
    fun getRequiredInputElementTypes(): List<PrimerInputFieldType>?
    fun setInputElements(elements: List<PrimerInputElement>)
    fun tokenize()
    fun isCardFormValid(): Boolean
    fun setCardManagerListener(listener: PrimerCardManagerListener)
}

@ExperimentalPrimerApi
interface PrimerCardManagerListener {
    fun onCardValidationChanged(isCardFormValid: Boolean)
}

@ExperimentalPrimerApi
class PrimerCardManager private constructor() :
    PrimerUniversalCheckoutCardManagerInterface,
    PrimerTextChangedListener {

    private val inputElements = mutableListOf<PrimerInputElement>()
    private var cardFormValid: Boolean = false
    private var listener: PrimerCardManagerListener? = null

    override fun getRequiredInputElementTypes(): List<PrimerInputFieldType>? {
        return PrimerHeadlessUniversalCheckout.instance.listRequiredInputElementTypes(
            PrimerPaymentMethodType.PAYMENT_CARD
        )
    }

    override fun setInputElements(elements: List<PrimerInputElement>) {
        inputElements.clear()
        inputElements.addAll(elements)
        setupInputElementsListeners()
    }

    override fun tokenize() {
        PrimerHeadlessUniversalCheckout.instance.startTokenization(
            PrimerPaymentMethodType.PAYMENT_CARD,
            CardInputData(
                getInputElementValue(PrimerInputFieldType.CARD_NUMBER).toString(),
                getInputElementValue(PrimerInputFieldType.EXPIRY_DATE).toString(),
                getInputElementValue(PrimerInputFieldType.CVV).toString(),
                getInputElementValue(PrimerInputFieldType.CARDHOLDER_NAME),
                getInputElementValue(PrimerInputFieldType.POSTAL_CODE),
            )
        )
    }

    override fun isCardFormValid() =
        inputElements.isNotEmpty() && inputElements.all { it.isValid() }

    override fun setCardManagerListener(listener: PrimerCardManagerListener) {
        this.listener = listener
    }

    override fun onTextChanged(text: String?) {
        if (cardFormValid != isCardFormValid()) {
            this.cardFormValid = isCardFormValid()
            listener?.onCardValidationChanged(isCardFormValid())
        }
    }

    private fun setupInputElementsListeners() {
        inputElements.forEach { (it as? PrimerEditText)?.setTextChangedListener(this) }

        val cardNumberEditText =
            inputElements.find { it.getType() == PrimerInputFieldType.CARD_NUMBER }
                as? PrimerCardNumberEditText

        val cvvEditText =
            inputElements.find { it.getType() == PrimerInputFieldType.CVV }
                as? PrimerCvvEditText

        cardNumberEditText?.setCvvListener(object : PrimerInputElementCardNumberListener {
            override fun inputElementCardChanged(cardNumber: String) {
                cvvEditText?.onCardNumberChanged(cardNumber)
            }
        })
    }

    private fun getInputElementValue(inputFieldType: PrimerInputFieldType): String? {
        return (
            inputElements.firstOrNull { it.getType() == inputFieldType } as? PrimerEditText
            )?.getSanitizedText()?.toString()
    }

    companion object {
        fun newInstance(): PrimerUniversalCheckoutCardManagerInterface = PrimerCardManager()
    }
}
