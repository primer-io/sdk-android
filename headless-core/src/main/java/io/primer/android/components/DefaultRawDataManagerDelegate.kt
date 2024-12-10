package io.primer.android.components

import android.content.Context
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.utils.RawDataManagerAnalyticsConstants
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.exception.UnsupportedPaymentMethodManagerException
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.implementation.domain.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.components.implementation.errors.domain.model.HeadlessError
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.components.validation.resolvers.PaymentMethodManagerInitValidationRulesResolver
import io.primer.android.components.validation.rules.PaymentMethodManagerInitValidationData
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.extensions.debounce
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.paymentmethods.PrimerInitializationData
import io.primer.android.paymentmethods.PrimerRawData
import io.primer.android.paymentmethods.core.composer.RawDataPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.registry.PaymentMethodComposerRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessDataInitializable
import io.primer.android.payments.core.helpers.PreparationStartHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.properties.Delegates

interface RawDataDelegate<T : PrimerRawData> {

    @Throws(
        SdkUninitializedException::class,
        UnsupportedPaymentMethodManagerException::class,
        UnsupportedPaymentMethodException::class
    )
    fun init(paymentMethodType: String, category: PrimerPaymentMethodManagerCategory)

    fun start(context: Context, paymentMethodType: String, primerSessionIntent: PrimerSessionIntent)

    fun configure(paymentMethodType: String, completion: (PrimerInitializationData?, PrimerError?) -> Unit)

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

    fun cleanup(paymentMethodType: String?)
}

