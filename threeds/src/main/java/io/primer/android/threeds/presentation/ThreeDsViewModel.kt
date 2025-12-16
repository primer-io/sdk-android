package io.primer.android.threeds.presentation

import android.app.Activity
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netcetera.threeds.sdk.api.transaction.AuthenticationRequestParameters
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.payments.core.tokenization.data.model.ResponseCode
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsCheckoutParams
import io.primer.android.threeds.domain.models.ThreeDsInitParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class ThreeDsViewModel(
    private val threeDsInteractor: ThreeDsInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val settings: PrimerSettings,
    private val getCurrentTimeMillis: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var challengeInProgress: Boolean = false

    private val _threeDsInitEvent = MutableLiveData<Unit>()
    val threeDsInitEvent: LiveData<Unit> = _threeDsInitEvent

    private val _threeDsStatusChangedEvent = MutableLiveData<ChallengeStatusData>()
    val threeDsStatusChangedEvent: LiveData<ChallengeStatusData> = _threeDsStatusChangedEvent

    private val _threeDsErrorEvent = MutableLiveData<Throwable>()
    val threeDsErrorEvent: LiveData<Throwable> = _threeDsErrorEvent

    private val _challengeRequiredEvent = MutableLiveData<ThreeDsEventData.ChallengeRequiredData>()
    val challengeRequiredEvent: LiveData<ThreeDsEventData.ChallengeRequiredData> = _challengeRequiredEvent

    private val _threeDsFinishedEvent = MutableLiveData<String>()
    val threeDsFinishedEvent: LiveData<String> = _threeDsFinishedEvent

    fun startThreeDsFlow() {
        viewModelScope.launch {
            threeDsInteractor.initialize(
                ThreeDsInitParams(
                    is3DSSanityCheckEnabled = settings.debugOptions.is3DSSanityCheckEnabled,
                    locale = settings.locale,
                ),
            ).onFailure { throwable ->
                _threeDsErrorEvent.postValue(throwable)
            }.onSuccess {
                _threeDsInitEvent.postValue(it)
            }
        }
    }

    fun performAuthorization(
        activity: Activity,
        supportedThreeDsProtocolVersions: List<String>,
        paymentMethodToken: String?,
        cardNetwork: CardNetwork.Type?,
        processingDisplayTimeMs: Long = PROCESSING_SCREEN_MIN_DISPLAY_MS,
    ) {
        runIfChallengeNotInProgress {
            viewModelScope.launch {
                threeDsInteractor.authenticateSdk(
                    supportedThreeDsProtocolVersions = supportedThreeDsProtocolVersions,
                    cardNetwork = requireNotNull(cardNetwork),
                ).onFailure { throwable ->
                    _threeDsErrorEvent.postValue(throwable)
                }.onSuccess { transaction ->
                    beginRemoteAuth(activity, transaction, paymentMethodToken, processingDisplayTimeMs)
                }
            }
        }
    }

    private suspend fun beginRemoteAuth(
        activity: Activity,
        transaction: Transaction,
        paymentMethodToken: String?,
        processingDisplayTimeMs: Long,
    ) {
        // Get and show progress view per EMVCo requirements
        val progressView = threeDsInteractor.getProgressView(activity, transaction)
        progressView?.showProgress()

        val startTime = getCurrentTimeMillis()

        threeDsInteractor.beginRemoteAuth(
            getThreeDsParams(transaction.authenticationRequestParameters),
            paymentMethodToken = requireNotNull(paymentMethodToken),
        ).onFailure { throwable ->
            progressView?.hideProgress()
            _threeDsErrorEvent.postValue(throwable)
            transaction.close()
        }.onSuccess { result ->
            // Ensure minimum display time
            val elapsed = getCurrentTimeMillis() - startTime
            if (elapsed < processingDisplayTimeMs) {
                delay(processingDisplayTimeMs - elapsed)
            }

            when (result.authentication.responseCode) {
                ResponseCode.CHALLENGE -> {
                    // Don't hide progress - SDK manages transition to challenge UI
                    // per Netcetera docs to prevent screen flickering
                    _challengeRequiredEvent.postValue(
                        ThreeDsEventData.ChallengeRequiredData(
                            transaction,
                            result,
                        ),
                    )
                }

                else -> {
                    progressView?.hideProgress()
                    _threeDsFinishedEvent.postValue(result.resumeToken)
                    transaction.close()
                }
            }
        }
    }

    fun performChallenge(
        activity: Activity,
        transaction: Transaction,
        authData: BeginAuthResponse,
    ) {
        runIfChallengeNotInProgress {
            challengeInProgress = true
            logThreeDsScreenPresented()
            viewModelScope.launch {
                threeDsInteractor.performChallenge(
                    activity,
                    transaction,
                    authData,
                ).catch { throwable ->
                    logThreeDsScreenDismissed()
                    _threeDsErrorEvent.postValue(throwable)
                }.collect {
                    logThreeDsScreenDismissed()
                    _threeDsStatusChangedEvent.postValue(it)
                }
            }
        }
    }

    fun continueRemoteAuth(
        challengeStatusData: ChallengeStatusData,
        supportedThreeDsProtocolVersions: List<String>,
    ) {
        viewModelScope.launch {
            threeDsInteractor.continueRemoteAuth(
                challengeStatusData = challengeStatusData,
                supportedThreeDsProtocolVersions = supportedThreeDsProtocolVersions,
            ).onFailure { throwable ->
                _threeDsErrorEvent.postValue(throwable)
            }.onSuccess { response ->
                _threeDsFinishedEvent.postValue(response.resumeToken)
            }
        }
    }

    fun continueRemoteAuthWithException(
        throwable: Throwable,
        resumeToken: String?,
        supportedThreeDsProtocolVersions: List<String>,
        paymentMethodToken: String?,
    ) {
        viewModelScope.launch {
            threeDsInteractor.continueRemoteAuthWithException(
                throwable = throwable,
                supportedThreeDsProtocolVersions = supportedThreeDsProtocolVersions,
                paymentMethodToken = requireNotNull(paymentMethodToken),
            ).onFailure {
                _threeDsFinishedEvent.postValue(resumeToken.orEmpty())
            }.onSuccess { response ->
                _threeDsFinishedEvent.postValue(response.resumeToken)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onCleared() {
        super.onCleared()
        threeDsInteractor.cleanup()
    }

    fun addAnalyticsEvent(params: BaseAnalyticsParams) = viewModelScope.launch {
        analyticsInteractor(params)
    }

    sealed class ThreeDsEventData {
        class ChallengeRequiredData(
            val transaction: Transaction,
            val authData: BeginAuthResponse,
        )
    }

    private fun getThreeDsParams(authenticationRequestParameters: AuthenticationRequestParameters) = run {
        ThreeDsCheckoutParams(
            authenticationRequestParameters,
        )
    }

    private fun logThreeDsScreenPresented() = addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.PRESENT,
            ObjectType.`3RD_PARTY_VIEW`,
            Place.`3DS_VIEW`,
        ),
    )

    private fun logThreeDsScreenDismissed() = addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.DISMISS,
            ObjectType.`3RD_PARTY_VIEW`,
            Place.`3DS_VIEW`,
        ),
    )

    private fun runIfChallengeNotInProgress(block: () -> Unit) = challengeInProgress.takeIf { it.not() }?.run {
        block()
    }

    companion object {
        /**
         * EMVCo requires the processing screen to be displayed for minimum 2 seconds
         * during the Authentication Request.
         */
        private const val PROCESSING_SCREEN_MIN_DISPLAY_MS = 2000L
    }
}
