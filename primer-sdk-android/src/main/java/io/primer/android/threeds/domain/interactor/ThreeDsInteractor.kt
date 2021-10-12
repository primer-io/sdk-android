package io.primer.android.threeds.domain.interactor

import android.app.Activity
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.doOnError
import io.primer.android.extensions.toResumeErrorEvent
import io.primer.android.logging.DefaultLogger
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

internal interface ThreeDsInteractor {

    suspend fun validate(
        threeDsConfigParams: ThreeDsConfigParams,
    ): Flow<Unit>

    suspend fun initialize(
        threeDsInitParams: ThreeDsInitParams,
    ): Flow<Unit>

    fun authenticateSdk(): Flow<Transaction>

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
    private val clientTokenRepository: ClientTokenRepository,
    private val threeDsAppUrlRepository: ThreeDsAppUrlRepository,
    private val threeDsConfigurationRepository: ThreeDsConfigurationRepository,
    private val threeDsConfigValidator: ThreeDsConfigValidator,
    private val resumeHandlerFactory: ResumeHandlerFactory,
    private val eventDispatcher: EventDispatcher,
    private val logger: DefaultLogger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ThreeDsInteractor {

    override suspend fun validate(threeDsConfigParams: ThreeDsConfigParams) =
        threeDsConfigValidator.validate(threeDsConfigParams).doOnError {
            handleErrorEvent(it)
        }

    override suspend fun initialize(
        threeDsInitParams: ThreeDsInitParams,
    ) =
        threeDsConfigurationRepository.getConfiguration().flatMapLatest { keys ->
            threeDsServiceRepository.initializeProvider(
                threeDsInitParams.is3DSSanityCheckEnabled,
                threeDsInitParams.locale,
                keys
            )
        }.flowOn(dispatcher)
            .doOnError {
                logger.warn(PRIMER_3DS_INIT_ERROR)
                handleErrorEvent(it)
            }

    override fun authenticateSdk() =
        threeDsConfigurationRepository.getProtocolVersion().flatMapLatest { protocolVersion ->
            threeDsServiceRepository.performProviderAuth(
                CardNetwork.valueOf(
                    paymentMethodRepository.getPaymentMethod().paymentInstrumentData
                        ?.binData?.network.orEmpty().uppercase()
                ),
                protocolVersion
            )
        }.flowOn(dispatcher)
            .doOnError {
                handleErrorEvent(it)
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
                handleAuthEvent(it.token, it.resumeToken)
            }
        }.doOnError {
            handleErrorEvent(it)
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
                handleErrorEvent(it)
            }

    override fun continueRemoteAuth(sdkTokenId: String) =
        threeDsRepository.continue3DSAuth(sdkTokenId)
            .flowOn(dispatcher)
            .onEach { handleAuthEvent(it.token, it.resumeToken) }
            .doOnError { handleErrorEvent(it) }

    override fun cleanup() =
        try {
            threeDsServiceRepository.performCleanup()
        } catch (_: Exception) {
        }

    private fun handleAuthEvent(token: PaymentMethodTokenInternal, resumeToken: String? = null) {
        when (clientTokenRepository.getClientTokenIntent()) {
            ClientTokenIntent.`3DS_AUTHENTICATION` -> {
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.ResumeSuccess(
                        resumeToken.orEmpty(),
                        resumeHandlerFactory.getResumeHandler(token.paymentInstrumentType)
                    )
                )
            }
            else -> dispatchTokenEvents(token)
        }
    }

    private fun handleErrorEvent(throwable: Throwable) {
        when (clientTokenRepository.getClientTokenIntent()) {
            ClientTokenIntent.`3DS_AUTHENTICATION` -> {
                eventDispatcher.dispatchEvent(
                    throwable.toResumeErrorEvent(throwable.message.orEmpty())
                )
            }
            else -> dispatchTokenEvents(
                paymentMethodRepository.getPaymentMethod()
                    .setClientThreeDsError(throwable.message.orEmpty())
            )
        }
    }

    private fun dispatchTokenEvents(token: PaymentMethodTokenInternal) {
        val externalToken = PaymentMethodTokenAdapter.internalToExternal(token)
        val events = mutableListOf<CheckoutEvent>(
            CheckoutEvent.TokenizationSuccess(
                externalToken,
                resumeHandlerFactory.getResumeHandler(token.paymentInstrumentType)
            )
        )
        if (token.tokenType == TokenType.MULTI_USE) {
            events.add(CheckoutEvent.TokenAddedToVault(externalToken))
        }
        eventDispatcher.dispatchEvents(events)
    }

    companion object {

        const val PRIMER_3DS_INIT_ERROR = "Cannot perform 3DS. Continue without 3DS." +
            "Please check debug options in order to run 3DS in debug mode."
    }
}
