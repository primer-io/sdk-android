package io.primer.android.threeds.data.repository

import android.app.Activity
import android.content.Context
import android.os.Build
import com.netcetera.threeds.sdk.ThreeDS2ServiceInstance
import com.netcetera.threeds.sdk.api.ThreeDS2Service
import com.netcetera.threeds.sdk.api.configparameters.builder.ConfigurationBuilder
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.amexConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.cbConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.dinersSchemeConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.jcbConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.mastercardSchemeConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.newSchemeConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.unionSchemeConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.visaSchemeConfiguration
import com.netcetera.threeds.sdk.api.transaction.Transaction
import com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeParameters
import com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeStatusReceiver
import com.netcetera.threeds.sdk.api.transaction.challenge.events.CompletionEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.ProtocolErrorEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.RuntimeErrorEvent
import com.netcetera.threeds.sdk.api.ui.logic.UiCustomization
import com.netcetera.threeds.sdk.api.utils.DsRidValues
import io.primer.android.analytics.domain.models.ThreeDsFailureContextParams
import io.primer.android.analytics.domain.models.ThreeDsProtocolFailureContextParams
import io.primer.android.analytics.domain.models.ThreeDsRuntimeFailureContextParams
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.Environment
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.core.utils.DeviceInfo
import io.primer.android.threeds.BuildConfig
import io.primer.android.threeds.data.exception.ThreeDsChallengeCancelledException
import io.primer.android.threeds.data.exception.ThreeDsChallengeTimedOutException
import io.primer.android.threeds.data.exception.ThreeDsConfigurationException
import io.primer.android.threeds.data.exception.ThreeDsInitException
import io.primer.android.threeds.data.exception.ThreeDsInvalidStatusException
import io.primer.android.threeds.data.exception.ThreeDsMissingDirectoryServerException
import io.primer.android.threeds.data.exception.ThreeDsProtocolFailedException
import io.primer.android.threeds.data.exception.ThreeDsRuntimeFailedException
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkProvider
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.domain.repository.ThreeDsServiceRepository
import io.primer.android.threeds.helpers.ProtocolVersion
import io.primer.android.threeds.main.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import kotlin.coroutines.coroutineContext

