package io.primer.android.components.presentation.paymentMethods.base

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.retailOutlet.PrimerRetailerData
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.payments.PaymentRawDataTypeValidateInteractor
import io.primer.android.components.domain.payments.models.PaymentRawDataParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.resolvers.PaymentMethodManagerInitValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.PaymentMethodManagerInitValidationData
import io.primer.android.components.presentation.paymentMethods.nativeUi.ipay88.IPay88State
import io.primer.android.components.presentation.paymentMethods.nativeUi.webRedirect.AsyncState
import io.primer.android.components.presentation.paymentMethods.raw.RawDataManagerAnalyticsConstants
import io.primer.android.components.ui.activity.PaymentMethodLauncherParams
import io.primer.android.components.ui.navigation.Navigator
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.data.payments.configure.PrimerInitializationData
import io.primer.android.data.payments.configure.retailOutlets.RetailOutletsList
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.async.AsyncPaymentMethodInteractor
import io.primer.android.domain.payments.async.models.AsyncMethodParams
import io.primer.android.domain.payments.create.CreatePaymentInteractor
import io.primer.android.domain.payments.create.model.CreatePaymentParams
import io.primer.android.domain.payments.methods.repository.PaymentMethodDescriptorsRepository
import io.primer.android.domain.payments.resume.ResumePaymentInteractor
import io.primer.android.domain.payments.resume.models.ResumeParams
import io.primer.android.domain.rpc.retailOutlets.RetailOutletInteractor
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutletParams
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.events.EventDispatcher
import io.primer.android.ui.CardNetwork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal interface HeadlessDelegate {

    fun init(paymentMethodType: String, category: PrimerPaymentMethodManagerCategory)

    fun dispatchRawDataAction(
        type: String,
        rawData: PrimerRawData,
        submit: Boolean,
        completion: ((Error?) -> Unit) = {}
    )

    fun dispatchAction(
        type: String,
        completion: ((Error?) -> Unit) = {}
    )

    fun startAsyncFlow(url: String, paymentMethodType: String)

    fun configure(
        paymentMethodType: String,
        completion: (PrimerInitializationData?, PrimerError?) -> Unit
    )

    fun cleanup()
}

