package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.composable

import android.content.Context
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentView
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentViewCallback
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentsSDKError
import io.primer.android.PrimerSessionIntent
import io.primer.android.R
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.error.KlarnaSdkErrorException
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.error.KlarnaUserUnapprovedException
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.validation.validator.KlarnaPaymentCategoryValidator
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.validation.validator.KlarnaPaymentFinalizationValidator
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import io.primer.android.components.manager.core.component.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessSteppable
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.presentation.mock.delegate.MockConfigurationDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.PrimerKlarnaPaymentView
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.analytics.KlarnaPaymentAnalyticsConstants
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.GetKlarnaAuthorizationSessionDataDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.KlarnaSessionCreationDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.KlarnaTokenizationDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.di.KlarnaComponentProvider
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.exception.KlarnaIllegalValueKey
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCollectableData
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentStep
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.error.models.PrimerError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import java.lang.ref.WeakReference
import java.util.UUID

@Suppress("LongParameterList")
class KlarnaComponent internal constructor(
    private val klarnaTokenizationDelegate: KlarnaTokenizationDelegate,
    private val klarnaSessionCreationDelegate: KlarnaSessionCreationDelegate,
    @Suppress("UnusedPrivateMember")
    headlessManagerDelegate: DefaultHeadlessManagerDelegate,
    private val mockConfigurationDelegate: MockConfigurationDelegate,
    private val eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
    private val errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate,
    private val authorizationSessionDataDelegate: GetKlarnaAuthorizationSessionDataDelegate,
    private val errorMapper: ErrorMapper,
    private val createKlarnaPaymentView:
        (Context, String, KlarnaPaymentViewCallback, String) -> KlarnaPaymentView,
    private val primerSettings: PrimerSettings,
    private val primerSessionIntent: PrimerSessionIntent
) : ViewModel(),
    PrimerHeadlessSteppable<KlarnaPaymentStep>,
    PrimerHeadlessCollectDataComponent<KlarnaPaymentCollectableData>,
    PrimerHeadlessStartable {

    @VisibleForTesting
    var klarnaSession: KlarnaSession? = null

    @VisibleForTesting
    var isFinalizationRequired: Boolean = false

    private var klarnaPaymentView: WeakReference<KlarnaPaymentView?> = WeakReference(null)

    private val isMockedFlow get() = mockConfigurationDelegate.isMockedFlow()

    private val klarnaPaymentViewCallback = object : KlarnaPaymentViewCallback {
        override fun onAuthorized(
            view: KlarnaPaymentView,
            approved: Boolean,
            authToken: String?,
            finalizedRequired: Boolean?
        ) {
            isFinalizationRequired = finalizedRequired == true
            if (approved) {
                viewModelScope.launch {
                    if (finalizedRequired != true) {
                        if (authToken != null) {
                            klarnaTokenizationDelegate.tokenize(
                                sessionId = requireKlarnaSession().sessionId,
                                authorizationToken = authToken,
                                primerSessionIntent = primerSessionIntent
                            )
                                .onFailure(::handleError)
                        }
                    }

                    _componentStep.emit(
                        KlarnaPaymentStep.PaymentSessionAuthorized(
                            isFinalized = finalizedRequired == false
                        )
                    )
                }
            } else {
                handleError(KlarnaUserUnapprovedException())
            }
        }

        override fun onErrorOccurred(view: KlarnaPaymentView, error: KlarnaPaymentsSDKError) {
            handleError(KlarnaSdkErrorException("${error.name}: ${error.message}"))
        }

        override fun onFinalized(view: KlarnaPaymentView, approved: Boolean, authToken: String?) {
            isFinalizationRequired = false
            if (approved) {
                viewModelScope.launch {
                    if (authToken != null) {
                        klarnaTokenizationDelegate.tokenize(
                            sessionId = requireKlarnaSession().sessionId,
                            authorizationToken = authToken,
                            primerSessionIntent = primerSessionIntent
                        )
                            .onFailure(::handleError)
                    }

                    _componentStep.emit(KlarnaPaymentStep.PaymentSessionFinalized)
                }
            } else {
                handleError(KlarnaUserUnapprovedException())
            }
        }

        override fun onInitialized(view: KlarnaPaymentView) {
            view.load(null)
        }

        override fun onLoadPaymentReview(view: KlarnaPaymentView, showForm: Boolean) {
            // no-op
        }

        override fun onLoaded(view: KlarnaPaymentView) {
            viewModelScope.launch {
                _componentStep.emit(
                    KlarnaPaymentStep.PaymentViewLoaded(
                        paymentView = PrimerKlarnaPaymentView(context = view.context).apply {
                            addView(view)
                        }
                    )
                )
            }
        }

        override fun onReauthorized(
            view: KlarnaPaymentView,
            approved: Boolean,
            authToken: String?
        ) {
            // no-op
        }
    }

    private val _componentValidationStatus =
        MutableSharedFlow<PrimerValidationStatus<KlarnaPaymentCollectableData>>()
    override val componentValidationStatus:
        Flow<PrimerValidationStatus<KlarnaPaymentCollectableData>>
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
                paymentMethodType = PaymentMethodType.KLARNA.name
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
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
        }
    }

    override fun submit() {
        viewModelScope.launch {
            if (isMockedFlow) {
                delay(2000)
                klarnaTokenizationDelegate.tokenize(
                    sessionId = requireKlarnaSession().sessionId,
                    authorizationToken = UUID.randomUUID().toString(),
                    primerSessionIntent = primerSessionIntent
                )
                _componentStep.emit(
                    KlarnaPaymentStep.PaymentSessionAuthorized(isFinalized = true)
                )
            } else {
                requireNotNullCheck(
                    klarnaPaymentView.get(),
                    KlarnaIllegalValueKey.KLARNA_PAYMENT_VIEW
                ).authorize(
                    autoFinalize = primerSettings.sdkIntegrationType != SdkIntegrationType.HEADLESS,
                    sessionData =
                    authorizationSessionDataDelegate.getAuthorizationSessionDataOrNull()
                )
            }
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
        }
    }

    // region Utils
    private fun handleError(
        throwable: Throwable
    ) = viewModelScope.launch {
        errorMapper.getPrimerError(throwable)
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
                    KlarnaPaymentStep.PaymentSessionCreated(session.availableCategories)
                )
            }
            .onFailure {
                handleError(it)
            }
    }

    private suspend fun onCollectPaymentOptions(
        paymentOptions: KlarnaPaymentCollectableData.PaymentOptions
    ) {
        _componentValidationStatus.emit(
            PrimerValidationStatus.Validating(collectableData = paymentOptions)
        )

        val validationError = KlarnaPaymentCategoryValidator.validate(
            paymentCategories = klarnaSession?.availableCategories,
            paymentCategory = paymentOptions.paymentCategory
        )

        _componentValidationStatus.emit(
            if (validationError != null) {
                PrimerValidationStatus.Invalid(
                    validationErrors = listOf(validationError),
                    collectableData = paymentOptions
                )
            } else {
                runCatching { requireKlarnaSession() }
                    .onSuccess { klarnaSession ->
                        if (isMockedFlow) {
                            _componentStep.emit(
                                KlarnaPaymentStep.PaymentViewLoaded(
                                    PrimerKlarnaPaymentView(paymentOptions.context).apply {
                                        addView(
                                            TextView(paymentOptions.context).apply {
                                                text = paymentOptions.context.getText(
                                                    R.string.mock_klarna_view
                                                )
                                            }
                                        )
                                    }
                                )
                            )
                        } else {
                            val returnUrl = paymentOptions.returnIntentUrl

                            // Will trigger onInitialized/onErrorOccurred in the callback
                            klarnaPaymentView = WeakReference(
                                createKlarnaPaymentView(
                                    context = paymentOptions.context,
                                    paymentCategory = paymentOptions.paymentCategory.identifier,
                                    returnUrl = returnUrl
                                ).apply {
                                    // Will trigger onInitialized/onErrorOccurred in the callback
                                    initialize(klarnaSession.clientToken, returnUrl)
                                }
                            )
                        }
                    }
                    .onFailure(::handleError)

                PrimerValidationStatus.Valid(collectableData = paymentOptions)
            }
        )
    }

    private suspend fun onCollectFinalizePayment(
        finalizePayment: KlarnaPaymentCollectableData.FinalizePayment
    ) {
        _componentValidationStatus.emit(
            PrimerValidationStatus.Validating(collectableData = finalizePayment)
        )

        val validationError = KlarnaPaymentFinalizationValidator.validate(isFinalizationRequired)

        _componentValidationStatus.emit(
            if (validationError == null) {
                klarnaPaymentView.get()?.finalize(null)
                PrimerValidationStatus.Valid(collectableData = finalizePayment)
            } else {
                PrimerValidationStatus.Invalid(
                    validationErrors = listOf(validationError),
                    collectableData = finalizePayment
                )
            }
        )
    }

    private fun createKlarnaPaymentView(
        context: Context,
        paymentCategory: String,
        returnUrl: String
    ): KlarnaPaymentView =
        createKlarnaPaymentView(
            context,
            paymentCategory,
            klarnaPaymentViewCallback,
            returnUrl
        )

    private fun requireKlarnaSession() =
        requireNotNullCheck(klarnaSession, KlarnaIllegalValueKey.KLARNA_SESSION)
    // endregion

    internal companion object {
        fun provideInstance(owner: ViewModelStoreOwner, primerSessionIntent: PrimerSessionIntent) =
            KlarnaComponentProvider().provideInstance(owner, primerSessionIntent)
    }
}
