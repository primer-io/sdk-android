package io.primer.android.components.manager.raw

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.raw.DefaultRawDataManagerDelegate
import io.primer.android.data.payments.configure.PrimerInitializationData
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import org.koin.core.component.inject

class PrimerHeadlessUniversalCheckoutRawDataManager private constructor(
    private val paymentMethodType: String
) : PrimerHeadlessUniversalCheckoutRawDataManagerInterface, DIAppComponent {

    private val rawDelegate: DefaultRawDataManagerDelegate by inject()
    private val headlessManagerDelegate: DefaultHeadlessManagerDelegate by inject()

    init {
        headlessManagerDelegate.init(paymentMethodType, PrimerPaymentMethodManagerCategory.RAW_DATA)
    }

    private var rawData: PrimerRawData? = null

    override fun configure(completion: (PrimerInitializationData?, PrimerError?) -> Unit) {
        PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                mapOf(
                    "paymentMethodType" to paymentMethodType,
                    "category" to PrimerPaymentMethodManagerCategory.RAW_DATA.name
                )
            )
        )
        headlessManagerDelegate.configure(paymentMethodType, completion)
    }

    override fun setListener(
        listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener
    ) {
        PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                mapOf(
                    "paymentMethodType" to paymentMethodType,
                    "category" to PrimerPaymentMethodManagerCategory.RAW_DATA.name
                )
            )
        )
        rawDelegate.setListener(listener)
    }

    override fun submit() {
        PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                mapOf(
                    "paymentMethodType" to paymentMethodType,
                    "category" to PrimerPaymentMethodManagerCategory.RAW_DATA.name
                )
            )
        )
        rawDelegate.submit(paymentMethodType, rawData)
    }

    override fun setRawData(rawData: PrimerRawData) {
        PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                mapOf(
                    "paymentMethodType" to paymentMethodType,
                    "category" to PrimerPaymentMethodManagerCategory.RAW_DATA.name
                )
            )
        )
        this.rawData = rawData
        headlessManagerDelegate.dispatchRawDataAction(paymentMethodType, rawData, false)
        rawDelegate.onRawDataChanged(paymentMethodType, rawData)
    }

    override fun getRequiredInputElementTypes(): List<PrimerInputElementType> {
        PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                mapOf(
                    "paymentMethodType" to paymentMethodType,
                    "category" to PrimerPaymentMethodManagerCategory.RAW_DATA.name
                )
            )
        )
        return rawDelegate.getRequiredInputElementTypes(paymentMethodType)
    }

    override fun cleanup() {
        PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                mapOf(
                    "paymentMethodType" to paymentMethodType,
                    "category" to PrimerPaymentMethodManagerCategory.RAW_DATA.name
                )
            )
        )
        rawDelegate.cleanup()
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
