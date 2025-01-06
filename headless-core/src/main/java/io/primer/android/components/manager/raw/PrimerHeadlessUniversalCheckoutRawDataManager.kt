package io.primer.android.components.manager.raw

import io.primer.android.PrimerSessionIntent
import io.primer.android.components.RawDataDelegate
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.paymentmethods.PrimerInitializationData
import io.primer.android.paymentmethods.PrimerRawData

class PrimerHeadlessUniversalCheckoutRawDataManager private constructor(
    private val paymentMethodType: String,
) : PrimerHeadlessUniversalCheckoutRawDataManagerInterface, DISdkComponent {
    private val rawDelegate: RawDataDelegate<PrimerRawData> by lazy {
        resolve()
    }
    private var rawData: PrimerRawData? = null

    init {
        rawDelegate.init(paymentMethodType, PrimerPaymentMethodManagerCategory.RAW_DATA)
        rawDelegate.start(
            context = resolve(),
            paymentMethodType = paymentMethodType,
            primerSessionIntent =
                runCatching { resolve<PrimerConfig>().paymentMethodIntent }
                    .getOrElse { PrimerSessionIntent.CHECKOUT },
        )
    }

    override fun configure(completion: (PrimerInitializationData?, PrimerError?) -> Unit) {
        rawDelegate.configure(paymentMethodType, completion)
    }

    override fun setListener(listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener) {
        rawDelegate.setListener(listener)
    }

    override fun submit() {
        rawDelegate.submit(paymentMethodType, rawData)
    }

    override fun setRawData(rawData: PrimerRawData) {
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
        fun newInstance(paymentMethodType: String): PrimerHeadlessUniversalCheckoutRawDataManagerInterface =
            PrimerHeadlessUniversalCheckoutRawDataManager(paymentMethodType)
    }
}
