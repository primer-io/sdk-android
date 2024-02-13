package io.primer.android.components.manager

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardData
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.raw.RawDataDelegate
import io.primer.android.components.ui.widgets.PrimerCardNumberEditText
import io.primer.android.components.ui.widgets.PrimerCvvEditText
import io.primer.android.components.ui.widgets.PrimerEditText
import io.primer.android.components.ui.widgets.PrimerInputElementCardNumberListener
import io.primer.android.components.ui.widgets.PrimerTextChangedListener
import io.primer.android.components.ui.widgets.elements.PrimerInputElement
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.inject
import io.primer.android.di.extension.resolve
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.utils.removeSpaces
import java.util.Calendar

private const val DEPRECATION_MESSAGE =
    """This integration will no longer receive ongoing maintenance and will be removed in the future.
        Use PrimerHeadlessUniversalCheckoutRawDataManager and corresponding listeners instead."""

@Deprecated(DEPRECATION_MESSAGE)
interface PrimerHeadlessUniversalCheckoutCardComponentsManagerInterface {
    fun getRequiredInputElementTypes(): List<PrimerInputElementType>
    fun setInputElements(elements: List<PrimerInputElement>)
    fun submit()
    fun isCardFormValid(): Boolean
    fun setCardManagerListener(
        listener: PrimerHeadlessUniversalCheckoutCardComponentsManagerListener
    )
}

@Deprecated(DEPRECATION_MESSAGE)
fun interface PrimerHeadlessUniversalCheckoutCardComponentsManagerListener {
    fun onCardValidationChanged(isCardFormValid: Boolean)
}

@Deprecated(DEPRECATION_MESSAGE)
class PrimerHeadlessUniversalCheckoutCardComponentsManager
private constructor(private val paymentMethodType: String) :
    PrimerHeadlessUniversalCheckoutCardComponentsManagerInterface,
    PrimerTextChangedListener,
    DISdkComponent {

    private val rawDelegate: RawDataDelegate<PrimerRawData> by lazy {
        when (paymentMethodType) {
            PaymentMethodType.PAYMENT_CARD.name -> resolve(paymentMethodType)
            else -> resolve()
        }
    }
    private val headlessManagerDelegate: DefaultHeadlessManagerDelegate by inject()

    private val inputElements = mutableListOf<PrimerInputElement>()
    private var cardFormValid: Boolean = false
    private var listener: PrimerHeadlessUniversalCheckoutCardComponentsManagerListener? = null

    init {
        headlessManagerDelegate.init(
            paymentMethodType,
            PrimerPaymentMethodManagerCategory.CARD_COMPONENTS
        )
    }

    override fun getRequiredInputElementTypes(): List<PrimerInputElementType> {
        PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                mapOf(
                    "paymentMethodType" to paymentMethodType,
                    "category" to PrimerPaymentMethodManagerCategory.CARD_COMPONENTS.name
                )
            )
        )
        return rawDelegate.getRequiredInputElementTypes(paymentMethodType)
    }

    override fun setInputElements(elements: List<PrimerInputElement>) {
        PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                mapOf(
                    "paymentMethodType" to paymentMethodType,
                    "category" to PrimerPaymentMethodManagerCategory.CARD_COMPONENTS.name
                )
            )
        )
        inputElements.clear()
        inputElements.addAll(elements)
        setupInputElementsListeners()
    }

    override fun submit() {
        PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                mapOf(
                    "paymentMethodType" to paymentMethodType,
                    "category" to PrimerPaymentMethodManagerCategory.CARD_COMPONENTS.name
                )
            )
        )
        rawDelegate.startTokenization(paymentMethodType, getRawData(paymentMethodType))
    }

    override fun isCardFormValid() =
        inputElements.isNotEmpty() && inputElements.all { it.isValid() }

    override fun setCardManagerListener(
        listener: PrimerHeadlessUniversalCheckoutCardComponentsManagerListener
    ) {
        PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                mapOf(
                    "paymentMethodType" to paymentMethodType,
                    "category" to PrimerPaymentMethodManagerCategory.CARD_COMPONENTS.name
                )
            )
        )
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
            inputElements.find { it.getType() == PrimerInputElementType.CARD_NUMBER }
                as? PrimerCardNumberEditText

        val cvvEditText =
            inputElements.find { it.getType() == PrimerInputElementType.CVV }
                as? PrimerCvvEditText

        cardNumberEditText?.setCvvListener(object : PrimerInputElementCardNumberListener {
            override fun inputElementCardChanged(cardNumber: String) {
                cvvEditText?.onCardNumberChanged(cardNumber)
            }
        })
    }

    private fun getInputElementValue(inputElementType: PrimerInputElementType): String? {
        return (
            inputElements.firstOrNull { it.getType() == inputElementType } as? PrimerEditText
            )?.getSanitizedText()?.toString()
    }

    private fun getRawData(paymentMethodType: String) = when (paymentMethodType) {
        PaymentMethodType.PAYMENT_CARD.name -> PrimerCardData(
            getInputElementValue(PrimerInputElementType.CARD_NUMBER).toString().removeSpaces(),
            getExpiryDate(),
            getInputElementValue(PrimerInputElementType.CVV).toString(),
            getInputElementValue(PrimerInputElementType.CARDHOLDER_NAME)
        )

        PaymentMethodType.ADYEN_BANCONTACT_CARD.name -> PrimerBancontactCardData(
            getInputElementValue(PrimerInputElementType.CARD_NUMBER).toString().removeSpaces(),
            getExpiryDate(),
            getInputElementValue(PrimerInputElementType.CARDHOLDER_NAME).toString()
        )

        else -> throw UnsupportedPaymentMethodException(paymentMethodType)
    }

    private fun getExpiryDate(): String {
        return "${getExpiryMonth()}/${getExpiryYear()}"
    }

    private fun getExpiryMonth() = getInputElementValue(PrimerInputElementType.EXPIRY_DATE)
        .toString()
        .split("/").getOrElse(0) { "" }

    private fun getExpiryYear() = getInputElementValue(PrimerInputElementType.EXPIRY_DATE)
        .toString()
        .split("/").getOrElse(1) { "" }
        .let { "${Calendar.getInstance().get(Calendar.YEAR).div(YEAR_DIVIDER)}$it" }

    companion object {
        private const val YEAR_DIVIDER = 100

        @Deprecated(DEPRECATION_MESSAGE)
        @Throws(SdkUninitializedException::class, UnsupportedPaymentMethodException::class)
        @JvmStatic
        fun newInstance(paymentMethodType: String):
            PrimerHeadlessUniversalCheckoutCardComponentsManagerInterface =
            PrimerHeadlessUniversalCheckoutCardComponentsManager(
                paymentMethodType
            )
    }
}
