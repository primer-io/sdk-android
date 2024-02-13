package io.primer.android.components.presentation.paymentMethods.raw.card

import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadataState
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.PaymentInputTypesInteractor
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.PaymentTokenizationInteractor
import io.primer.android.components.domain.payments.metadata.card.CardDataMetadataRetriever
import io.primer.android.components.domain.payments.metadata.card.CardDataMetadataStateRetriever
import io.primer.android.components.domain.payments.models.PaymentTokenizationDescriptorParams
import io.primer.android.components.domain.payments.paymentMethods.PaymentRawDataValidationInteractor
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.components.presentation.paymentMethods.raw.RawDataDelegate
import io.primer.android.components.presentation.paymentMethods.raw.RawDataManagerAnalyticsConstants
import io.primer.android.components.presentation.paymentMethods.raw.RawDataManagerAnalyticsConstants.PREFERRED_NETWORK_PARAM
import io.primer.android.core.extensions.debounce
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.error.models.HUCError
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

@Suppress("LongParameterList")
internal class CardRawDataManagerDelegate(
    private val tokenizationInteractor: TokenizationInteractor,
    private val paymentTokenizationInteractor: PaymentTokenizationInteractor,
    private val cardDataMetadataRetriever: CardDataMetadataRetriever,
    private val paymentRawDataValidationInteractor: PaymentRawDataValidationInteractor,
    private val cardDataMetadataStateRetriever: CardDataMetadataStateRetriever,
    private val paymentInputTypesInteractor: PaymentInputTypesInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val eventDispatcher: EventDispatcher
) : RawDataDelegate<PrimerCardData> {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener? = null
    private var rawData: PrimerCardData? = null

    private var errors by
    Delegates.observable<List<PrimerInputValidationError>?>(null) { _, oldValue, newValue ->
        run {
            if (oldValue != newValue && newValue != null) {
                logSdkAnalyticsEvent(
                    RawDataManagerAnalyticsConstants.ON_VALIDATION_CHANGED,
                    mapOf(
                        RawDataManagerAnalyticsConstants.ON_VALIDATION_IS_VALID_PARAM
                            to newValue.isEmpty().toString(),
                        RawDataManagerAnalyticsConstants.ON_VALIDATION_IS_VALIDATION_ERRORS_PARAM
                            to newValue.toString()
                    )
                )
            }
        }
    }

    private var metadata by
    Delegates.observable<PrimerCardMetadataState?>(null) { _, oldValue, newValue ->
        run {
            if (oldValue != newValue && newValue != null) {
                logSdkAnalyticsEvent(
                    RawDataManagerAnalyticsConstants.ON_METADATA_STATE_CHANGED,
                    mapOf(
                        RawDataManagerAnalyticsConstants.ON_METADATA_STATE_STATE_PARAM
                            to newValue.toString()
                    )
                )
            }
        }
    }

    override fun start() {
        scope.launch {
            cardDataMetadataStateRetriever.metadataState.collectLatest { cardMetadataState ->
                metadata = cardMetadataState
                rawData?.let { cardData ->
                    // validate card data each time metadata state changes
                    validateRawData(cardData)
                }
                listener?.onMetadataStateChanged(cardMetadataState)
            }
        }
    }

    override fun startTokenization(type: String, rawData: PrimerCardData) {
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
        oldRawData: PrimerCardData?,
        rawData: PrimerCardData
    ) {
        scope.launch {
            logSdkAnalyticsEvent(
                RawDataManagerAnalyticsConstants.SET_RAW_DATA_METHOD,
                mapOf(
                    RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType,
                    PREFERRED_NETWORK_PARAM to rawData.cardNetwork?.name
                )
            )
        }
        val cardDataChanged =
            oldRawData?.copy(cardNetwork = null) != rawData.copy(cardNetwork = null)
        this.rawData = rawData
        // don't trigger side effects in case only card network changed
        if (cardDataChanged) {
            metadataRawDataDataUpdated(rawData)
            // validate card data each time it changes
            validateRawData(rawData)
            scope.launch {
                listener?.onMetadataChanged(cardDataMetadataRetriever.retrieveMetadata(rawData))
            }
        }
    }

    override fun submit(paymentMethodType: String, rawData: PrimerCardData?) {
        scope.launch {
            mapOf(
                RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType,
                PREFERRED_NETWORK_PARAM to rawData?.cardNetwork?.name
            ).let { params ->
                logSdkAnalyticsEvent(
                    RawDataManagerAnalyticsConstants.SUBMIT_METHOD,
                    params
                )
            }
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
        scope.launch {
            logSdkAnalyticsEvent(
                RawDataManagerAnalyticsConstants.GET_INPUT_ELEMENT_TYPES_METHOD,
                mapOf(
                    RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType
                )
            )
        }
        return paymentInputTypesInteractor.execute(paymentMethodType)
    }

    override fun setListener(listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener) {
        scope.launch {
            logSdkAnalyticsEvent(RawDataManagerAnalyticsConstants.SET_LISTENER_METHOD)
        }
        this.listener = listener
    }

    override fun cleanup(paymentMethodType: String) {
        scope.launch {
            logSdkAnalyticsEvent(
                RawDataManagerAnalyticsConstants.CLEANUP_METHOD,
                mapOf(
                    RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType
                )
            )
        }
        this.listener = null
        scope.coroutineContext.cancelChildren()
    }

    override fun reset() {
        this.listener = null
        scope.coroutineContext.cancelChildren()
    }

    private val metadataRawDataDataUpdated: (PrimerCardData) -> Unit =
        scope.debounce { rawData ->
            cardDataMetadataStateRetriever.handleInputData(rawData)
        }

    private fun validateRawData(
        cardData: PrimerCardData
    ) = scope.launch {
        paymentRawDataValidationInteractor(
            PaymentTokenizationDescriptorParams(
                PaymentMethodType.PAYMENT_CARD.name,
                cardData
            )
        ).onSuccess { validationErrors ->
            errors = validationErrors
            listener?.onValidationChanged(
                validationErrors.isEmpty(),
                validationErrors
            )
        }
    }

    private fun logSdkAnalyticsEvent(
        methodName: String,
        params: Map<String, String?> = emptyMap()
    ) = scope.launch {
        analyticsInteractor(
            SdkFunctionParams(
                methodName,
                params.filterNotNullValues() + mapOf(
                    "category" to PrimerPaymentMethodManagerCategory.RAW_DATA.name
                )
            )
        ).collect {}
    }

    private fun <K, V> Map<K, V?>.filterNotNullValues() = filterValues { it != null } as Map<K, V>
}
