package io.primer.android.components.manager.raw

import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.raw.RawDataDelegate
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.configure.PrimerInitializationData
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.inject
import io.primer.android.di.extension.resolve
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.exception.UnsupportedPaymentMethodException

class PrimerHeadlessUniversalCheckoutRawDataManager private constructor(
    private val paymentMethodType: String
) : PrimerHeadlessUniversalCheckoutRawDataManagerInterface, DISdkComponent {

    private val headlessManagerDelegate: DefaultHeadlessManagerDelegate by inject()
    private val rawDelegate: RawDataDelegate<PrimerRawData> by lazy {
        when (paymentMethodType) {
            PaymentMethodType.PAYMENT_CARD.name -> resolve(paymentMethodType)
            else -> resolve()
        }
    }
    private var rawData: PrimerRawData? = null

    init {
        headlessManagerDelegate.init(paymentMethodType, PrimerPaymentMethodManagerCategory.RAW_DATA)
        rawDelegate.start()
    }

    override fun configure(completion: (PrimerInitializationData?, PrimerError?) -> Unit) {
        headlessManagerDelegate.configure(paymentMethodType, completion)
    }

    override fun setListener(
        listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener
    ) {
        rawDelegate.setListener(listener)
    }

    override fun submit() {
        rawDelegate.submit(paymentMethodType, rawData)
    }

    override fun setRawData(rawData: PrimerRawData) {
        headlessManagerDelegate.dispatchRawDataAction(paymentMethodType, rawData, false)
        rawDelegate.onRawDataChanged(paymentMethodType, this.rawData, rawData).also {
            this.rawData = rawData
        }
    }

    override fun getRequiredInputElementTypes(): List<PrimerInputElementType> {
        return rawDelegate.getRequiredInputElementTypes(paymentMethodType)
    }

    override fun cleanup() {
        rawDelegate.cleanup(paymentMethodType)
    }

    companion object {

        /**
         * Creates raw data manager tied to current session for a given payment method.
         *
         * @param paymentMethodType the payment method flow to be shown.
         * @throws SdkUninitializedException
         * @throws UnsupportedPaymentMethodException
         */
        @Throws(SdkUninitializedException::class, UnsupportedPaymentMethodException::class)
        @JvmStatic
        fun newInstance(paymentMethodType: String):
            PrimerHeadlessUniversalCheckoutRawDataManagerInterface =
            PrimerHeadlessUniversalCheckoutRawDataManager(
                paymentMethodType
            )
    }
}
