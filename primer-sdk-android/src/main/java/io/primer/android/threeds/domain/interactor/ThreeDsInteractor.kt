package io.primer.android.threeds.domain.interactor

import android.app.Activity
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.doOnError
import io.primer.android.logging.Logger
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.model.dto.TokenType
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.CardNetwork
import io.primer.android.threeds.data.models.PostAuthResponse
import io.primer.android.threeds.data.models.ResponseCode
import io.primer.android.threeds.domain.respository.ThreeDsRepository
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsConfigParams
import io.primer.android.threeds.domain.models.ThreeDsInitParams
import io.primer.android.threeds.domain.models.ThreeDsParams
import io.primer.android.threeds.domain.models.toBeginAuthRequest
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.domain.respository.ThreeDsAppUrlRepository
import io.primer.android.threeds.domain.respository.ThreeDsConfigurationRepository
import io.primer.android.threeds.domain.respository.ThreeDsServiceRepository
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator
import io.primer.android.threeds.helpers.ProtocolVersion
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

internal interface ThreeDsInteractor {

    suspend fun validate(
        threeDsConfigParams: ThreeDsConfigParams,
    ): Flow<Unit>

    suspend fun initialize(
        threeDsInitParams: ThreeDsInitParams,
    ): Flow<Unit>

    fun authenticateSdk(
        protocolVersion: ProtocolVersion,
    ): Flow<Transaction>

    fun beginRemoteAuth(
        threeDsParams: ThreeDsParams,
    ): Flow<BeginAuthResponse>

    fun performChallenge(
        activity: Activity,
        transaction: Transaction,
        authResponse: BeginAuthResponse,
    ): Flow<ChallengeStatusData>

    fun continueRemoteAuth(
        sdkTokenId: String,
    ): Flow<PostAuthResponse>

    fun cleanup()
}

internal class DefaultThreeDsInteractor(
    private val threeDsServiceRepository: ThreeDsServiceRepository,
    private val threeDsRepository: ThreeDsRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val threeDsAppUrlRepository: ThreeDsAppUrlRepository,
    private val threeDsConfigurationRepository: ThreeDsConfigurationRepository,
    private val threeDsConfigValidator: ThreeDsConfigValidator,
    private val eventDispatcher: EventDispatcher,
    private val logger: Logger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ThreeDsInteractor {

    override suspend fun validate(threeDsConfigParams: ThreeDsConfigParams) =
        threeDsConfigValidator.validate(threeDsConfigParams).doOnError {
            dispatchEvents(
                paymentMethodRepository.getPaymentMethod()
                    .setClientThreeDsError(
                        it.message.orEmpty()
                    )
            )
        }

    override suspend fun initialize(
        threeDsInitParams: ThreeDsInitParams,
    ) =
        threeDsConfigurationRepository.getConfiguration().flatMapConcat { keys ->
            threeDsServiceRepository.initializeProvider(
                threeDsInitParams.is3DSSanityCheckEnabled,
                threeDsInitParams.locale,
                keys
            )
        }.flowOn(dispatcher)
            .doOnError {
                logger.warn(PRIMER_3DS_INIT_ERROR)
                dispatchEvents(
                    paymentMethodRepository.getPaymentMethod()
                        .setClientThreeDsError(it.message.orEmpty())
                )
            }

    override fun authenticateSdk(
        protocolVersion: ProtocolVersion,
    ) =
        threeDsServiceRepository.performProviderAuth(
            CardNetwork.valueOf(
                paymentMethodRepository.getPaymentMethod().paymentInstrumentData
                    ?.binData?.network.orEmpty().toUpperCase()
            ),
            protocolVersion
        ).flowOn(dispatcher)
            .doOnError {
                dispatchEvents(
                    paymentMethodRepository.getPaymentMethod().setClientThreeDsError(
                        it.message.orEmpty()
                    )
                )
            }

    override fun beginRemoteAuth(
        threeDsParams: ThreeDsParams,
    ) =
        threeDsRepository.begin3DSAuth(
            paymentMethodRepository.getPaymentMethod().token,
            threeDsParams.toBeginAuthRequest()
        ).flowOn(dispatcher).onEach {
            // we mark flow ended and send results if there is no challenge
            if (it.authentication.responseCode != ResponseCode.CHALLENGE) {
                dispatchEvents(it.token)
            }
        }.doOnError {
            dispatchEvents(
                paymentMethodRepository.getPaymentMethod()
                    .setClientThreeDsError(it.message.orEmpty())
            )
        }

    override fun performChallenge(
        activity: Activity,
        transaction: Transaction,
        authResponse: BeginAuthResponse,
    ) =
        threeDsServiceRepository.performChallenge(
            activity,
            transaction,
            authResponse,
            threeDsAppUrlRepository.getAppUrl(transaction)
        )
            .doOnError {
                dispatchEvents(
                    authResponse.token.setClientThreeDsError(
                        it.message.orEmpty()
                    )
                )
            }

    override fun continueRemoteAuth(sdkTokenId: String) =
        threeDsRepository.continue3DSAuth(sdkTokenId)
            .flowOn(dispatcher)
            .onEach { dispatchEvents(it.token) }
            .doOnError { dispatchEvents(paymentMethodRepository.getPaymentMethod()) }

    override fun cleanup() =
        try {
            threeDsServiceRepository.performCleanup()
        } catch (_: Exception) {
        }

    private fun dispatchEvents(token: PaymentMethodTokenInternal) {
        val externalToken = PaymentMethodTokenAdapter.internalToExternal(token)
        val events = mutableListOf<CheckoutEvent>(
            CheckoutEvent.TokenizationSuccess(
                externalToken,
                ::completionHandler
            )
        )
        if (token.tokenType == TokenType.MULTI_USE) {
            events.add(CheckoutEvent.TokenAddedToVault(externalToken))
        }
        eventDispatcher.dispatchEvents(events)
    }

    private fun completionHandler(error: Error?) {
        if (error == null) {
            eventDispatcher.dispatchEvents(
                listOf(CheckoutEvent.ShowSuccess(successType = SuccessType.PAYMENT_SUCCESS))
            )
        } else {
            eventDispatcher.dispatchEvents(
                listOf(
                    CheckoutEvent.ShowError(errorType = ErrorType.PAYMENT_FAILED)
                )
            )
        }
    }

    companion object {

        const val PRIMER_3DS_INIT_ERROR = "Cannot perform 3DS. Continue without 3DS. " +
            "Please check debug options in order to run 3DS in debug mode."
    }
}
