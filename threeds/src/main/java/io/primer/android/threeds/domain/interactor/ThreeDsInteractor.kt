package io.primer.android.threeds.domain.interactor

import android.app.Activity
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.threeds.BuildConfig
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.data.models.postAuth.PostAuthResponse
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkProvider
import io.primer.android.threeds.domain.models.BaseThreeDsParams
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.FailureThreeDsContinueAuthParams
import io.primer.android.threeds.domain.models.SuccessThreeDsContinueAuthParams
import io.primer.android.threeds.domain.models.ThreeDsInitParams
import io.primer.android.threeds.domain.repository.ThreeDsAppUrlRepository
import io.primer.android.threeds.domain.repository.ThreeDsConfigurationRepository
import io.primer.android.threeds.domain.repository.ThreeDsRepository
import io.primer.android.threeds.domain.repository.ThreeDsServiceRepository
import io.primer.android.threeds.errors.domain.exception.ThreeDsLibraryNotFoundException
import io.primer.android.threeds.errors.domain.exception.ThreeDsLibraryVersionMismatchException
import io.primer.android.threeds.helpers.ProtocolVersion
import io.primer.android.threeds.helpers.ThreeDsLibraryVersionValidator
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal interface ThreeDsInteractor {

    suspend fun initialize(
        threeDsInitParams: ThreeDsInitParams
    ): Result<Unit>

    suspend fun authenticateSdk(supportedThreeDsProtocolVersions: List<String>): Result<Transaction>

    suspend fun beginRemoteAuth(
        threeDsParams: BaseThreeDsParams
    ): Result<BeginAuthResponse>

    fun performChallenge(
        activity: Activity,
        transaction: Transaction,
        authResponse: BeginAuthResponse
    ): Flow<ChallengeStatusData>

    suspend fun continueRemoteAuth(
        challengeStatusData: ChallengeStatusData,
        supportedThreeDsProtocolVersions: List<String>
    ): Result<PostAuthResponse>

    suspend fun continueRemoteAuthWithException(
        throwable: Throwable,
        supportedThreeDsProtocolVersions: List<String>
    ): Result<PostAuthResponse>

    fun cleanup()
}

internal class DefaultThreeDsInteractor(
    private val threeDsSdkClassValidator: ThreeDsSdkClassValidator,
    private val threeDsLibraryVersionValidator: ThreeDsLibraryVersionValidator,
    private val threeDsServiceRepository: ThreeDsServiceRepository,
    private val threeDsRepository: ThreeDsRepository,
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository,
    private val threeDsAppUrlRepository: ThreeDsAppUrlRepository,
    private val threeDsConfigurationRepository: ThreeDsConfigurationRepository,
    private val errorMapperRegistry: ErrorMapperRegistry,
    private val analyticsRepository: AnalyticsRepository,
    private val logReporter: LogReporter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ThreeDsInteractor {

    override suspend fun initialize(
        threeDsInitParams: ThreeDsInitParams
    ) = withContext(dispatcher) {
        when {
            threeDsSdkClassValidator.is3dsSdkIncluded().not() ->
                Result.failure(
                    ThreeDsLibraryNotFoundException(
                        ThreeDsSdkClassValidator.THREE_DS_CLASS_NOT_LOADED_ERROR
                    )
                )

            threeDsLibraryVersionValidator.isValidVersion().not() -> {
                Result.failure(
                    ThreeDsLibraryVersionMismatchException(
                        BuildConfig.SDK_VERSION_STRING,
                        ThreeDsFailureContextParams(
                            threeDsSdkVersion = null,
                            initProtocolVersion = null,
                            threeDsWrapperSdkVersion = BuildConfig.SDK_VERSION_STRING,
                            threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name
                        )
                    )
                )
            }

            else -> threeDsConfigurationRepository.getConfiguration().flatMap { keys ->
                threeDsServiceRepository.initializeProvider(
                    threeDsInitParams.is3DSSanityCheckEnabled,
                    threeDsInitParams.locale,
                    keys
                )
            }.map { }
        }
    }

    override suspend fun authenticateSdk(supportedThreeDsProtocolVersions: List<String>) = withContext(dispatcher) {
        threeDsConfigurationRepository.getPreAuthConfiguration(supportedThreeDsProtocolVersions)
            .flatMap { authParams ->
                threeDsServiceRepository.performProviderAuth(
                    CardNetwork.Type.valueOrNull(
                        tokenizedPaymentMethodRepository.getPaymentMethod().paymentInstrumentData
                            ?.binData?.network.orEmpty().uppercase()
                    ) ?: CardNetwork.Type.OTHER,
                    authParams.protocolVersions.max(),
                    authParams.environment
                )
            }
    }

    override suspend fun beginRemoteAuth(
        threeDsParams: BaseThreeDsParams
    ) = withContext(dispatcher) {
        threeDsRepository.begin3DSAuth(
            tokenizedPaymentMethodRepository.getPaymentMethod().token,
            threeDsParams
        )
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
                    true -> logReporter.warn(PRIMER_INVALID_APP_URL_ERROR, ANALYTICS_3DS_COMPONENT)
                    false -> Unit
                }
                null
            },
            authResponse.authentication.protocolVersion.orEmpty()
        )

    override suspend fun continueRemoteAuth(
        challengeStatusData: ChallengeStatusData,
        supportedThreeDsProtocolVersions: List<String>
    ) = withContext(dispatcher) {
        threeDsConfigurationRepository.getPreAuthConfiguration(supportedThreeDsProtocolVersions)
            .flatMap { params ->
                threeDsRepository.continue3DSAuth(
                    challengeStatusData.paymentMethodToken,
                    SuccessThreeDsContinueAuthParams(
                        threeDsServiceRepository.threeDsSdkVersion,
                        params.protocolVersions.max().versionNumber
                    )
                )
            }
    }

    override suspend fun continueRemoteAuthWithException(
        throwable: Throwable,
        supportedThreeDsProtocolVersions: List<String>
    ) = withContext(dispatcher) {
        threeDsConfigurationRepository.getPreAuthConfiguration(supportedThreeDsProtocolVersions)
            .flatMap { params ->
                threeDsRepository.continue3DSAuth(
                    tokenizedPaymentMethodRepository.getPaymentMethod().token,
                    FailureThreeDsContinueAuthParams(
                        threeDsSdkVersion = threeDsServiceRepository.threeDsSdkVersion,
                        initProtocolVersion = params.protocolVersions.max().versionNumber,
                        error = errorMapperRegistry.getPrimerError(throwable).also { error ->
                            logAnalytics(error)
                            logReporter.warn(
                                "${error.description} ${error.recoverySuggestion.orEmpty()}",
                                ANALYTICS_3DS_COMPONENT
                            )
                        }
                    )
                )
            }
    }

    override fun cleanup() =
        try {
            threeDsServiceRepository.performCleanup()
        } catch (_: Exception) {
        } catch (_: NoClassDefFoundError) {
        }

    private fun logAnalytics(error: PrimerError) = analyticsRepository.addEvent(
        MessageAnalyticsParams(
            MessageType.ERROR,
            "$ANALYTICS_3DS_COMPONENT: ${error.description}",
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
        const val ANALYTICS_3DS_COMPONENT = "3DS"
    }
}
