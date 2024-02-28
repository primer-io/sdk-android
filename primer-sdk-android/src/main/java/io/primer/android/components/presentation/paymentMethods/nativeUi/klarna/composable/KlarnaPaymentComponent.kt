package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.composable

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentView
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentViewCallback
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentsSDKError
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.validation.validator.KlarnaPaymentCategoryValidator
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import io.primer.android.components.manager.core.component.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessSteppable
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.PrimerKlarnaPaymentView
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.analytics.KlarnaPaymentAnalyticsConstants
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.KlarnaSessionCreationDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate.KlarnaTokenizationDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.di.KlarnaPaymentComponentProvider
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.exception.KlarnaIllegalValueKey
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCollectableData
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentStep
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.error.ErrorMapper
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.klarna.exceptions.KlarnaSdkErrorException
import io.primer.android.klarna.exceptions.KlarnaUserUnapprovedException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting

class KlarnaPaymentComponent internal constructor(
    private val klarnaTokenizationDelegate: KlarnaTokenizationDelegate,
    private val klarnaSessionCreationDelegate: KlarnaSessionCreationDelegate,
    @Suppress("UnusedPrivateMember")
    headlessManagerDelegate: DefaultHeadlessManagerDelegate,
    private val eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
    private val errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate,
    private val errorMapper: ErrorMapper,
    private val createKlarnaPaymentView:
        (Context, String, KlarnaPaymentViewCallback, String) -> KlarnaPaymentView
) : ViewModel(),
    PrimerHeadlessSteppable<KlarnaPaymentStep>,
    PrimerHeadlessCollectDataComponent<KlarnaPaymentCollectableData>,
    PrimerHeadlessStartable {

    @VisibleForTesting
    var klarnaSession: KlarnaSession? = null

    private val klarnaPaymentViewCallback = object : KlarnaPaymentViewCallback {
        override fun onAuthorized(
            view: KlarnaPaymentView,
            approved: Boolean,
            authToken: String?,
            finalizedRequired: Boolean?
        ) {
            if (approved) {
                viewModelScope.launch {
                    if (finalizedRequired != true) {
                        if (authToken != null) {
                            klarnaTokenizationDelegate.tokenize(
                                sessionId = requireKlarnaSession().sessionId,
                                authorizationToken = authToken
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
            }

            if (finalizedRequired == true) {
                view.finalize(null)
            } else if (!approved) {
                handleError(KlarnaUserUnapprovedException())
            }
        }

        override fun onErrorOccurred(view: KlarnaPaymentView, error: KlarnaPaymentsSDKError) {
            handleError(KlarnaSdkErrorException("${error.name}: ${error.message}"))
        }

        override fun onFinalized(view: KlarnaPaymentView, approved: Boolean, authToken: String?) {
            if (approved) {
                viewModelScope.launch {
                    if (authToken != null) {
                        klarnaTokenizationDelegate.tokenize(
                            sessionId = requireKlarnaSession().sessionId,
                            authorizationToken = authToken
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
                        paymentView = PrimerKlarnaPaymentView(
                            context = view.context,
                            doAuthorize = {
                                view.authorize(
                                    autoFinalize = true,
                                    sessionData = null
                                )
                            }
                        ).apply {
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
                is KlarnaPaymentCollectableData.PaymentCategory ->
                    onCollectPaymentCategory(collectedData)
            }
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName = KlarnaPaymentAnalyticsConstants.KLARNA_PAYMENT_COLLECTED_DATA_METHOD,
                paymentMethodType = PaymentMethodType.KLARNA.name
            )
        }
    }

    override fun submit() {
        viewModelScope.launch {
            _componentStep.emit(KlarnaPaymentStep.PaymentAuthorizationRequired)
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
        klarnaSessionCreationDelegate.createSession()
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

    private suspend fun onCollectPaymentCategory(
        paymentCategory: KlarnaPaymentCollectableData.PaymentCategory
    ) {
        _componentValidationStatus.emit(
            PrimerValidationStatus.Validating(collectableData = paymentCategory)
        )

        val validationError = KlarnaPaymentCategoryValidator.validate(
            paymentCategories = klarnaSession?.availableCategories,
            paymentCategory = paymentCategory.paymentCategory
        )

        _componentValidationStatus.emit(
            if (validationError != null) {
                PrimerValidationStatus.Invalid(
                    validationErrors = listOf(validationError),
                    collectableData = paymentCategory
                )
            } else {
                val returnUrl = Uri.Builder().scheme(paymentCategory.returnIntentData.scheme)
                    .authority(paymentCategory.returnIntentData.host)
                    .build()
                    .toString()
                runCatching { requireKlarnaSession() }
                    .onSuccess { klarnaSession ->
                        // Will trigger onInitialized/onErrorOccurred in the callback
                        createKlarnaPaymentView(
                            context = paymentCategory.context,
                            paymentCategory = paymentCategory.paymentCategory.identifier,
                            returnUrl = returnUrl
                        ).initialize(klarnaSession.clientToken, returnUrl)
                    }
                    .onFailure(::handleError)

                PrimerValidationStatus.Valid(collectableData = paymentCategory)
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
        fun provideInstance(owner: ViewModelStoreOwner) =
            KlarnaPaymentComponentProvider().provideInstance(owner)
    }
}
