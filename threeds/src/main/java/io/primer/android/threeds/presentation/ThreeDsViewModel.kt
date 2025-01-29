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
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.payments.core.tokenization.data.model.ResponseCode
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsCheckoutParams
import io.primer.android.threeds.domain.models.ThreeDsInitParams
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class ThreeDsViewModel(
    private val threeDsInteractor: ThreeDsInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val settings: PrimerSettings,
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
    val challengeRequiredEvent: LiveData<ThreeDsEventData.ChallengeRequiredData> =
        _challengeRequiredEvent

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

    fun performAuthorization(supportedThreeDsProtocolVersions: List<String>) {
        runIfChallengeNotInProgress {
            viewModelScope.launch {
                threeDsInteractor.authenticateSdk(supportedThreeDsProtocolVersions = supportedThreeDsProtocolVersions)
                    .onFailure { throwable ->
                        _threeDsErrorEvent.postValue(throwable)
                    }.onSuccess { transaction ->
                        threeDsInteractor.beginRemoteAuth(
                            getThreeDsParams(transaction.authenticationRequestParameters),
                        ).onFailure { throwable ->
                            _threeDsErrorEvent.postValue(throwable)
                            transaction.close()
                        }.onSuccess { result ->
                            when (result.authentication.responseCode) {
                                ResponseCode.CHALLENGE ->
                                    _challengeRequiredEvent.postValue(
                                        ThreeDsEventData.ChallengeRequiredData(
                                            transaction,
                                            result,
                                        ),
                                    )

                                else -> {
                                    _threeDsFinishedEvent.postValue(result.resumeToken)
                                    transaction.close()
                                }
                            }
                        }
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
                    transaction.close()
                }.onCompletion { challengeInProgress = false }.collect {
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
            )
                .onFailure { throwable ->
                    _threeDsErrorEvent.postValue(throwable)
                }
                .onSuccess { response ->
                    _threeDsFinishedEvent.postValue(response.resumeToken)
                }
        }
    }

    fun continueRemoteAuthWithException(
        throwable: Throwable,
        supportedThreeDsProtocolVersions: List<String>,
    ) {
        viewModelScope.launch {
            threeDsInteractor.continueRemoteAuthWithException(
                throwable = throwable,
                supportedThreeDsProtocolVersions = supportedThreeDsProtocolVersions,
            ).onFailure {
                _threeDsErrorEvent.postValue(throwable)
            }
                .onSuccess { response ->
                    _threeDsFinishedEvent.postValue(response.resumeToken)
                }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onCleared() {
        super.onCleared()
        threeDsInteractor.cleanup()
    }

    fun addAnalyticsEvent(params: BaseAnalyticsParams) =
        viewModelScope.launch {
            analyticsInteractor(params)
        }

    sealed class ThreeDsEventData {
        class ChallengeRequiredData(
            val transaction: Transaction,
            val authData: BeginAuthResponse,
        )
    }

    private fun getThreeDsParams(authenticationRequestParameters: AuthenticationRequestParameters) =
        run {
            ThreeDsCheckoutParams(
                authenticationRequestParameters,
            )
        }

    private fun logThreeDsScreenPresented() =
        addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.PRESENT,
                ObjectType.`3RD_PARTY_VIEW`,
                Place.`3DS_VIEW`,
            ),
        )

    private fun logThreeDsScreenDismissed() =
        addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.DISMISS,
                ObjectType.`3RD_PARTY_VIEW`,
                Place.`3DS_VIEW`,
            ),
        )

    private fun runIfChallengeNotInProgress(block: () -> Unit) =
        challengeInProgress.takeIf { it.not() }?.run {
            block()
        }
}
