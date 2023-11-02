package io.primer.android.threeds.domain

import android.app.Activity
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.analytics.data.repository.AnalyticsDataRepository
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.data.tokenization.models.BinData
import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.data.tokenization.models.TokenType
import io.primer.android.domain.error.CheckoutErrorEventResolver
import io.primer.android.domain.error.ErrorMapperFactory
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.helpers.ResumeEventResolver
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.data.models.common.ResponseCode
import io.primer.android.threeds.data.models.postAuth.PostAuthResponse
import io.primer.android.threeds.domain.interactor.DefaultThreeDsInteractor
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor
import io.primer.android.threeds.domain.models.BaseThreeDsParams
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsAuthParams
import io.primer.android.threeds.domain.models.ThreeDsInitParams
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.domain.respository.ThreeDsAppUrlRepository
import io.primer.android.threeds.domain.respository.ThreeDsConfigurationRepository
import io.primer.android.threeds.domain.respository.ThreeDsRepository
import io.primer.android.threeds.domain.respository.ThreeDsServiceRepository
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class ThreeDsInteractorTest {

    @RelaxedMockK
    internal lateinit var threeDsServiceRepository: ThreeDsServiceRepository

    @RelaxedMockK
    internal lateinit var threeDsRepository: ThreeDsRepository

    @RelaxedMockK
    internal lateinit var threeDsConfigurationRepository: ThreeDsConfigurationRepository

    @RelaxedMockK
    internal lateinit var paymentMethodRepository: PaymentMethodRepository

    @RelaxedMockK
    internal lateinit var clientTokenRepository: ClientTokenRepository

    @RelaxedMockK
    internal lateinit var threeDsAppUrlRepository: ThreeDsAppUrlRepository

    @RelaxedMockK
    internal lateinit var resumeEventResolver: ResumeEventResolver

    @RelaxedMockK
    internal lateinit var errorMapperFactory: ErrorMapperFactory

    @RelaxedMockK
    internal lateinit var checkoutErrorEventResolver: CheckoutErrorEventResolver

    @RelaxedMockK
    internal lateinit var analyticsRepository: AnalyticsDataRepository

    @RelaxedMockK
    internal lateinit var logReporter: LogReporter

    private lateinit var interactor: ThreeDsInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor = DefaultThreeDsInteractor(
            threeDsServiceRepository,
            threeDsRepository,
            paymentMethodRepository,
            clientTokenRepository,
            threeDsAppUrlRepository,
            threeDsConfigurationRepository,
            resumeEventResolver,
            checkoutErrorEventResolver,
            errorMapperFactory,
            analyticsRepository,
            logReporter
        )

        every {
            paymentMethodRepository.getPaymentMethod()
        }.returns(paymentMethodTokenInternal)
    }

    @Test
    fun `initialize() should continue when service repository initialize() and config repository getConfiguration() was success`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)
        val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)

        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            flowOf(keysParams)
        )
        coEvery { threeDsServiceRepository.initializeProvider(any(), any(), any(), any()) }.returns(
            flowOf(Unit)
        )

        runTest {
            interactor.initialize(initParams).first()
        }

        coVerify { threeDsConfigurationRepository.getConfiguration() }
        coVerify { threeDsServiceRepository.initializeProvider(any(), any(), any(), any()) }
    }

    @Test
    fun `initialize() should dispatch error events when repository service initialize() was success and config repository getConfiguration() failed and Intent was 3DS_AUTHENTICATION`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Config failed.")

        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            flow {
                throw exception
            }
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`.name
        )

        val capturedException = assertThrows<Exception> {
            runTest {
                interactor.initialize(initParams).first()
            }
        }

        coVerify { threeDsConfigurationRepository.getConfiguration() }

        assertEquals(exception.javaClass, capturedException.javaClass)
        assertEquals(exception.message, capturedException.message)
    }

    @Test
    fun `initialize() should dispatch error events when service repository initialize() failed and config repository getConfiguration() was success and Intent was 3DS_AUTHENTICATION`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)
        val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("3DS init failed.")

        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            flowOf(keysParams)
        )
        coEvery { threeDsServiceRepository.initializeProvider(any(), any(), any(), any()) }.returns(
            flow {
                throw exception
            }
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`.name
        )

        val capturedException = assertThrows<Exception> {
            runTest {
                interactor.initialize(initParams).first()
            }
        }

        coVerify { threeDsServiceRepository.initializeProvider(any(), any(), any(), any()) }
        coVerify { threeDsConfigurationRepository.getConfiguration() }

        assertEquals(exception.javaClass, capturedException.javaClass)
        assertEquals(exception.message, capturedException.message)
    }

    @Test
    fun `performProviderAuth() should return transaction when repository performProviderAuth() was success`() {
        val transactionMock = mockk<Transaction>(relaxed = true)
        val authParams = mockk<ThreeDsAuthParams>(relaxed = true)
        every { authParams.protocolVersions }.returns(listOf(ProtocolVersion.V_210))

        every { threeDsConfigurationRepository.getPreAuthConfiguration() }.returns(
            flowOf(authParams)
        )
        coEvery { threeDsServiceRepository.performProviderAuth(any(), any(), any()) }.returns(
            flowOf(transactionMock)
        )
        runTest {
            val transaction = interactor.authenticateSdk().first()
            assertEquals(transactionMock, transaction)
        }

        coVerify { threeDsServiceRepository.performProviderAuth(any(), any(), any()) }
    }

    @Test
    fun `performProviderAuth() should dispatch resume error events when repository performProviderAuth() failed and Intent was 3DS_AUTHENTICATION`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to perform provider auth.")

        val authParams = mockk<ThreeDsAuthParams>(relaxed = true)
        every { authParams.protocolVersions }.returns(listOf(ProtocolVersion.V_210))

        every { threeDsConfigurationRepository.getPreAuthConfiguration() }.returns(
            flowOf(authParams)
        )
        coEvery { threeDsServiceRepository.performProviderAuth(any(), any(), any()) }.returns(
            flow {
                throw exception
            }
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`.name
        )

        val capturedException = assertThrows<Exception> {
            runTest {
                interactor.authenticateSdk().first()
            }
        }

        assertEquals(exception.javaClass, capturedException.javaClass)
        assertEquals(exception.message, capturedException.message)
    }

    @Test
    fun `beginRemoteAuth() should dispatch resume events when repository begin3DSAuth() was success and status was not CHALLENGE`() {
        val beginAuthResponse = mockk<BeginAuthResponse>(relaxed = true)
        val threeDsParams = mockk<BaseThreeDsParams>(relaxed = true)

        every { beginAuthResponse.authentication.responseCode } returns ResponseCode.AUTH_SUCCESS
        every { beginAuthResponse.token }.returns(paymentMethodTokenInternal)

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`.name
        )

        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            flowOf(
                beginAuthResponse
            )
        )

        runTest {
            val response = interactor.beginRemoteAuth(threeDsParams).first()
            assertEquals(beginAuthResponse, response)
        }

        val paymentInstrumentType = slot<String>()

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }
        verify { clientTokenRepository.getClientTokenIntent() }
        verify { resumeEventResolver.resolve(capture(paymentInstrumentType), any(), any()) }

        assertEquals(beginAuthResponse.token.paymentInstrumentType, paymentInstrumentType.captured)
    }

    @Test
    fun `beginRemoteAuth() should not dispatch events when repository begin3DSAuth() was success`() {
        val beginAuthResponse = mockk<BeginAuthResponse>(relaxed = true)
        val threeDsParams = mockk<BaseThreeDsParams>(relaxed = true)

        every { beginAuthResponse.authentication.responseCode } returns ResponseCode.CHALLENGE
        every { beginAuthResponse.token }.returns(paymentMethodTokenInternal)

        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            flowOf(
                beginAuthResponse
            )
        )

        runTest {
            val response = interactor.beginRemoteAuth(threeDsParams).first()
            assertEquals(beginAuthResponse, response)
        }

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }
    }

    @Test
    fun `beginRemoteAuth() should dispatch error events when repository begin3DSAuth() failed and Intent was 3DS_AUTHENTICATION`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to execute 3DS auth.")

        val threeDsParams = mockk<BaseThreeDsParams>(relaxed = true)
        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            flow {
                throw exception
            }
        )

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`.name
        )

        val capturedException = assertThrows<Exception> {
            runTest {
                interactor.beginRemoteAuth(threeDsParams).first()
            }
        }

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }

        assertEquals(exception.javaClass, capturedException.javaClass)
        assertEquals(exception.message, capturedException.message)
    }

    @Test
    fun `performChallenge() should log warnings when threeDsAppUrlRepository_getAppUrl() returns invalid URL and protocol version is higher than 2_1_0`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val authResponse = mockk<BeginAuthResponse>(relaxed = true)
        val challengeStatusData = mockk<ChallengeStatusData>()

        every { authResponse.token }.returns(paymentMethodTokenInternal)
        every { threeDsAppUrlRepository.getAppUrl(transaction) }.returns(null)
        every { authResponse.authentication.protocolVersion }.returns(
            ProtocolVersion.V_220.versionNumber
        )

        coEvery {
            threeDsServiceRepository.performChallenge(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }.returns(
            flowOf(
                challengeStatusData
            )
        )

        runTest {
            val statusData =
                interactor.performChallenge(activity, transaction, authResponse).first()
            assertEquals(challengeStatusData, statusData)
        }

        coVerify { threeDsServiceRepository.performChallenge(any(), any(), any(), any(), any()) }
        verify { logReporter.warn(any(), any()) }
    }

    @Test
    fun `performChallenge() should continue when repository performChallenge() was success`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val authResponse = mockk<BeginAuthResponse>(relaxed = true)
        val challengeStatusData = mockk<ChallengeStatusData>()

        every { authResponse.token }.returns(paymentMethodTokenInternal)

        coEvery {
            threeDsServiceRepository.performChallenge(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }.returns(
            flowOf(
                challengeStatusData
            )
        )

        runTest {
            val statusData =
                interactor.performChallenge(activity, transaction, authResponse).first()
            assertEquals(challengeStatusData, statusData)
        }

        coVerify { threeDsServiceRepository.performChallenge(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { logReporter.warn(any()) }
    }

    @Test
    fun `performChallenge() should dispatch error events when repository performChallenge() failed and Intent was 3DS_AUTHENTICATION`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val authResponse = mockk<BeginAuthResponse>(relaxed = true)
        val challengeStatusData = mockk<ChallengeStatusData>()

        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to perform 3DS challenge.")

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`.name
        )

        coEvery {
            threeDsServiceRepository.performChallenge(any(), any(), any(), any(), any())
        }.returns(
            flow {
                throw exception
            }
        )

        val capturedException = assertThrows<Exception> {
            runTest {
                val statusData =
                    interactor.performChallenge(activity, transaction, authResponse).first()
                assertEquals(challengeStatusData, statusData)
            }
        }

        coVerify { threeDsServiceRepository.performChallenge(any(), any(), any(), any(), any()) }

        assertEquals(exception.javaClass, capturedException.javaClass)
        assertEquals(exception.message, capturedException.message)
    }

    @Test
    fun `continueRemoteAuth() should dispatch resume events when repository continue3DSAuth() was success and Intent was 3DS_AUTHENTICATION`() {
        val postAuthResponse = mockk<PostAuthResponse>(relaxed = true)

        val authParams = mockk<ThreeDsAuthParams>(relaxed = true)
        every { authParams.protocolVersions }.returns(listOf(ProtocolVersion.V_210))

        every { threeDsConfigurationRepository.getPreAuthConfiguration() }.returns(
            flowOf(authParams)
        )

        coEvery { threeDsRepository.continue3DSAuth(any(), any()) }.returns(
            flowOf(postAuthResponse)
        )

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`.name
        )

        val paymentInstrumentType = slot<String>()

        runTest {
            val response = interactor.continueRemoteAuth(ChallengeStatusData("", "Y")).first()
            assertEquals(postAuthResponse, response)
        }

        coVerify { threeDsRepository.continue3DSAuth(any(), any()) }
        verify { clientTokenRepository.getClientTokenIntent() }
        verify { resumeEventResolver.resolve(capture(paymentInstrumentType), any(), any()) }

        assertEquals(postAuthResponse.token.paymentInstrumentType, paymentInstrumentType.captured)
    }

    @Test
    fun `continueRemoteAuthWithException() should dispatch resume events when repository continue3DSAuth() was success and Intent was 3DS_AUTHENTICATION`() {
        val postAuthResponse = mockk<PostAuthResponse>(relaxed = true)

        val authParams = mockk<ThreeDsAuthParams>(relaxed = true)
        every { authParams.protocolVersions }.returns(listOf(ProtocolVersion.V_210))

        coEvery { threeDsConfigurationRepository.getPreAuthConfiguration() }.returns(
            flowOf(authParams)
        )

        val threeDsException = mockk<Exception>(relaxed = true)
        coEvery { threeDsRepository.continue3DSAuth(any(), any()) }.returns(
            flowOf(postAuthResponse)
        )

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`.name
        )

        val paymentInstrumentType = slot<String>()

        runTest {
            val response = interactor.continueRemoteAuthWithException(threeDsException).first()
            assertEquals(postAuthResponse, response)
        }

        coVerify { threeDsConfigurationRepository.getPreAuthConfiguration() }
        coVerify { threeDsRepository.continue3DSAuth(any(), any()) }
        verify { clientTokenRepository.getClientTokenIntent() }
        verify { resumeEventResolver.resolve(capture(paymentInstrumentType), any(), any()) }

        assertEquals(postAuthResponse.token.paymentInstrumentType, paymentInstrumentType.captured)
    }

    @Test
    fun `continueRemoteAuth() should dispatch resume error events when repository continue3DSAuth() failed and Intent was 3DS_AUTHENTICATION`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to continue 3DS auth.")

        val authParams = mockk<ThreeDsAuthParams>(relaxed = true)
        every { authParams.protocolVersions }.returns(listOf(ProtocolVersion.V_210))

        coEvery { threeDsConfigurationRepository.getPreAuthConfiguration() }.returns(
            flowOf(authParams)
        )
        coEvery { threeDsRepository.continue3DSAuth(any(), any()) }.returns(
            flow {
                throw exception
            }
        )

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`.name
        )

        val event = slot<Throwable>()

        assertThrows<Exception> {
            runTest {
                interactor.continueRemoteAuth(ChallengeStatusData("", "Y")).first()
            }
        }

        coVerify { threeDsConfigurationRepository.getPreAuthConfiguration() }
        coVerify { threeDsRepository.continue3DSAuth(any(), any()) }
        verify { clientTokenRepository.getClientTokenIntent() }
        verify {
            checkoutErrorEventResolver.resolve(
                capture(event),
                ErrorMapperType.THREE_DS
            )
        }

        assertEquals(exception.javaClass, event.captured.javaClass)
        assertEquals(exception.message, event.captured.message)
    }

    @Test
    fun `continueRemoteAuthWithException() should dispatch resume error events when repository continue3DSAuth() failed and Intent was 3DS_AUTHENTICATION`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to continue 3DS auth.")

        val threeDsException = mockk<Exception>(relaxed = true)

        val authParams = mockk<ThreeDsAuthParams>(relaxed = true)
        every { authParams.protocolVersions }.returns(listOf(ProtocolVersion.V_210))

        coEvery { threeDsConfigurationRepository.getPreAuthConfiguration() }.returns(
            flowOf(authParams)
        )
        coEvery { threeDsRepository.continue3DSAuth(any(), any()) }.returns(
            flow {
                throw exception
            }
        )

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`.name
        )

        val event = slot<Throwable>()

        assertThrows<Exception> {
            runTest {
                interactor.continueRemoteAuthWithException(threeDsException).first()
            }
        }

        coVerify { threeDsConfigurationRepository.getPreAuthConfiguration() }
        coVerify { threeDsRepository.continue3DSAuth(any(), any()) }
        verify { clientTokenRepository.getClientTokenIntent() }
        verify {
            checkoutErrorEventResolver.resolve(
                capture(event),
                ErrorMapperType.THREE_DS
            )
        }

        assertEquals(exception.javaClass, event.captured.javaClass)
        assertEquals(exception.message, event.captured.message)
    }

    @Test
    fun `cleanup() should call repository performCleanup()`() {
        every { threeDsServiceRepository.performCleanup() }.returns(Unit)

        runTest {
            interactor.cleanup()
        }

        coVerify { threeDsServiceRepository.performCleanup() }
    }

    private companion object {

        private val paymentMethodTokenInternal =
            PaymentMethodTokenInternal(
                token = UUID.randomUUID().toString(),
                analyticsId = UUID.randomUUID().toString(),
                tokenType = TokenType.MULTI_USE,
                paymentInstrumentType = "PAYMENT_CARD",
                vaultData = null,
                threeDSecureAuthentication = null,
                paymentInstrumentData = PaymentInstrumentData(
                    network = "VISA",
                    binData = BinData("VISA")
                ),
                isVaulted = false
            )
    }
}
