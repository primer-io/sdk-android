package io.primer.android.threeds.presentation

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.netcetera.threeds.sdk.api.transaction.AuthenticationRequestParameters
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.presentation.base.BaseViewModel
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.ChallengePreference
import io.primer.android.threeds.data.models.ResponseCode
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsCheckoutParams
import io.primer.android.threeds.domain.models.ThreeDsInitParams
import io.primer.android.threeds.domain.models.ThreeDsVaultParams
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

internal class ThreeDsViewModel(
    private val threeDsInteractor: ThreeDsInteractor,
    analyticsInteractor: AnalyticsInteractor,
    private val config: PrimerConfig,
) : BaseViewModel(analyticsInteractor) {

    private val _threeDsInitEvent = MutableLiveData<Unit>()
    val threeDsInitEvent: LiveData<Unit> = _threeDsInitEvent

    private val _challengeStatusChangedEvent = MutableLiveData<ChallengeStatusData>()
    val challengeStatusChangedEvent: LiveData<ChallengeStatusData> = _challengeStatusChangedEvent

    private val _challengeRequiredEvent = MutableLiveData<ThreeDsEventData.ChallengeRequiredData>()
    val challengeRequiredEvent: LiveData<ThreeDsEventData.ChallengeRequiredData> =
        _challengeRequiredEvent

    private val _threeDsFinishedEvent = MutableLiveData<Unit>()
    val threeDsFinishedEvent: LiveData<Unit> = _threeDsFinishedEvent

    fun startThreeDsFlow() {
        viewModelScope.launch {
            threeDsInteractor.initialize(
                ThreeDsInitParams(
                    config.settings.debugOptions.is3DSSanityCheckEnabled,
                    config.settings.locale
                )
            ).catch {
                _threeDsFinishedEvent.postValue(Unit)
            }.collect {
                _threeDsInitEvent.postValue(it)
            }
        }
    }

    fun performAuthorization() {
        viewModelScope.launch {
            threeDsInteractor.authenticateSdk()
                .catch {
                    _threeDsFinishedEvent.postValue(Unit)
                }.collect { transaction ->
                    threeDsInteractor.beginRemoteAuth(
                        getThreeDsParams(
                            transaction.authenticationRequestParameters,
                        )
                    )
                        .catch {
                            _threeDsFinishedEvent.postValue(Unit)
                            transaction.close()
                        }
                        .collect { result ->
                            when (result.authentication.responseCode) {
                                ResponseCode.CHALLENGE -> _challengeRequiredEvent.postValue(
                                    ThreeDsEventData.ChallengeRequiredData(
                                        transaction,
                                        result
                                    )
                                )
                                else -> {
                                    _threeDsFinishedEvent.postValue(Unit)
                                    transaction.close()
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
        viewModelScope.launch {
            threeDsInteractor.performChallenge(
                activity,
                transaction,
                authData,
            ).catch {
                _threeDsFinishedEvent.postValue(Unit)
                transaction.close()
            }.collect {
                _challengeStatusChangedEvent.postValue(it)
            }
        }
    }

    fun continueRemoteAuth(
        token: String,
    ) {
        viewModelScope.launch {
            threeDsInteractor.continueRemoteAuth(token)
                .catch {
                    _threeDsFinishedEvent.postValue(Unit)
                }
                .collect {
                    _threeDsFinishedEvent.postValue(Unit)
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        threeDsInteractor.cleanup()
    }

    sealed class ThreeDsEventData {
        class ChallengeRequiredData(
            val transaction: Transaction,
            val authData: BeginAuthResponse,
        )
    }

    private fun getThreeDsParams(authenticationRequestParameters: AuthenticationRequestParameters) =
        run {
            when (config.intent.paymentMethodIntent.isCheckout) {
                true -> ThreeDsCheckoutParams(
                    authenticationRequestParameters,
                    ChallengePreference.REQUESTED_DUE_TO_MANDATE
                )
                false -> ThreeDsVaultParams(
                    authenticationRequestParameters,
                    config,
                    ChallengePreference.REQUESTED_DUE_TO_MANDATE
                )
            }
        }
}
