package io.primer.android.components.manager.raw

import io.primer.android.ExperimentalPrimerApi
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.presentation.DefaultRawDataDelegate
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.error.models.HUCError
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import org.koin.core.component.inject

@ExperimentalPrimerApi
class PrimerHeadlessUniversalCheckoutRawDataManager private constructor(
    val paymentMethodType: String
) : PrimerHeadlessUniversalCheckoutRawDataManagerInterface, DIAppComponent {

    init {
        if (
            listOf(
                PaymentMethodType.PAYMENT_CARD,
                PaymentMethodType.XENDIT_OVO,
                PaymentMethodType.ADYEN_MBWAY,
                PaymentMethodType.ADYEN_BANCONTACT_CARD
            ).map { it.name }.contains(paymentMethodType).not()
        ) {
            throw UnsupportedPaymentMethodException(paymentMethodType)
        }
    }

    private val eventBusListener = object : EventBus.EventListener {
        override fun onEvent(e: CheckoutEvent) {
            when (e) {
                is CheckoutEvent.HucValidationError ->
                    listener?.onValidationChanged(e.errors.isEmpty(), e.errors)
                is CheckoutEvent.HucMetadataChanged ->
                    listener?.onMetadataChanged(e.metadata)
                is CheckoutEvent.StartAsyncFlow -> delegate.startAsyncFlow(
                    e.statusUrl,
                    e.paymentMethodType
                )
                else -> Unit
            }
        }
    }

    private var subscription: EventBus.SubscriptionHandle? = null
    private var listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener? = null
    private var rawData: PrimerRawData? = null

    private val delegate: DefaultRawDataDelegate by inject()

    override fun setManagerListener(
        listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener
    ) {
        subscription?.unregister()
        subscription = null
        subscription = EventBus.subscribe(eventBusListener)
        this.listener = listener
    }

    override fun submit() {
        rawData?.let {
            PrimerHeadlessUniversalCheckout.instance.startTokenization(
                paymentMethodType,
                it
            )
        } ?: PrimerHeadlessUniversalCheckout.instance.emitError(HUCError.InvalidRawDataError)
    }

    override fun setRawData(rawData: PrimerRawData) {
        this.rawData = rawData
        delegate.dispatchAction(paymentMethodType, rawData, false)
        delegate.onInputDataChanged(paymentMethodType, rawData)
    }

    override fun getRequiredInputElementTypes(): List<PrimerInputElementType> {
        return delegate.getRequiredInputElementTypes(paymentMethodType).orEmpty()
    }

    override fun cleanup() {
        subscription?.unregister()
        subscription = null
        delegate.cleanup()
        listener = null
    }

    companion object {

        /**
         * Creates raw data manager tied to current session for a given payment method.
         *
         * @param paymentMethodType the payment method flow to be shown.
         * @throws UnsupportedPaymentMethodException
         */
        @Throws(UnsupportedPaymentMethodException::class)
        fun newInstance(paymentMethodType: String):
            PrimerHeadlessUniversalCheckoutRawDataManagerInterface =
            PrimerHeadlessUniversalCheckoutRawDataManager(
                paymentMethodType
            )
    }
}