@Suppress("LongParameterList")
internal class DefaultRawDataManagerDelegate(
    private val initValidationRulesResolver: PaymentMethodManagerInitValidationRulesResolver,
    private val paymentInputTypesInteractor: PaymentInputTypesInteractor,
    private val paymentMethodMapper: PrimerHeadlessUniversalCheckoutPaymentMethodMapper,
    private val analyticsInteractor: AnalyticsInteractor,
    private val composerRegistry: PaymentMethodComposerRegistry,
    private val providerFactoryRegistry: PaymentMethodProviderFactoryRegistry,
    private val paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry,
    private val actionInteractor: ActionInteractor,
    private val logReporter: LogReporter,
    private val preparationStartHandler: PreparationStartHandler
) : RawDataDelegate<PrimerRawData> {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var listener: PrimerHeadlessUniversalCheckoutRawDataManagerListener? = null
    private var composer: RawDataPaymentMethodComponent<PrimerCollectableData> by Delegates.notNull()

    val collector = scope.debounce<Pair<String, PrimerRawData>> {
        onRawDataChangedImpl(
            paymentMethodType = it.first,
            rawData = it.second
        )
    }

    override fun init(paymentMethodType: String, category: PrimerPaymentMethodManagerCategory) {
        logSdkAnalyticsEvent(
            methodName = RawDataManagerAnalyticsConstants.NEW_INSTANCE_METHOD,
            params = mapOf(RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType)
        )

        scope.launch {
            val validationResults = initValidationRulesResolver.resolve().rules.map {
                it.validate(
                    PaymentMethodManagerInitValidationData(
                        paymentMethodType,
                        category
                    )
                )
            }

            validationResults.filterIsInstance<ValidationResult.Failure>()
                .forEach { validationResult ->
                    throw validationResult.exception
                }
        }
    }

    override fun start(context: Context, paymentMethodType: String, primerSessionIntent: PrimerSessionIntent) {
        composerRegistry.unregister(paymentMethodType)
        composer = providerFactoryRegistry.create(
            paymentMethodType = paymentMethodType,
            sessionIntent = primerSessionIntent
        ) as RawDataPaymentMethodComponent<PrimerCollectableData>

        composer.let { composerRegistry.register(paymentMethodType, it) }

        scope.launch {
            val uiEventable = composer as? UiEventable
            uiEventable?.uiEvent?.collect { event ->
                when (event) {
                    is ComposerUiEvent.Navigate -> {
                        (
                            paymentMethodNavigationFactoryRegistry.create(paymentMethodType) as?
                                PaymentMethodContextNavigationHandler
                            )
                            ?.getSupportedNavigators(context = context)
                            ?.firstOrNull { it.canHandle(event.params) }?.navigate(event.params)
                            ?: println("Navigation handler for ${event.params} not found.")
                    }

                    else -> {
                        Unit
                    }
                }
            }
        }

        scope.launch {
            launch {
                composer.componentInputValidations.collectLatest { validationErrors ->
                    listener?.onValidationChanged(
                        isValid = validationErrors.isEmpty(),
                        errors = validationErrors
                    )
                }
            }

            launch {
                composer.metadataStateFlow.distinctUntilChanged().collectLatest { metadataState ->
                    logSdkAnalyticsEvent(
                        RawDataManagerAnalyticsConstants.ON_METADATA_STATE_CHANGED,
                        mapOf(
                            RawDataManagerAnalyticsConstants.ON_METADATA_STATE_STATE_PARAM
                                to metadataState.toString()
                        )
                    )
                    listener?.onMetadataStateChanged(metadataState)
                }
            }

            launch {
                composer.metadataFlow.distinctUntilChanged().collectLatest { metadata ->
                    listener?.onMetadataChanged(metadata)
                }
            }
        }
        composer.start(paymentMethodType = paymentMethodType, sessionIntent = primerSessionIntent)
    }

    override fun configure(paymentMethodType: String, completion: (PrimerInitializationData?, PrimerError?) -> Unit) {
        (composer as? PrimerHeadlessDataInitializable)?.configure(completion) ?: completion(null, null)
    }

    override fun startTokenization(
        type: String,
        rawData: PrimerRawData
    ) {
        composer.submit()
    }

    override fun onRawDataChanged(
        paymentMethodType: String,
        oldRawData: PrimerRawData?,
        rawData: PrimerRawData
    ) {
        logReporter.info("Queueing onRawDataChanged call for $paymentMethodType", component = TAG)
        collector(Pair(paymentMethodType, rawData))
    }

    private suspend fun onRawDataChangedImpl(
        paymentMethodType: String,
        rawData: PrimerRawData
    ) {
        logSdkAnalyticsEvent(
            RawDataManagerAnalyticsConstants.SET_RAW_DATA_METHOD,
            mapOf(RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType)
        )
        val requiredInputDataClass =
            paymentMethodMapper.getPrimerHeadlessUniversalCheckoutPaymentMethod(
                paymentMethodType = paymentMethodType
            ).requiredInputDataClass
        if (requiredInputDataClass != rawData::class) {
            PrimerHeadlessUniversalCheckout.instance.emitError(
                HeadlessError.InvalidTokenizationInputDataError(
                    paymentMethodType = paymentMethodType,
                    inputData = rawData::class,
                    requiredInputData = requiredInputDataClass
                )
            )
        } else {
            actionInteractor.invoke(getActionUpdateParams(paymentMethodType, rawData))
                .flatMap {
                    runCatching {
                        yield()
                        composer.updateCollectedData(rawData)
                    }
                }
                .onFailure {
                    yield()
                    PrimerHeadlessUniversalCheckout.instance.emitError(HeadlessError.InvalidRawDataError)
                }
        }
    }

    private fun getActionUpdateParams(
        paymentMethodType: String,
        rawData: PrimerRawData
    ) = when (rawData) {
        is PrimerCardData -> {
            val cardType = CardNetwork.lookup(rawData.cardNumber).type
            if (cardType == CardNetwork.Type.OTHER) {
                ActionUpdateUnselectPaymentMethodParams
            } else {
                ActionUpdateSelectPaymentMethodParams(
                    paymentMethodType = paymentMethodType,
                    cardNetwork = rawData.cardNetwork?.name ?: cardType.name
                )
            }
        }

        else -> ActionUpdateSelectPaymentMethodParams(paymentMethodType)
    }.let {
        MultipleActionUpdateParams(
            listOf(it)
        )
    }

    override fun submit(paymentMethodType: String, rawData: PrimerRawData?) {
        scope.launch {
            logSdkAnalyticsEvent(
                RawDataManagerAnalyticsConstants.SUBMIT_METHOD,
                mapOf(
                    RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType
                )
            )
            preparationStartHandler.handle(paymentMethodType)
            rawData?.let { rawData ->
                startTokenization(paymentMethodType, rawData)
            } ?: run {
                PrimerHeadlessUniversalCheckout.instance.emitError(HeadlessError.InvalidRawDataError)
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

    override fun cleanup(paymentMethodType: String?) {
        logSdkAnalyticsEvent(
            RawDataManagerAnalyticsConstants.CLEANUP_METHOD,
            mapOf(RawDataManagerAnalyticsConstants.PAYMENT_METHOD_TYPE_PARAM to paymentMethodType.orEmpty())
        )
        this.listener = null
        composer.cancel()
        scope.cancel()
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
        )
    }

    private companion object {
        private val TAG = DefaultRawDataManagerDelegate::class.simpleName
    }
}