@ExperimentalCoroutinesApi
internal class NetceteraThreeDsServiceRepository(
    private val context: Context,
    threeDS2ServiceLazy: Lazy<ThreeDS2Service> = lazy { ThreeDS2ServiceInstance.get() },
) : ThreeDsServiceRepository {
    private val threeDS2Service: ThreeDS2Service by threeDS2ServiceLazy

    override val threeDsSdkVersion: String?
        get() =
            try {
                threeDS2Service.sdkVersion
            } catch (ignored: Exception) {
                null
            } catch (ignored: NoClassDefFoundError) {
                null
            }

    @Suppress("LongMethod")
    override suspend fun initializeProvider(
        is3DSSanityCheckEnabled: Boolean,
        locale: Locale,
        threeDsKeysParams: ThreeDsKeysParams?,
    ): Result<Unit> =
        runSuspendCatching {
            coroutineContext.ensureActive()

            try {
                requireNotNull(threeDsKeysParams) { KEYS_CONFIG_ERROR }
                requireNotNull(threeDsKeysParams.apiKey) { API_KEY_CONFIG_ERROR }
            } catch (expected: IllegalArgumentException) {
                throw ThreeDsConfigurationException(
                    expected.message,
                    ThreeDsFailureContextParams(
                        threeDsSdkVersion = null,
                        initProtocolVersion = null,
                        threeDsWrapperSdkVersion = BuildConfig.SDK_VERSION_STRING,
                        threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    ),
                )
            }

            val configurationBuilder =
                ConfigurationBuilder()
                    .apiKey(threeDsKeysParams.apiKey)

            threeDsKeysParams.let { (environment, _, threeDsSecureCertificates) ->
                if (environment != Environment.PRODUCTION) {
                    threeDsSecureCertificates?.forEach { certificate ->
                        val scheme =
                            when (certificate.cardNetwork.uppercase()) {
                                // Choose specialized scheme constructor if available
                                CardNetwork.Type.MASTERCARD.name -> mastercardSchemeConfiguration()
                                CardNetwork.Type.VISA.name -> visaSchemeConfiguration()
                                CardNetwork.Type.AMEX.name -> amexConfiguration()
                                CardNetwork.Type.DINERS_CLUB.name -> dinersSchemeConfiguration()
                                CardNetwork.Type.UNIONPAY.name -> unionSchemeConfiguration()
                                CardNetwork.Type.JCB.name -> jcbConfiguration()
                                CardNetwork.Type.CARTES_BANCAIRES.name -> cbConfiguration()
                                else -> {
                                    // Fallback to default scheme constructor if no specialized API exists
                                    newSchemeConfiguration(TEST_SCHEME_NAME)
                                        .logo(R.drawable.ds_logo_visa.toString())
                                        .logoDark(R.drawable.ds_logo_visa.toString())
                                        .ids(listOf(TEST_SCHEME_ID))
                                }
                            }

                        configurationBuilder.configureScheme(
                            scheme
                                .encryptionPublicKey(certificate.encryptionKey)
                                .rootPublicKey(certificate.rootCertificate)
                                .build(),
                        )
                    }
                }
            }

            try {
                threeDS2Service.initialize(
                    context,
                    configurationBuilder.build(),
                    if (DeviceInfo.isSdkVersionAtLeast(Build.VERSION_CODES.O)) {
                        locale.stripExtensions().toString()
                    } else {
                        locale.toString()
                    },
                    emptyMap<UiCustomization.UiCustomizationType, UiCustomization>(),
                )
            } catch (expected: Exception) {
                throw ThreeDsInitException(
                    expected.message,
                    ThreeDsFailureContextParams(
                        threeDsSdkVersion = threeDsSdkVersion,
                        initProtocolVersion = null,
                        threeDsWrapperSdkVersion = BuildConfig.SDK_VERSION_STRING,
                        threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    ),
                )
            }

            coroutineContext.ensureActive()
            val warnings = threeDS2Service.warnings
            coroutineContext.ensureActive()
            if (is3DSSanityCheckEnabled.not() || warnings.isEmpty()) {
                Unit
            } else {
                throw ThreeDsInitException(
                    warnings.joinToString(" | ") { "${it.severity}  ${it.message}" },
                    ThreeDsFailureContextParams(
                        threeDsSdkVersion = threeDsSdkVersion,
                        initProtocolVersion = null,
                        threeDsWrapperSdkVersion = BuildConfig.SDK_VERSION_STRING,
                        threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    ),
                )
            }
        }

    override suspend fun performProviderAuth(
        cardNetwork: CardNetwork.Type,
        protocolVersion: ProtocolVersion,
        environment: Environment,
    ): Result<Transaction> =
        runSuspendCatching {
            threeDS2Service.createTransaction(
                directoryServerIdForCard(cardNetwork, environment),
                protocolVersion.versionNumber,
            )
        }

    override fun performChallenge(
        activity: Activity,
        transaction: Transaction,
        authResponse: BeginAuthResponse,
        threeDSAppURL: String?,
        initProtocolVersion: String,
    ): Flow<ChallengeStatusData> =
        callbackFlow {
            transaction.doChallenge(
                activity,
                ChallengeParameters().apply {
                    acsTransactionID =
                        authResponse.authentication.acsTransactionId
                    acsRefNumber =
                        authResponse.authentication.acsReferenceNumber
                    acsSignedContent =
                        authResponse.authentication.acsSignedContent
                    threeDSRequestorAppURL = threeDSAppURL
                    set3DSServerTransactionID(authResponse.authentication.transactionId)
                },
                object : ChallengeStatusReceiver {
                    override fun completed(completionEvent: CompletionEvent) {
                        if (completionEvent.transactionStatus ==
                            ChallengeStatusData.TRANSACTION_STATUS_SUCCESS
                        ) {
                            trySend(
                                ChallengeStatusData(
                                    authResponse.token.token,
                                    completionEvent.transactionStatus,
                                ),
                            )
                        } else {
                            cancel(
                                ThreeDsInvalidStatusException(
                                    completionEvent.transactionStatus,
                                    completionEvent.sdkTransactionID,
                                    THREE_DS_CHALLENGE_INVALID_STATUS_CODE,
                                    ThreeDsRuntimeFailureContextParams(
                                        threeDsSdkVersion = threeDsSdkVersion,
                                        initProtocolVersion = initProtocolVersion,
                                        threeDsWrapperSdkVersion = BuildConfig.SDK_VERSION_STRING,
                                        threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                                        THREE_DS_CHALLENGE_INVALID_STATUS_CODE,
                                    ),
                                    "3DS challenge failed.",
                                ),
                            )
                        }
                        close()
                    }

                    override fun cancelled() {
                        cancel(
                            ThreeDsChallengeCancelledException(
                                THREE_DS_CHALLENGE_CANCELLED_ERROR_CODE,
                                ThreeDsRuntimeFailureContextParams(
                                    threeDsSdkVersion = threeDsSdkVersion,
                                    initProtocolVersion = initProtocolVersion,
                                    threeDsWrapperSdkVersion = BuildConfig.SDK_VERSION_STRING,
                                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                                    THREE_DS_CHALLENGE_CANCELLED_ERROR_CODE,
                                ),
                                "3DS Challenge cancelled.",
                            ),
                        )
                    }

                    override fun timedout() {
                        cancel(
                            ThreeDsChallengeTimedOutException(
                                THREE_DS_CHALLENGE_TIMEOUT_ERROR_CODE,
                                ThreeDsRuntimeFailureContextParams(
                                    threeDsSdkVersion = threeDsSdkVersion,
                                    initProtocolVersion = initProtocolVersion,
                                    threeDsWrapperSdkVersion = BuildConfig.SDK_VERSION_STRING,
                                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                                    errorCode = THREE_DS_CHALLENGE_TIMEOUT_ERROR_CODE,
                                ),
                                "3DS Challenge timed out.",
                            ),
                        )
                    }

                    override fun protocolError(errorEvent: ProtocolErrorEvent) {
                        val errorMessage = errorEvent.errorMessage
                        cancel(
                            ThreeDsProtocolFailedException(
                                errorEvent.errorMessage.errorCode,
                                ThreeDsProtocolFailureContextParams(
                                    errorMessage.errorDetails,
                                    errorMessage.errorDescription,
                                    errorMessage.errorCode,
                                    errorMessage.errorMessageType,
                                    errorMessage.errorComponent,
                                    errorMessage.transactionID,
                                    errorMessage.messageVersionNumber,
                                    threeDsSdkVersion = threeDsSdkVersion,
                                    initProtocolVersion = initProtocolVersion,
                                    threeDsWrapperSdkVersion = BuildConfig.SDK_VERSION_STRING,
                                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                                ),
                                errorMessage.errorDescription,
                            ),
                        )
                    }

                    override fun runtimeError(errorEvent: RuntimeErrorEvent) {
                        cancel(
                            ThreeDsRuntimeFailedException(
                                ThreeDsRuntimeFailureContextParams(
                                    threeDsSdkVersion = threeDsSdkVersion,
                                    initProtocolVersion = initProtocolVersion,
                                    threeDsWrapperSdkVersion = BuildConfig.SDK_VERSION_STRING,
                                    threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                                    errorCode = errorEvent.errorCode,
                                ),
                                errorEvent.errorMessage,
                            ),
                        )
                    }
                },
                CHALLENGE_TIMEOUT_IN_SECONDS,
            )

            awaitClose {}
        }

    override fun performCleanup() = threeDS2Service.cleanup(context)

    @Throws(ThreeDsMissingDirectoryServerException::class)
    private fun directoryServerIdForCard(
        cardNetwork: CardNetwork.Type,
        environment: Environment,
    ) = when (cardNetwork) {
        CardNetwork.Type.VISA -> DsRidValues.VISA
        CardNetwork.Type.AMEX -> DsRidValues.AMEX
        CardNetwork.Type.DINERS_CLUB, CardNetwork.Type.DISCOVER -> DsRidValues.DINERS
        CardNetwork.Type.UNIONPAY -> DsRidValues.UNION
        CardNetwork.Type.JCB -> DsRidValues.JCB
        CardNetwork.Type.MASTERCARD, CardNetwork.Type.MAESTRO -> DsRidValues.MASTERCARD
        else ->
            when (environment == Environment.PRODUCTION) {
                true -> throw ThreeDsMissingDirectoryServerException(
                    cardNetwork,
                    ThreeDsFailureContextParams(
                        threeDsSdkVersion = threeDsSdkVersion,
                        initProtocolVersion = null,
                        threeDsWrapperSdkVersion = BuildConfig.SDK_VERSION_STRING,
                        threeDsSdkProvider = ThreeDsSdkProvider.NETCETERA.name,
                    ),
                )

                false -> TEST_SCHEME_ID
            }
    }

    internal companion object {
        private const val TEST_SCHEME_NAME = "test_schema"
        private const val CHALLENGE_TIMEOUT_IN_SECONDS = 60

        const val TEST_SCHEME_ID = "A999999999"

        const val KEYS_CONFIG_ERROR = "3DS Config threeDsCertificates are missing."
        const val API_KEY_CONFIG_ERROR = "3DS Config apiKey is missing."

        const val THREE_DS_CHALLENGE_CANCELLED_ERROR_CODE = "-4"
        const val THREE_DS_CHALLENGE_TIMEOUT_ERROR_CODE = "-3"
        const val THREE_DS_CHALLENGE_INVALID_STATUS_CODE = "-5"
    }
}
