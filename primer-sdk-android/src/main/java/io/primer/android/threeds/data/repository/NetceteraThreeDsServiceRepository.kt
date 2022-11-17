package io.primer.android.threeds.data.repository

import android.app.Activity
import android.content.Context
import com.netcetera.threeds.sdk.api.ThreeDS2Service
import com.netcetera.threeds.sdk.api.configparameters.builder.ConfigurationBuilder
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration
import com.netcetera.threeds.sdk.api.transaction.Transaction
import com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeParameters
import com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeStatusReceiver
import com.netcetera.threeds.sdk.api.transaction.challenge.events.CompletionEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.ProtocolErrorEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.RuntimeErrorEvent
import com.netcetera.threeds.sdk.api.ui.logic.UiCustomization
import com.netcetera.threeds.sdk.api.utils.DsRidValues
import io.primer.android.R
import io.primer.android.data.configuration.models.Environment
import io.primer.android.threeds.data.exception.ThreeDsConfigurationException
import io.primer.android.threeds.data.exception.ThreeDsFailedException
import io.primer.android.threeds.data.exception.ThreeDsInitException
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.CardNetwork
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.domain.respository.ThreeDsServiceRepository
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.util.Locale
import kotlin.coroutines.coroutineContext

@ExperimentalCoroutinesApi
internal class NetceteraThreeDsServiceRepository(
    private val context: Context,
    private val threeDS2Service: ThreeDS2Service,
) : ThreeDsServiceRepository {

    override suspend fun initializeProvider(
        is3DSSanityCheckEnabled: Boolean,
        locale: Locale,
        threeDsKeysParams: ThreeDsKeysParams?,
    ): Flow<Unit> =
        flow {
            coroutineContext.ensureActive()

            try {
                requireNotNull(threeDsKeysParams) { KEYS_CONFIG_ERROR }
                requireNotNull(threeDsKeysParams.licenceKey) { LICENCE_CONFIG_ERROR }
            } catch (expected: IllegalArgumentException) {
                throw ThreeDsConfigurationException(expected.message)
            }

            val configurationBuilder = ConfigurationBuilder()
                .license(threeDsKeysParams.licenceKey)

            threeDsKeysParams.let { (environment, _, threeDsSecureCertificates) ->
                if (environment != Environment.PRODUCTION) {
                    threeDsSecureCertificates?.forEach { (_, rootCertificate, encryptionKey) ->
                        configurationBuilder.configureScheme(
                            SchemeConfiguration.newSchemeConfiguration(TEST_SCHEME_NAME)
                                .logo(R.drawable.ds_logo_visa.toString())
                                .logoDark(R.drawable.ds_logo_visa.toString())
                                .ids(listOf(TEST_SCHEME_ID))
                                .encryptionPublicKey(encryptionKey)
                                .rootPublicKey(rootCertificate)
                                .build()
                        )
                    }
                }
            }

            threeDS2Service.initialize(
                context,
                configurationBuilder.build(),
                locale.toString(),
                UiCustomization()
            )

            coroutineContext.ensureActive()
            val warnings = threeDS2Service.warnings
            coroutineContext.ensureActive()
            if (is3DSSanityCheckEnabled.not() || warnings.isEmpty()) {
                emit(Unit)
            } else {
                throw ThreeDsInitException(
                    "3DS init failed with warnings: " +
                        warnings.joinToString(",") { "${it.severity}  ${it.message}" }
                )
            }
        }

    override fun performProviderAuth(
        cardNetwork: CardNetwork,
        protocolVersion: ProtocolVersion,
    ): Flow<Transaction> =
        flow {
            emit(
                threeDS2Service.createTransaction(
                    directoryServerIdForCard(cardNetwork),
                    protocolVersion.versionNumber
                )
            )
        }

    override fun performChallenge(
        activity: Activity,
        transaction: Transaction,
        authResponse: BeginAuthResponse,
        threeDSAppURL: String,
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
                                    completionEvent.transactionStatus
                                )
                            )
                        } else cancel(ThreeDsFailedException(message = "3DS challenge failed."))
                        close()
                    }

                    override fun cancelled() {
                        cancel(ThreeDsFailedException(message = "3DS cancelled."))
                    }

                    override fun timedout() {
                        cancel(ThreeDsFailedException(message = "3DS timed out."))
                    }

                    override fun protocolError(errorEvent: ProtocolErrorEvent) {
                        cancel(
                            ThreeDsFailedException(
                                errorEvent.errorMessage.errorCode,
                                errorEvent.errorMessage.errorDetails
                            )
                        )
                    }

                    override fun runtimeError(errorEvent: RuntimeErrorEvent) {
                        cancel(
                            ThreeDsFailedException(
                                errorEvent.errorCode,
                                errorEvent.errorMessage
                            )
                        )
                    }
                },
                CHALLENGE_TIMEOUT_IN_SECONDS
            )

            awaitClose {}
        }

    override fun performCleanup() =
        threeDS2Service.cleanup(context)

    private fun directoryServerIdForCard(cardNetwork: CardNetwork) =
        when (cardNetwork) {
            CardNetwork.VISA -> DsRidValues.VISA
            CardNetwork.AMEX -> DsRidValues.AMEX
            CardNetwork.DINERS_CLUB, CardNetwork.DISCOVER -> DsRidValues.DINERS
            CardNetwork.UNIONPAY -> DsRidValues.UNION
            CardNetwork.JCB -> DsRidValues.JCB
            CardNetwork.MASTERCARD -> DsRidValues.MASTERCARD
            else -> TEST_SCHEME_ID
        }

    private companion object {

        const val TEST_SCHEME_NAME = "test_schema"
        const val TEST_SCHEME_ID = "A999999999"

        const val CHALLENGE_TIMEOUT_IN_SECONDS = 60
        const val KEYS_CONFIG_ERROR = "3DS Config params missing."
        const val LICENCE_CONFIG_ERROR = "3DS Config licence is missing."
    }
}
