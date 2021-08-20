package io.primer.android.threeds.presentation

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.primer.android.BuildConfig
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.ChallengePreference
import io.primer.android.threeds.data.models.ResponseCode
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor
import io.primer.android.threeds.domain.models.ThreeDsConfigParams
import io.primer.android.threeds.domain.models.ThreeDsInitParams
import io.primer.android.threeds.domain.models.ThreeDsParams
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class ThreeDsViewModel(
    private val threeDsInteractor: ThreeDsInteractor,
    private val checkoutConfig: CheckoutConfig,
) : ViewModel() {

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
            threeDsInteractor.validate(ThreeDsConfigParams(checkoutConfig))
                .catch { _threeDsFinishedEvent.postValue(Unit) }.collect {
                    threeDsInteractor.initialize(
                        ThreeDsInitParams(
                            checkoutConfig.debugOptions?.is3DSSanityCheckEnabled
                                ?: BuildConfig.DEBUG.not(),
                            checkoutConfig.locale
                        )
                    ).catch {
                        _threeDsFinishedEvent.postValue(Unit)
                    }.collect {
                        _threeDsInitEvent.postValue(it)
                    }
                }
        }
    }

    fun performAuthorization() {
        viewModelScope.launch {
            val protocolVersion = ProtocolVersion.V_210
            threeDsInteractor.authenticateSdk(protocolVersion)
                .catch {
                    _threeDsFinishedEvent.postValue(Unit)
                }.collect { transaction ->
                    threeDsInteractor.beginRemoteAuth(
                        ThreeDsParams(
                            transaction.authenticationRequestParameters,
                            checkoutConfig,
                            protocolVersion,
                            ChallengePreference.REQUESTED_BY_REQUESTOR
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
}
