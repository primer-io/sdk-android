package io.primer.android.components.presentation.paymentMethods.raw

import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.inputs.PaymentInputTypesInteractor
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.PaymentRawDataChangedInteractor
import io.primer.android.components.domain.payments.PaymentRawDataTypeValidateInteractor
import io.primer.android.components.domain.payments.PaymentTokenizationInteractor
import io.primer.android.components.domain.payments.models.PaymentRawDataParams
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import io.primer.android.components.domain.payments.paymentMethods.PaymentRawDataValidationInteractor
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.components.ui.navigation.Navigator
import io.primer.android.domain.error.models.HUCError
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.events.EventDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

internal interface RawDataDelegate {

    fun startTokenization(
        type: String,
        rawData: PrimerRawData
    )

    fun onRawDataChanged(
        paymentMethodType: String,
        rawData: PrimerRawData
    )

    fun submit(paymentMethodType: String, rawData: PrimerRawData?)

    fun getRequiredInputElementTypes(paymentMethodType: String): List<PrimerInputElementType>

    fun setListener(listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener)

    fun cleanup()
}

internal class DefaultRawDataManagerDelegate(
    private val tokenizationInteractor: TokenizationInteractor,
    private val paymentTokenizationInteractor: PaymentTokenizationInteractor,
    private val paymentRawDataChangedInteractor: PaymentRawDataChangedInteractor,
    private val paymentRawDataTypeValidateInteractor: PaymentRawDataTypeValidateInteractor,
    private val paymentRawDataValidationInteractor: PaymentRawDataValidationInteractor,
    private val paymentInputTypesInteractor: PaymentInputTypesInteractor,
    private val eventDispatcher: EventDispatcher,
    private val navigator: Navigator
) :
    RawDataDelegate, EventBus.EventListener {

    private val scope = CoroutineScope(SupervisorJob())
    private var subscription: EventBus.SubscriptionHandle? = null
    private var listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener? = null

    init {
        subscription = EventBus.subscribe(this)
    }

    override fun onEvent(e: CheckoutEvent) {
        when (e) {
            is CheckoutEvent.HucValidationError -> {
                listener?.onValidationChanged(e.errors.isEmpty(), e.errors)
                e.errors.forEach { error ->
                    PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
                        MessageAnalyticsParams(
                            MessageType.VALIDATION_FAILED,
                            error.description,
                            Severity.INFO,
                            error.diagnosticsId
                        )
                    )
                }
            }
            is CheckoutEvent.HucMetadataChanged ->
                listener?.onMetadataChanged(e.metadata)
            is CheckoutEvent.Start3DSMock -> {
                navigator.open3DSMockScreen()
            }
            else -> Unit
        }
    }

    override fun startTokenization(
        type: String,
        rawData: PrimerRawData
    ) {
        scope.launch {
            paymentTokenizationInteractor.execute(
                PaymentTokenizationDescriptorParams(type, rawData)
            ).flatMapLatest {
                tokenizationInteractor.executeV2(
                    TokenizationParamsV2(
                        it,
                        PrimerSessionIntent.CHECKOUT
                    )
                )
            }.catch { }.collect { }
        }
    }

    override fun onRawDataChanged(
        paymentMethodType: String,
        rawData: PrimerRawData
    ) {
        scope.launch {
            paymentRawDataTypeValidateInteractor(
                PaymentRawDataParams(
                    paymentMethodType,
                    rawData
                )
            ).flatMapLatest {
                paymentRawDataChangedInteractor(
                    PaymentTokenizationDescriptorParams(
                        paymentMethodType,
                        rawData
                    )
                )
            }.catch {}.collect {}
        }
    }

    override fun submit(paymentMethodType: String, rawData: PrimerRawData?) {
        scope.launch {
            rawData?.let {
                paymentRawDataValidationInteractor.execute(
                    PaymentTokenizationDescriptorParams(
                        paymentMethodType,
                        rawData
                    )
                ).onStart {
                    eventDispatcher.dispatchEvent(
                        CheckoutEvent.PreparationStarted(paymentMethodType)
                    )
                }.mapLatest {
                    when (it.isNullOrEmpty()) {
                        true -> startTokenization(paymentMethodType, rawData)
                        else -> PrimerHeadlessUniversalCheckout.instance.emitError(
                            HUCError.InvalidRawDataError
                        )
                    }
                }.collect()
            } ?: run {
                PrimerHeadlessUniversalCheckout.instance.emitError(HUCError.InvalidRawDataError)
            }
        }
    }

    override fun getRequiredInputElementTypes(paymentMethodType: String) =
        paymentInputTypesInteractor.execute(paymentMethodType)

    override fun setListener(listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener) {
        this.listener = listener
    }

    override fun cleanup() {
        this.listener = null
        scope.coroutineContext.cancelChildren()
    }

    fun reset() {
        this.listener = null
        subscription?.unregister()
        subscription = null
        scope.coroutineContext.cancelChildren()
    }
}
