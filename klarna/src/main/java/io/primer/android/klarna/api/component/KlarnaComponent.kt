package io.primer.android.klarna.api.component

import android.content.Context
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentView
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentViewCallback
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentsSDKError
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.configuration.mock.presentation.MockConfigurationDelegate
import io.primer.android.core.extensions.flatMap
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.klarna.api.composable.KlarnaPaymentCollectableData
import io.primer.android.klarna.api.composable.KlarnaPaymentStep
import io.primer.android.klarna.api.ui.PrimerKlarnaPaymentView
import io.primer.android.klarna.di.KlarnaComponentProvider
import io.primer.android.klarna.implementation.analytics.KlarnaPaymentAnalyticsConstants
import io.primer.android.klarna.implementation.errors.data.exception.KlarnaIllegalValueKey
import io.primer.android.klarna.implementation.errors.data.exception.KlarnaSdkErrorException
import io.primer.android.klarna.implementation.errors.data.exception.KlarnaUserUnapprovedException
import io.primer.android.klarna.implementation.payment.presentation.KlarnaPaymentDelegate
import io.primer.android.klarna.implementation.session.data.validation.validator.KlarnaPaymentCategoryValidator
import io.primer.android.klarna.implementation.session.data.validation.validator.KlarnaPaymentFinalizationValidator
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSession
import io.primer.android.klarna.implementation.session.presentation.GetKlarnaAuthorizationSessionDataDelegate
import io.primer.android.klarna.implementation.session.presentation.KlarnaSessionCreationDelegate
import io.primer.android.klarna.implementation.tokenization.presentation.KlarnaTokenizationDelegate
import io.primer.android.klarna.implementation.tokenization.presentation.KlarnaTokenizationInputable
import io.primer.android.klarna.main.R
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.manager.component.PrimerHeadlessCollectDataComponent
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStartable
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessSteppable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@VisibleForTesting
val MOCK_EMISSION_DELAY = 2.seconds

