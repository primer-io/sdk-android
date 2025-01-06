package io.primer.android.stripe.ach.api.component

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.core.extensions.debounce
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.requireCause
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.manager.component.ach.PrimerHeadlessAchComponent
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.ManualFlowSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PendingResumeHandler
import io.primer.android.stripe.ach.api.additionalInfo.AchAdditionalInfo
import io.primer.android.stripe.ach.api.composable.AchUserDetailsCollectableData
import io.primer.android.stripe.ach.api.composable.AchUserDetailsStep
import io.primer.android.stripe.ach.di.StripeAchUserDetailsComponentProvider
import io.primer.android.stripe.ach.implementation.analytics.StripeAchUserDetailsAnalyticsConstants
import io.primer.android.stripe.ach.implementation.payment.presentation.StripeAchPaymentDelegate
import io.primer.android.stripe.ach.implementation.selection.presentation.StripeAchBankFlowDelegate
import io.primer.android.stripe.ach.implementation.session.data.exception.StripeAchUserDetailsIllegalValueKey
import io.primer.android.stripe.ach.implementation.session.data.validation.validator.EmailAddressValidator
import io.primer.android.stripe.ach.implementation.session.data.validation.validator.FirstNameValidator
import io.primer.android.stripe.ach.implementation.session.data.validation.validator.LastNameValidator
import io.primer.android.stripe.ach.implementation.session.presentation.GetClientSessionCustomerDetailsDelegate
import io.primer.android.stripe.ach.implementation.session.presentation.StripeAchClientSessionPatchDelegate
import io.primer.android.stripe.ach.implementation.tokenization.presentation.StripeAchTokenizationDelegate
import io.primer.android.stripe.ach.implementation.tokenization.presentation.model.StripeAchTokenizationInputable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@Suppress("LongParameterList")
class StripeAchUserDetailsComponent internal constructor(
    private val getClientSessionCustomerDetailsDelegate: GetClientSessionCustomerDetailsDelegate,
    private val stripeAchClientSessionPatchDelegate: StripeAchClientSessionPatchDelegate,
    private val stripeAchTokenizationDelegate: StripeAchTokenizationDelegate,
    private val stripeAchPaymentDelegate: StripeAchPaymentDelegate,
    private val eventLoggingDelegate: PaymentMethodSdkAnalyticsEventLoggingDelegate,
    private val stripeAchBankFlowDelegate: StripeAchBankFlowDelegate,
    private val errorLoggingDelegate: SdkAnalyticsErrorLoggingDelegate,
    private val validationErrorLoggingDelegate: SdkAnalyticsValidationErrorLoggingDelegate,
    private val errorMapperRegistry: ErrorMapperRegistry,
    private val successHandler: CheckoutSuccessHandler,
    private val pendingResumeHandler: PendingResumeHandler,
    private val manualFlowSuccessHandler: ManualFlowSuccessHandler,
    private val primerSettings: PrimerSettings,
    private val config: PrimerConfig,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(),
    PrimerHeadlessAchComponent<AchUserDetailsCollectableData, AchUserDetailsStep> {
    private var firstName: String = savedStateHandle.get<String>(FIRST_NAME_KEY).orEmpty()
        set(value) {
            savedStateHandle[FIRST_NAME_KEY] = value
            field = value
        }
    private var lastName: String = savedStateHandle.get<String>(LAST_NAME_KEY).orEmpty()
        set(value) {
            savedStateHandle[LAST_NAME_KEY] = value
            field = value
        }
    private var emailAddress: String = savedStateHandle.get<String>(EMAIL_ADDRESS_KEY).orEmpty()
        set(value) {
            savedStateHandle[EMAIL_ADDRESS_KEY] = value
            field = value
        }
    private var hadFirstSubmission: Boolean = savedStateHandle[HAD_FIRST_SUBMISSION_KEY] ?: false
        set(value) {
            savedStateHandle[HAD_FIRST_SUBMISSION_KEY] = value
            field = value
        }
    private val _componentError = MutableSharedFlow<PrimerError>()
    override val componentError: Flow<PrimerError> = _componentError

    private val _componentValidationStatus =
        MutableSharedFlow<PrimerValidationStatus<AchUserDetailsCollectableData>>()
    override val componentValidationStatus:
        Flow<PrimerValidationStatus<AchUserDetailsCollectableData>> = _componentValidationStatus

    private val _componentStep = MutableSharedFlow<AchUserDetailsStep>()
    override val componentStep: Flow<AchUserDetailsStep> = _componentStep

    override fun start() {
        viewModelScope.launch {
            getClientSessionCustomerDetailsDelegate()
                .onSuccess {
                    firstName = it.firstName
                    lastName = it.lastName
                    emailAddress = it.emailAddress
                    _componentStep.emit(
                        AchUserDetailsStep.UserDetailsRetrieved(
                            firstName = it.firstName,
                            lastName = it.lastName,
                            emailAddress = it.emailAddress,
                        ),
                    )
                }
                .onFailure(::handleError)

            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                    StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_START_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
            )
        }
    }

    val collector = viewModelScope.debounce<AchUserDetailsCollectableData> { updateCollectedDataImpl(it) }

    override fun updateCollectedData(collectedData: AchUserDetailsCollectableData) {
        collector(collectedData)
    }

    private suspend fun updateCollectedDataImpl(collectedData: AchUserDetailsCollectableData): Boolean {
        _componentValidationStatus.emit(PrimerValidationStatus.Validating(collectedData))

        val validationError =
            when (collectedData) {
                is AchUserDetailsCollectableData.FirstName -> {
                    val error = FirstNameValidator.validate(collectedData.value)
                    if (error == null) firstName = collectedData.value
                    error
                }

                is AchUserDetailsCollectableData.LastName -> {
                    val error = LastNameValidator.validate(collectedData.value)
                    if (error == null) lastName = collectedData.value
                    error
                }

                is AchUserDetailsCollectableData.EmailAddress -> {
                    val error = EmailAddressValidator.validate(collectedData.value)
                    if (error == null) emailAddress = collectedData.value
                    error
                }
            }

        _componentValidationStatus.emit(
            if (validationError == null) {
                PrimerValidationStatus.Valid(collectedData)
            } else {
                validationErrorLoggingDelegate.logSdkAnalyticsError(validationError)
                PrimerValidationStatus.Invalid(validationError, collectedData)
            },
        )

        eventLoggingDelegate.logSdkAnalyticsEvent(
            methodName =
                StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_COLLECTED_DATA_METHOD,
            paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
        )
        return validationError == null
    }

    override fun submit() {
        viewModelScope.launch {
            eventLoggingDelegate.logSdkAnalyticsEvent(
                methodName =
                    StripeAchUserDetailsAnalyticsConstants.STRIPE_ACH_USER_DETAIL_SUBMIT_DATA_METHOD,
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
            )

            if (!hadFirstSubmission) {
                // First time submit is called, trigger validation
                var isValid = updateCollectedDataImpl(AchUserDetailsCollectableData.FirstName(firstName))
                isValid = isValid and updateCollectedDataImpl(AchUserDetailsCollectableData.LastName(lastName))
                isValid = isValid and updateCollectedDataImpl(AchUserDetailsCollectableData.EmailAddress(emailAddress))
                hadFirstSubmission = true

                if (!isValid) {
                    return@launch
                }
            }

            runCatching {
                object {
                    val firstName =
                        requireNotNullCheck(
                            value = this@StripeAchUserDetailsComponent.firstName.takeIf { it.isNotBlank() },
                            key = StripeAchUserDetailsIllegalValueKey.MISSING_FIRST_NAME,
                        )
                    val lastName =
                        requireNotNullCheck(
                            value = this@StripeAchUserDetailsComponent.lastName.takeIf { it.isNotBlank() },
                            key = StripeAchUserDetailsIllegalValueKey.MISSING_LAST_NAME,
                        )
                    val emailAddress =
                        requireNotNullCheck(
                            value = this@StripeAchUserDetailsComponent.emailAddress.takeIf { it.isNotBlank() },
                            key = StripeAchUserDetailsIllegalValueKey.MISSING_EMAIL_ADDRESS,
                        )
                }
            }
                .flatMap { userDetails ->
                    stripeAchClientSessionPatchDelegate(
                        firstName = userDetails.firstName,
                        lastName = userDetails.lastName,
                        emailAddress = userDetails.emailAddress,
                    )
                        .recoverCatching { throw CheckoutFailureException(it) }
                        .onFailure { stripeAchPaymentDelegate.handleError(it.requireCause()) }
                }
                .onSuccess {
                    _componentStep.emit(AchUserDetailsStep.UserDetailsCollected)
                }
                .flatMap {
                    stripeAchTokenizationDelegate.tokenize(
                        StripeAchTokenizationInputable(
                            paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                            primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                        ),
                    )
                        .recoverCatching { throw CheckoutFailureException(it) }
                        .onFailure { stripeAchPaymentDelegate.handleError(it.requireCause()) }
                }
                .flatMap { paymentMethodTokenData ->
                    stripeAchPaymentDelegate.handlePaymentMethodToken(
                        paymentMethodTokenData = paymentMethodTokenData,
                        primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                    )
                        .recoverCatching { throw CheckoutFailureException(it) }
                        .onFailure { stripeAchPaymentDelegate.handleError(it.requireCause()) }
                        .mapCatching {
                            if (it is PaymentDecision.Error) {
                                // Already handled by the payment delegate, handle by component emission only.
                                throw CheckoutFailureException(Exception(it.error.description))
                            } else {
                                it
                            }
                        }
                }
                .flatMap {
                    stripeAchPaymentDelegate.lastDecision?.let { stripeAchDecision ->
                        stripeAchBankFlowDelegate.handle(
                            clientSecret = stripeAchDecision.stripeClientSecret,
                            paymentIntentId = stripeAchDecision.stripePaymentIntentId,
                            sdkCompleteUrl = stripeAchDecision.sdkCompleteUrl,
                        )
                            .map {
                                when (config.settings.paymentHandling) {
                                    PrimerPaymentHandling.MANUAL -> {
                                        pendingResumeHandler.handle(
                                            additionalInfo = AchAdditionalInfo.MandateAccepted(it.mandateTimestamp),
                                        )
                                        manualFlowSuccessHandler.handle()
                                    }

                                    PrimerPaymentHandling.AUTO ->
                                        successHandler.handle(
                                            payment = requireNotNull(it.payment),
                                            additionalInfo = null,
                                        )
                                }
                            }
                            .recoverCatching {
                                // Don't show the failure screen when the coroutine is cancelled.
                                if (it is PaymentMethodCancelledException || it !is CancellationException) {
                                    throw CheckoutFailureException(it)
                                }
                            }
                            .onFailure {
                                stripeAchPaymentDelegate.handleError(
                                    if (it is CheckoutFailureException) {
                                        it.requireCause()
                                    } else {
                                        it
                                    },
                                )
                            }
                    } ?: Result.success(Unit)
                }
                .onFailure(::handleError)
        }
    }

    private fun handleError(throwable: Throwable) =
        viewModelScope.launch {
            val isCheckoutFailureException = throwable is CheckoutFailureException
        /*
        exclude CheckoutFailureException when in Drop-in because the inner exception is always dispatched.
         */
            if (primerSettings.sdkIntegrationType == SdkIntegrationType.DROP_IN && !isCheckoutFailureException) {
                stripeAchPaymentDelegate.handleError(throwable)
            }
            errorMapperRegistry.getPrimerError(
                if (isCheckoutFailureException) {
                    requireNotNull(throwable.cause)
                } else {
                    throwable
                },
            )
                .also { error ->
                    _componentError.emit(error)
                    errorLoggingDelegate.logSdkAnalyticsErrors(error = error)
                }
        }

    internal class CheckoutFailureException(cause: Throwable) : Throwable(cause = cause)

    companion object {
        private const val FIRST_NAME_KEY = "first_name"
        private const val LAST_NAME_KEY = "last_name"
        private const val EMAIL_ADDRESS_KEY = "email_address"
        private const val HAD_FIRST_SUBMISSION_KEY = "had_first_submission"

        fun provideInstance(owner: ViewModelStoreOwner) = StripeAchUserDetailsComponentProvider.provideInstance(owner)
    }
}
