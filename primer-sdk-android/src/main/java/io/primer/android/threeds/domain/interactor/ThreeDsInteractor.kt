package io.primer.android.threeds.domain.interactor

import android.app.Activity
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.helpers.ResumeEventResolver
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.tokenization.helpers.PostTokenizationEventResolver
import io.primer.android.extensions.doOnError
import io.primer.android.logging.DefaultLogger
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.CardNetwork
import io.primer.android.threeds.data.models.PostAuthResponse
import io.primer.android.threeds.data.models.ResponseCode
import io.primer.android.threeds.domain.models.BaseThreeDsParams
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsInitParams
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.domain.respository.ThreeDsAppUrlRepository
import io.primer.android.threeds.domain.respository.ThreeDsConfigurationRepository
import io.primer.android.threeds.domain.respository.ThreeDsRepository
import io.primer.android.threeds.domain.respository.ThreeDsServiceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

internal interface ThreeDsInteractor {

    suspend fun initialize(
        threeDsInitParams: ThreeDsInitParams,
    ): Flow<Unit>

    fun authenticateSdk(): Flow<Transaction>

    fun beginRemoteAuth(
        threeDsParams: BaseThreeDsParams,
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
    private val postTokenizationEventResolver: PostTokenizationEventResolver,
    private val resumeEventResolver: ResumeEventResolver,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    private val logger: DefaultLogger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ThreeDsInteractor {

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
        threeDsParams: BaseThreeDsParams,
    ) =
        threeDsRepository.begin3DSAuth(
            paymentMethodRepository.getPaymentMethod().token,
            threeDsParams
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
            ClientTokenIntent.`3DS_AUTHENTICATION`.name -> resumeEventResolver.resolve(
                token.paymentInstrumentType,
                resumeToken
            )
            else -> postTokenizationEventResolver.resolve(token)
        }
    }

    private fun handleErrorEvent(throwable: Throwable) {
        when (clientTokenRepository.getClientTokenIntent()) {
            ClientTokenIntent.`3DS_AUTHENTICATION`.name -> {
                baseErrorEventResolver.resolve(throwable, ErrorMapperType.PAYMENT_RESUME)
            }
            else -> postTokenizationEventResolver.resolve(
                paymentMethodRepository.getPaymentMethod()
                    .setClientThreeDsError(throwable.message.orEmpty())
            )
        }
    }

    companion object {

        const val PRIMER_3DS_INIT_ERROR = "Cannot perform 3DS. Continue without 3DS." +
            "Please check debug options in order to run 3DS in debug mode."
    }
}