@Suppress("TooManyFunctions")
internal class DefaultHeadlessManagerDelegate(
    private val paymentRawDataTypeValidateInteractor: PaymentRawDataTypeValidateInteractor,
    private val actionInteractor: ActionInteractor,
    private val asyncPaymentMethodInteractor: AsyncPaymentMethodInteractor,
    private val paymentMethodDescriptorsRepository: PaymentMethodDescriptorsRepository,
    private val retailOutletInteractor: RetailOutletInteractor,
    private val retailOutletRepository: RetailOutletRepository,
    private val createPaymentInteractor: CreatePaymentInteractor,
    private val resumePaymentInteractor: ResumePaymentInteractor,
    private val initValidationRulesResolver: PaymentMethodManagerInitValidationRulesResolver,
    private val navigator: Navigator,
    private val eventDispatcher: EventDispatcher
) : HeadlessDelegate, EventBus.EventListener {

    private val scope = CoroutineScope(SupervisorJob())
    private var subscription: EventBus.SubscriptionHandle? = null
    private var transactionId: String? = null

    init {
        subscription = EventBus.subscribe(this)
    }

    override fun init(paymentMethodType: String, category: PrimerPaymentMethodManagerCategory) {
        PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
            SdkFunctionParams(
                "newInstance",
                mapOf("paymentMethodType" to paymentMethodType, "category" to category.name)
            )
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

    override fun onEvent(e: CheckoutEvent) {
        when (e) {
            is CheckoutEvent.StartAsyncFlow -> startAsyncFlow(
                e.statusUrl,
                e.paymentMethodType
            )
            is CheckoutEvent.ResumeSuccessInternalHUC -> resumePayment(
                e.resumeToken,
                e.resumeHandler
            )
            is CheckoutEvent.PaymentContinueHUC -> createPayment(
                e.data.token,
                e.resumeHandler
            )
            is CheckoutEvent.StartAsyncRedirectFlowHUC -> {
                navigator.openHeadlessScreen(
                    PaymentMethodLauncherParams(
                        e.paymentMethodType,
                        e.sessionIntent,
                        AsyncState.StartRedirect(
                            e.title,
                            e.paymentMethodType,
                            e.redirectUrl,
                            e.statusUrl,
                            e.deeplinkUrl
                        )
                    )
                )
            }
            is CheckoutEvent.StartIPay88Flow -> {
                navigator.openHeadlessScreen(
                    PaymentMethodLauncherParams(
                        e.paymentMethodType,
                        e.sessionIntent,
                        IPay88State.StartRedirect(
                            e.paymentMethodType,
                            e.statusUrl,
                            e.paymentId,
                            e.paymentMethod,
                            e.merchantCode,
                            e.actionType,
                            e.amount,
                            e.referenceNumber,
                            e.prodDesc,
                            e.currencyCode,
                            e.countryCode,
                            e.customerName,
                            e.customerEmail,
                            e.remark,
                            e.backendCallbackUrl,
                            e.deeplinkUrl
                        )
                    )
                )
            }
            is CheckoutEvent.Start3DS -> {
                if (e.processor3DSData == null) {
                    navigator.openThreeDsScreen()
                } else {
                    navigator.openProcessor3dsViewScreen(
                        e.processor3DSData.title,
                        e.processor3DSData.paymentMethodType,
                        e.processor3DSData.redirectUrl,
                        e.processor3DSData.statusUrl
                    )
                }
            }
            is CheckoutEvent.Start3DSMock -> {
                navigator.open3DSMockScreen()
            }
            else -> Unit
        }
    }

    override fun dispatchRawDataAction(
        type: String,
        rawData: PrimerRawData,
        submit: Boolean,
        completion: ((Error?) -> Unit)
    ) {
        scope.launch {
            prepareAdditionalInfo(rawData)
            paymentRawDataTypeValidateInteractor(
                PaymentRawDataParams(
                    type,
                    rawData
                )
            )
                .flatMapLatest {
                    actionInteractor(getActionUpdateParams(type, rawData))
                }.catch {
                    completion(Error(it))
                }
                .collect {
                    completion(null)
                }
        }
    }

    override fun dispatchAction(type: String, completion: (Error?) -> Unit) {
        scope.launch {
            actionInteractor(ActionUpdateSelectPaymentMethodParams(type))
                .flowOn(Dispatchers.Main)
                .onStart {
                    eventDispatcher.dispatchEvent(CheckoutEvent.PreparationStarted(type))
                }
                .catch {
                    completion(Error(it))
                }
                .collect {
                    completion(null)
                }
        }
    }

    private fun createPayment(
        paymentMethodToken: String,
        resumeHandler: PrimerResumeDecisionHandler
    ) {
        scope.launch {
            createPaymentInteractor(
                CreatePaymentParams(
                    paymentMethodToken,
                    resumeHandler
                )
            ).collect {
                transactionId = it
            }
        }
    }

    fun resumePayment(resumeToken: String, resumeHandler: PrimerResumeDecisionHandler) {
        scope.launch {
            resumePaymentInteractor(
                ResumeParams(
                    transactionId.orEmpty(),
                    resumeToken,
                    resumeHandler
                )
            ).collect { }
        }
    }

    override fun startAsyncFlow(url: String, paymentMethodType: String) {
        scope.launch {
            asyncPaymentMethodInteractor(AsyncMethodParams(url, paymentMethodType)).catch {
                it.printStackTrace()
            }
                .collect {}
        }
    }

    override fun configure(
        paymentMethodType: String,
        completion: (PrimerInitializationData?, PrimerError?) -> Unit
    ) {
        PrimerHeadlessUniversalCheckout.instance.addAnalyticsEvent(
            SdkFunctionParams(
                RawDataManagerAnalyticsConstants.CONFIGURE_METHOD,
                mapOf(
                    "paymentMethodType" to paymentMethodType,
                    "category" to PrimerPaymentMethodManagerCategory.RAW_DATA.name
                )
            )
        )
        when (paymentMethodType) {
            PaymentMethodType.XENDIT_RETAIL_OUTLETS.name -> {
                scope.launch {
                    val descriptor =
                        paymentMethodDescriptorsRepository.resolvePaymentMethodDescriptors()
                            .mapLatest { descriptors ->
                                descriptors.first { descriptor ->
                                    descriptor.config.type == paymentMethodType
                                }
                            }.first()
                    retailOutletInteractor(
                        RetailOutletParams(descriptor.config.id.orEmpty())
                    ).catch {
                        withContext(Dispatchers.Main) {
                            completion(null, DefaultErrorMapper().getPrimerError(it))
                        }
                    }.collect {
                        withContext(Dispatchers.Main) { completion(RetailOutletsList(it), null) }
                    }
                }
            }
            else -> completion(null, null)
        }
    }

    override fun cleanup() {
        scope.coroutineContext.cancelChildren()
    }

    fun reset() {
        subscription?.unregister()
        subscription = null
        scope.coroutineContext.cancelChildren()
    }

    private fun getActionUpdateParams(
        type: String,
        rawData: PrimerRawData
    ) = when (rawData) {
        is PrimerCardData -> {
            val cardType = CardNetwork.lookup(rawData.cardNumber).type
            if (cardType == CardNetwork.Type.OTHER) {
                ActionUpdateUnselectPaymentMethodParams
            } else {
                ActionUpdateSelectPaymentMethodParams(
                    type,
                    cardType.name
                )
            }
        }
        else -> ActionUpdateSelectPaymentMethodParams(type)
    }

    private fun prepareAdditionalInfo(rawData: PrimerRawData) {
        when (rawData) {
            is PrimerRetailerData -> {
                retailOutletRepository.setSelectedRetailOutlet(rawData.id)
            }
        }
    }
}
