package io.primer.android.components.presentation.paymentMethods.raw

import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

internal interface RawDataDelegate<T : PrimerRawData> {

    fun start()

    fun startTokenization(
        type: String,
        rawData: T
    )

    fun onRawDataChanged(
        paymentMethodType: String,
        oldRawData: T?,
        rawData: T
    )

    fun submit(paymentMethodType: String, rawData: T?)

    fun getRequiredInputElementTypes(paymentMethodType: String): List<PrimerInputElementType>

    fun setListener(listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener)

    fun cleanup(paymentMethodType: String)

    fun reset()
}

@Suppress("LongParameterList")
internal class DefaultRawDataManagerDelegate(
    private val tokenizationInteractor: TokenizationInteractor,
    private val paymentTokenizationInteractor: PaymentTokenizationInteractor,
    private val paymentRawDataChangedInteractor: PaymentRawDataChangedInteractor,
    private val paymentRawDataTypeValidateInteractor: PaymentRawDataTypeValidateInteractor,
    private val paymentRawDataValidationInteractor: PaymentRawDataValidationInteractor,
    private val paymentInputTypesInteractor: PaymentInputTypesInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val eventDispatcher: EventDispatcher
) : RawDataDelegate<PrimerRawData>, EventBus.EventListener {

    private val scope = CoroutineScope(SupervisorJob())
    private var subscription: EventBus.SubscriptionHandle? = null
    private var listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener? = null

    override fun start() {
        subscription = EventBus.subscribe(this)
    }

    override fun onEvent(e: CheckoutEvent) {
        when (e) {
            is CheckoutEvent.HucValidationError -> {
                listener?.onValidationChanged(e.errors.isEmpty(), e.errors)
            }

            is CheckoutEvent.HucMetadataChanged ->
                listener?.onMetadataChanged(e.metadata)

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
        oldRawData: PrimerRawData?,
        rawData: PrimerRawData
    ) {
        logSdkAnalyticsEvent(
            RawDataManagerAnalyticsConstants.SET_RAW_DATA_METHOD,
            mapOf(RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType)
        )
        scope.launch {
            paymentRawDataTypeValidateInteractor(
                PaymentRawDataParams(
                    paymentMethodType,
                    rawData
                )
            ).mapLatest {
                paymentRawDataChangedInteractor(
                    PaymentTokenizationDescriptorParams(
                        paymentMethodType,
                        rawData
                    )
                ).getOrThrow()
            }.catch { it.printStackTrace() }.collect {}
        }
    }

    override fun submit(paymentMethodType: String, rawData: PrimerRawData?) {
        scope.launch {
            logSdkAnalyticsEvent(
                RawDataManagerAnalyticsConstants.SUBMIT_METHOD,
                mapOf(
                    RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType
                )
            )
            rawData?.let {
                flowOf(
                    paymentRawDataValidationInteractor(
                        PaymentTokenizationDescriptorParams(
                            paymentMethodType,
                            rawData
                        )
                    ).getOrThrow()
                ).onStart {
                    eventDispatcher.dispatchEvent(
                        CheckoutEvent.PreparationStarted(paymentMethodType)
                    )
                }.mapLatest {
                    when (it.isEmpty()) {
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

    override fun getRequiredInputElementTypes(paymentMethodType: String):
        List<PrimerInputElementType> {
        logSdkAnalyticsEvent(
            RawDataManagerAnalyticsConstants.GET_INPUT_ELEMENT_TYPES_METHOD,
            mapOf(RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType)
        )
        return paymentInputTypesInteractor.execute(paymentMethodType)
    }

    override fun setListener(listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener) {
        logSdkAnalyticsEvent(RawDataManagerAnalyticsConstants.SET_LISTENER_METHOD)
        this.listener = listener
    }

    override fun cleanup(paymentMethodType: String) {
        logSdkAnalyticsEvent(
            RawDataManagerAnalyticsConstants.CLEANUP_METHOD,
            mapOf(RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType)
        )
        this.listener = null
        scope.coroutineContext.cancelChildren()
    }

    override fun reset() {
        this.listener = null
        subscription?.unregister()
        subscription = null
        scope.coroutineContext.cancelChildren()
    }

    private fun logSdkAnalyticsEvent(
        methodName: String,
        params: Map<String, String> = emptyMap()
    ) = scope.launch {
        analyticsInteractor(
            SdkFunctionParams(
                methodName,
                params + mapOf(
                    "category" to PrimerPaymentMethodManagerCategory.RAW_DATA.name
                )
            )
        ).collect {}
    }
}
