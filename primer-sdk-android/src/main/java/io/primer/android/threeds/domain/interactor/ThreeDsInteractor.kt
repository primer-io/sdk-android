package io.primer.android.threeds.domain.interactor

import android.app.Activity
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperFactory
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.helpers.ResumeEventResolver
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.extensions.doOnError
import io.primer.android.logging.DefaultLogger
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.data.models.common.CardNetwork
import io.primer.android.threeds.data.models.common.ResponseCode
import io.primer.android.threeds.data.models.postAuth.PostAuthResponse
import io.primer.android.threeds.domain.models.BaseThreeDsParams
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.FailureThreeDsContinueAuthParams
import io.primer.android.threeds.domain.models.SuccessThreeDsContinueAuthParams
import io.primer.android.threeds.domain.models.ThreeDsInitParams
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.domain.respository.ThreeDsAppUrlRepository
import io.primer.android.threeds.domain.respository.ThreeDsConfigurationRepository
import io.primer.android.threeds.domain.respository.ThreeDsRepository
import io.primer.android.threeds.domain.respository.ThreeDsServiceRepository
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

internal interface ThreeDsInteractor {

    suspend fun initialize(
        threeDsInitParams: ThreeDsInitParams
    ): Flow<Unit>

    fun authenticateSdk(): Flow<Transaction>

    fun beginRemoteAuth(
        threeDsParams: BaseThreeDsParams
    ): Flow<BeginAuthResponse>

    fun performChallenge(
        activity: Activity,
        transaction: Transaction,
        authResponse: BeginAuthResponse
    ): Flow<ChallengeStatusData>

    fun continueRemoteAuth(
        challengeStatusData: ChallengeStatusData
    ): Flow<PostAuthResponse>

    fun continueRemoteAuthWithException(
        throwable: Throwable
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
    private val resumeEventResolver: ResumeEventResolver,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    private val errorMapperFactory: ErrorMapperFactory,
    private val analyticsRepository: AnalyticsRepository,
    private val logger: DefaultLogger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ThreeDsInteractor {

    override suspend fun initialize(
        threeDsInitParams: ThreeDsInitParams
    ) =
        threeDsConfigurationRepository.getConfiguration().flatMapLatest { keys ->
            threeDsServiceRepository.initializeProvider(
                threeDsInitParams.is3DSSanityCheckEnabled,
                threeDsInitParams.locale,
                clientTokenRepository.useThreeDsWeakValidation() ?: true,
                keys
            )
        }.flowOn(dispatcher)

    override fun authenticateSdk() =
        threeDsConfigurationRepository.getPreAuthConfiguration().flatMapLatest { authParams ->
            threeDsServiceRepository.performProviderAuth(
                CardNetwork.valueOf(
                    paymentMethodRepository.getPaymentMethod().paymentInstrumentData
                        ?.binData?.network.orEmpty().uppercase()
                ),
                authParams.protocolVersions.max(),
                authParams.environment
            )
        }.flowOn(dispatcher)

    override fun beginRemoteAuth(
        threeDsParams: BaseThreeDsParams
    ) =
        threeDsRepository.begin3DSAuth(
            paymentMethodRepository.getPaymentMethod().token,
            threeDsParams
        ).flowOn(dispatcher).onEach {
            // we mark flow ended and send results if there is no challenge
            if (it.authentication.responseCode != ResponseCode.CHALLENGE) {
                handleAuthEvent(it.token, it.resumeToken)
            }
        }

    override fun performChallenge(
        activity: Activity,
        transaction: Transaction,
        authResponse: BeginAuthResponse
    ) =
        threeDsServiceRepository.performChallenge(
            activity,
            transaction,
            authResponse,
            threeDsAppUrlRepository.getAppUrl(transaction) ?: run {
                when (
                    authResponse.authentication.protocolVersion.orEmpty() >=
                        ProtocolVersion.V_220.versionNumber
                ) {
                    true -> logger.warn(PRIMER_INVALID_APP_URL_ERROR)
                    false -> Unit
                }
                null
            },
            authResponse.authentication.protocolVersion.orEmpty()
        )

    override fun continueRemoteAuth(challengeStatusData: ChallengeStatusData) =
        threeDsConfigurationRepository.getPreAuthConfiguration().flatMapLatest { params ->
            threeDsRepository.continue3DSAuth(
                challengeStatusData.paymentMethodToken,
                SuccessThreeDsContinueAuthParams(
                    threeDsServiceRepository.threeDsSdkVersion,
                    params.protocolVersions.max().versionNumber
                )
            )
        }
            .flowOn(dispatcher)
            .onEach { handleAuthEvent(it.token, it.resumeToken) }
            .doOnError { handleErrorEvent(it) }

    override fun continueRemoteAuthWithException(throwable: Throwable) =
        threeDsConfigurationRepository.getPreAuthConfiguration().flatMapLatest { params ->
            threeDsRepository.continue3DSAuth(
                paymentMethodRepository.getPaymentMethod().token,
                FailureThreeDsContinueAuthParams(
                    threeDsServiceRepository.threeDsSdkVersion,
                    params.protocolVersions.max().versionNumber,
                    errorMapperFactory.buildErrorMapper(ErrorMapperType.THREE_DS)
                        .getPrimerError(throwable).also { error ->
                            logAnalytics(error)
                            logger.warn(
                                "${error.description} ${error.recoverySuggestion.orEmpty()}"
                            )
                        }
                )
            )
        }
            .flowOn(dispatcher)
            .onEach { handleAuthEvent(it.token, it.resumeToken) }
            .doOnError { handleErrorEvent(it) }

    override fun cleanup() =
        try {
            threeDsServiceRepository.performCleanup()
        } catch (_: Exception) {
        }

    private fun handleAuthEvent(token: PaymentMethodTokenInternal, resumeToken: String) {
        when (clientTokenRepository.getClientTokenIntent()) {
            ClientTokenIntent.`3DS_AUTHENTICATION`.name -> resumeEventResolver.resolve(
                token.paymentInstrumentType,
                if (paymentMethodRepository.getPaymentMethod().token == token.token) {
                    paymentMethodRepository.getPaymentMethod().isVaulted
                } else { token.isVaulted },
                resumeToken
            )
            else -> Unit
        }
    }

    private fun handleErrorEvent(throwable: Throwable) {
        when (clientTokenRepository.getClientTokenIntent()) {
            ClientTokenIntent.`3DS_AUTHENTICATION`.name -> {
                baseErrorEventResolver.resolve(throwable, ErrorMapperType.THREE_DS)
            }
            else -> Unit
        }
    }

    private fun logAnalytics(error: PrimerError) = analyticsRepository.addEvent(
        MessageAnalyticsParams(
            MessageType.ERROR,
            "$ANALYTICS_TAG_3DS: ${error.description}",
            Severity.ERROR,
            error.diagnosticsId,
            error.context
        )
    )

    companion object {

        private val PRIMER_INVALID_APP_URL_ERROR = """
            threeDsAppRequestorUrl is not having a valid format ("https://applink"). In case you
            want to support redirecting back during the OOB flows please set correct
            threeDsAppRequestorUrl in PrimerThreeDsOptions during SDK initialization.
        """.trimIndent()
        const val ANALYTICS_TAG_3DS = "Primer3DS"
    }
}