@Suppress("LongParameterList")
class KlarnaComponent internal constructor(
    private val tokenizationDelegate: KlarnaTokenizationDelegate,
    private val paymentDelegate: KlarnaPaymentDelegate,
    private val klarnaSessionCreationDelegate: KlarnaSessionCreationDelegate,
    private val mockConfigurationDelegate: MockConfigurationDelegate,
    private val eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
    private val errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate,
    private val validationErrorLoggingDelegate: SdkAnalyticsValidationErrorLoggingDelegate,
    private val authorizationSessionDataDelegate: GetKlarnaAuthorizationSessionDataDelegate,
    private val errorMapperRegistry: ErrorMapperRegistry,
    private val createKlarnaPaymentView: (Context, String, KlarnaPaymentViewCallback, String) -> KlarnaPaymentView,
    private val primerSettings: PrimerSettings,
    private val primerSessionIntent: PrimerSessionIntent,
) : ViewModel(),
    PrimerHeadlessSteppable<KlarnaPaymentStep>,
    PrimerHeadlessCollectDataComponent<KlarnaPaymentCollectableData>,
    PrimerHeadlessStartable {
    @VisibleForTesting
    var klarnaSession: KlarnaSession? = null

    @VisibleForTesting
    var isFinalizationRequired: Boolean = false

    private var klarnaPaymentView: WeakReference<KlarnaPaymentView?> = WeakReference(null)

    private val isMockedFlow
        get() = mockConfigurationDelegate.isMockedFlow()

    private val klarnaPaymentViewCallback =
        object : KlarnaPaymentViewCallback {
            override fun onAuthorized(
                view: KlarnaPaymentView,
                approved: Boolean,
                authToken: String?,
                finalizedRequired: Boolean?,
            ) {
                isFinalizationRequired = finalizedRequired == true
                if (approved) {
                    viewModelScope.launch {
                        if (finalizedRequired != true && authToken != null) {
                            runCatching {
                                requireKlarnaSession().sessionId
                            }
                                .flatMap { sessionId ->
                                    tokenizationDelegate.tokenize(
                                        KlarnaTokenizationInputable(
                                            sessionId = sessionId,
                                            authorizationToken = authToken,
                                            paymentMethodType = PaymentMethodType.KLARNA.name,
                                            primerSessionIntent = primerSessionIntent,
                                        ),
                                    )
                                        .recoverCatching { throw CheckoutFailureException(it) }
                                        .onFailure { paymentDelegate.handleError(it) }
                                }
                                .flatMap { paymentMethodTokenData ->
                                    paymentDelegate.handlePaymentMethodToken(
                                        paymentMethodTokenData = paymentMethodTokenData,
                                        primerSessionIntent = primerSessionIntent,
                                    )
                                        .recoverCatching { throw CheckoutFailureException(it) }
                                        .onFailure { paymentDelegate.handleError(it) }
                                }
                                .onFailure(::handleError)
                        }

                        _componentStep.emit(
                            KlarnaPaymentStep.PaymentSessionAuthorized(
                                isFinalized = finalizedRequired == false,
                            ),
                        )
                    }
                } else {
                    handleError(KlarnaUserUnapprovedException())
                }
            }

            override fun onErrorOccurred(
                view: KlarnaPaymentView,
                error: KlarnaPaymentsSDKError,
            ) {
                handleError(KlarnaSdkErrorException("${error.name}: ${error.message}"))
            }

            override fun onFinalized(
                view: KlarnaPaymentView,
                approved: Boolean,
                authToken: String?,
            ) {
                isFinalizationRequired = false
                if (approved) {
                    if (authToken != null) {
                        viewModelScope.launch {
                            runCatching {
                                requireKlarnaSession().sessionId
                            }
                                .flatMap { sessionId ->
                                    tokenizationDelegate.tokenize(
                                        KlarnaTokenizationInputable(
                                            sessionId = sessionId,
                                            authorizationToken = authToken,
                                            paymentMethodType = PaymentMethodType.KLARNA.name,
                                            primerSessionIntent = primerSessionIntent,
                                        ),
                                    )
                                        .recoverCatching { throw CheckoutFailureException(it) }
                                        .onFailure { paymentDelegate.handleError(it) }
                                }
                                .flatMap { paymentMethodTokenData ->
                                    paymentDelegate.handlePaymentMethodToken(
                                        paymentMethodTokenData = paymentMethodTokenData,
                                        primerSessionIntent = primerSessionIntent,
                                    )
                                        .recoverCatching { throw CheckoutFailureException(it) }
                                        .onFailure { paymentDelegate.handleError(it) }
                                }
                                .onFailure(::handleError)
                        }

                        viewModelScope.launch {
                            _componentStep.emit(KlarnaPaymentStep.PaymentSessionFinalized)
                        }
                    }
                } else {
                    handleError(KlarnaUserUnapprovedException())
                }
            }

            override fun onInitialized(view: KlarnaPaymentView) {
                view.load(null)
            }

            override fun onLoadPaymentReview(
                view: KlarnaPaymentView,
                showForm: Boolean,
            ) {
                // no-op
            }

            override fun onLoaded(view: KlarnaPaymentView) {
                viewModelScope.launch {
                    _componentStep.emit(
                        KlarnaPaymentStep.PaymentViewLoaded(
                            paymentView =
                            PrimerKlarnaPaymentView(context = view.context).apply {
                                addView(view)
                            },
                        ),
                    )
                }
            }

            override fun onReauthorized(
                view: KlarnaPaymentView,
                approved: Boolean,
                authToken: String?,
            ) {
                // no-op
            }
        }

    private val _componentValidationStatus =
        MutableSharedFlow<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
    override val componentValidationStatus: Flow<PrimerValidationStatus<KlarnaPaymentCollectableData>>
        get() = _componentValidationStatus

    private val _componentStep = MutableSharedFlow<KlarnaPaymentStep>()
    override val componentStep: Flow<KlarnaPaymentStep> = _componentStep

    private val _componentError = MutableSharedFlow<PrimerError>()
    override val componentError: Flow<PrimerError> = _componentError

    override fun start() {
        viewModelScope.launch {
            createKlarnaSession()
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_START_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name,
            )
        }
    }

    override fun updateCollectedData(collectedData: KlarnaPaymentCollectableData) {
        viewModelScope.launch {
            when (collectedData) {
                is KlarnaPaymentCollectableData.PaymentOptions ->
                    onCollectPaymentOptions(collectedData)

                is KlarnaPaymentCollectableData.FinalizePayment ->
                    onCollectFinalizePayment(collectedData)
            }
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_COLLECTED_DATA_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name,
            )
        }
    }

    override fun submit() {
        viewModelScope.launch {
            if (isMockedFlow) {
                delay(MOCK_EMISSION_DELAY)
                runCatching {
                    requireKlarnaSession().sessionId
                }
                    .flatMap {
                        tokenizationDelegate.tokenize(
                            KlarnaTokenizationInputable(
                                sessionId = requireKlarnaSession().sessionId,
                                authorizationToken = UUID.randomUUID().toString(),
                                paymentMethodType = PaymentMethodType.KLARNA.name,
                                primerSessionIntent = primerSessionIntent,
                            ),
                        )
                            .recoverCatching { throw CheckoutFailureException(it) }
                            .onFailure { paymentDelegate.handleError(it) }
                    }
                    .flatMap { paymentMethodTokenData ->
                        paymentDelegate.handlePaymentMethodToken(
                            paymentMethodTokenData = paymentMethodTokenData,
                            primerSessionIntent = primerSessionIntent,
                        )
                            .recoverCatching { throw CheckoutFailureException(it) }
                            .onFailure { paymentDelegate.handleError(it) }
                    }.onFailure(::handleError)

                _componentStep.emit(
                    KlarnaPaymentStep.PaymentSessionAuthorized(isFinalized = true),
                )
            } else {
                runCatching {
                    requireNotNullCheck(
                        klarnaPaymentView.get(),
                        KlarnaIllegalValueKey.KLARNA_PAYMENT_VIEW,
                    )
                }
                    .onSuccess {
                        it.authorize(
                            autoFinalize = primerSettings.sdkIntegrationType != SdkIntegrationType.HEADLESS,
                            sessionData = authorizationSessionDataDelegate.getAuthorizationSessionDataOrNull(),
                        )
                    }
                    .onFailure(::handleError)
            }
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name,
            )
        }
    }

    // region Utils
    private fun handleError(throwable: Throwable) =
        viewModelScope.launch {
            val isCheckoutFailureException = throwable is CheckoutFailureException
            /*
            exclude CheckoutFailureException when in Drop-in because the inner exception is always dispatched.
             */
            if (primerSettings.sdkIntegrationType == SdkIntegrationType.DROP_IN && !isCheckoutFailureException) {
                paymentDelegate.handleError(throwable)
            }

            errorMapperRegistry.getPrimerError(throwable)
                .also { error ->
                    _componentError.emit(error)
                    errorLoggingDelegate.logSdkAnalyticsErrors(error = error)
                }
        }

    private suspend fun createKlarnaSession() {
        klarnaSessionCreationDelegate.createSession(primerSessionIntent)
            .onSuccess { session ->
                klarnaSession = session
                _componentStep.emit(
                    KlarnaPaymentStep.PaymentSessionCreated(session.availableCategories),
                )
            }
            .recoverCatching { throw CheckoutFailureException(it) }
            .onFailure {
                paymentDelegate.handleError(it)
                handleError(it)
            }
    }

    private suspend fun onCollectPaymentOptions(paymentOptions: KlarnaPaymentCollectableData.PaymentOptions) {
        _componentValidationStatus.emit(
            PrimerValidationStatus.Validating(collectableData = paymentOptions),
        )

        val validationError =
            KlarnaPaymentCategoryValidator.validate(
                paymentCategories = klarnaSession?.availableCategories,
                paymentCategory = paymentOptions.paymentCategory,
            )

        _componentValidationStatus.emit(
            if (validationError != null) {
                PrimerValidationStatus.Invalid<KlarnaPaymentCollectableData>(
                    validationErrors = listOf(validationError),
                    collectableData = paymentOptions,
                ).also { invalidValidationStatus ->
                    invalidValidationStatus.validationErrors.forEach { error ->
                        validationErrorLoggingDelegate.logSdkAnalyticsError(error)
                    }
                }
            } else {
                runCatching { requireKlarnaSession() }
                    .onSuccess { klarnaSession ->
                        if (isMockedFlow) {
                            _componentStep.emit(
                                KlarnaPaymentStep.PaymentViewLoaded(
                                    PrimerKlarnaPaymentView(paymentOptions.context).apply {
                                        addView(
                                            TextView(paymentOptions.context).apply {
                                                text =
                                                    paymentOptions.context.getText(
                                                        R.string.mock_klarna_view,
                                                    )
                                            },
                                        )
                                    },
                                ),
                            )
                        } else {
                            val returnUrl = paymentOptions.returnIntentUrl

                            // Will trigger onInitialized/onErrorOccurred in the callback
                            klarnaPaymentView =
                                WeakReference(
                                    createKlarnaPaymentView(
                                        context = paymentOptions.context,
                                        paymentCategory = paymentOptions.paymentCategory.identifier,
                                        returnUrl = returnUrl,
                                    ).apply {
                                        // Will trigger onInitialized/onErrorOccurred in the callback
                                        initialize(klarnaSession.clientToken, returnUrl)
                                    },
                                )
                        }
                    }
                    .onFailure(::handleError)

                PrimerValidationStatus.Valid(collectableData = paymentOptions)
            },
        )
    }

    private suspend fun onCollectFinalizePayment(finalizePayment: KlarnaPaymentCollectableData.FinalizePayment) {
        _componentValidationStatus.emit(
            PrimerValidationStatus.Validating(collectableData = finalizePayment),
        )

        val validationError = KlarnaPaymentFinalizationValidator.validate(isFinalizationRequired)

        _componentValidationStatus.emit(
            if (validationError == null) {
                klarnaPaymentView.get()?.finalize(null)
                PrimerValidationStatus.Valid(collectableData = finalizePayment)
            } else {
                PrimerValidationStatus.Invalid<KlarnaPaymentCollectableData>(
                    validationErrors = listOf(validationError),
                    collectableData = finalizePayment,
                ).also {
                    validationErrorLoggingDelegate.logSdkAnalyticsError(validationError)
                }
            },
        )
    }

    private fun createKlarnaPaymentView(
        context: Context,
        paymentCategory: String,
        returnUrl: String,
    ): KlarnaPaymentView =
        createKlarnaPaymentView(
            context,
            paymentCategory,
            klarnaPaymentViewCallback,
            returnUrl,
        )

    private fun requireKlarnaSession() = requireNotNullCheck(klarnaSession, KlarnaIllegalValueKey.KLARNA_SESSION)
    // endregion

    internal class CheckoutFailureException(cause: Throwable) : Throwable(cause = cause)

    internal companion object {
        fun provideInstance(
            owner: ViewModelStoreOwner,
            primerSessionIntent: PrimerSessionIntent,
        ) = KlarnaComponentProvider().provideInstance(owner, primerSessionIntent)
    }
}
