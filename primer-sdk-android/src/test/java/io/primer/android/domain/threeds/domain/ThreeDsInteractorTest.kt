package io.primer.android.domain.threeds.domain

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
import io.primer.android.data.base.models.BasePaymentToken
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.error.CheckoutErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.helpers.ResumeEventResolver
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.domain.tokenization.helpers.PostTokenizationEventResolver
import io.primer.android.logging.DefaultLogger
import io.primer.android.model.dto.BinData
import io.primer.android.model.dto.PaymentInstrumentData
import io.primer.android.model.dto.TokenType
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.PostAuthResponse
import io.primer.android.threeds.data.models.ResponseCode
import io.primer.android.threeds.domain.interactor.DefaultThreeDsInteractor
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsInitParams
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.domain.models.ThreeDsParams
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
    internal lateinit var postTokenizationEventResolver: PostTokenizationEventResolver

    @RelaxedMockK
    internal lateinit var resumeEventResolver: ResumeEventResolver

    @RelaxedMockK
    internal lateinit var checkoutErrorEventResolver: CheckoutErrorEventResolver

    @RelaxedMockK
    internal lateinit var logger: DefaultLogger

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var interactor: ThreeDsInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor =
            DefaultThreeDsInteractor(
                threeDsServiceRepository,
                threeDsRepository,
                paymentMethodRepository,
                clientTokenRepository,
                threeDsAppUrlRepository,
                threeDsConfigurationRepository,
                postTokenizationEventResolver,
                resumeEventResolver,
                checkoutErrorEventResolver,
                logger,
                testCoroutineDispatcher
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
        coEvery { threeDsServiceRepository.initializeProvider(any(), any(), any()) }.returns(
            flowOf(Unit)
        )
        runBlockingTest {
            interactor.initialize(initParams).first()
        }

        coVerify { threeDsConfigurationRepository.getConfiguration() }
        coVerify { threeDsServiceRepository.initializeProvider(any(), any(), any()) }
    }

    @Test
    fun `initialize() should dispatch token with error event when repository service initialize() was success and config repository getConfiguration() failed and Intent was CHECKOUT`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            flow {
                throw exception
            }
        )

        val token = slot<PaymentMethodTokenInternal>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.initialize(initParams).first()
            }
        }

        coVerify { threeDsConfigurationRepository.getConfiguration() }
        verify { postTokenizationEventResolver.resolve(capture(token)) }

        assertNotNull(token.captured.threeDSecureAuthentication)
        assertNotNull("CLIENT_ERROR", token.captured.threeDSecureAuthentication?.reasonCode)
        assertNotNull(exception.message, token.captured.threeDSecureAuthentication?.reasonText)
    }

    @Test
    fun `initialize() should dispatch resume error events when repository service initialize() was success and config repository getConfiguration() failed and Intent was 3DS_AUTHENTICATION`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Config failed.")

        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            flow {
                throw exception
            }
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        val event = slot<Throwable>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.initialize(initParams).first()
            }
        }

        coVerify { threeDsConfigurationRepository.getConfiguration() }
        verify {
            checkoutErrorEventResolver.resolve(
                capture(event),
                ErrorMapperType.PAYMENT_RESUME
            )
        }

        assertEquals(exception.javaClass, event.captured.javaClass)
        assertEquals(exception.message, event.captured.message)
    }

    @Test
    fun `initialize() should dispatch token with error event when service repository initialize() failed and config repository getConfiguration() was success and Intent was CHECKOUT`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)
        val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("3DS init failed.")

        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            flowOf(keysParams)
        )
        coEvery { threeDsServiceRepository.initializeProvider(any(), any(), any()) }.returns(
            flow {
                throw exception
            }
        )

        val token = slot<PaymentMethodTokenInternal>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.initialize(initParams).first()
            }
        }

        coVerify { threeDsServiceRepository.initializeProvider(any(), any(), any()) }
        coVerify { threeDsConfigurationRepository.getConfiguration() }
        verify { postTokenizationEventResolver.resolve(capture(token)) }

        assertNotNull(token.captured.threeDSecureAuthentication)
        assertNotNull("CLIENT_ERROR", token.captured.threeDSecureAuthentication?.reasonCode)
        assertNotNull(exception.message, token.captured.threeDSecureAuthentication?.reasonText)
    }

    @Test
    fun `initialize() should dispatch resume error events when service repository initialize() failed and config repository getConfiguration() was success and Intent was 3DS_AUTHENTICATION`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)
        val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("3DS init failed.")

        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            flowOf(keysParams)
        )
        coEvery { threeDsServiceRepository.initializeProvider(any(), any(), any()) }.returns(
            flow {
                throw exception
            }
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        val event = slot<Throwable>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.initialize(initParams).first()
            }
        }

        coVerify { threeDsServiceRepository.initializeProvider(any(), any(), any()) }
        coVerify { threeDsConfigurationRepository.getConfiguration() }
        verify {
            checkoutErrorEventResolver.resolve(
                capture(event),
                ErrorMapperType.PAYMENT_RESUME
            )
        }

        assertEquals(exception.javaClass, event.captured.javaClass)
        assertEquals(exception.message, event.captured.message)
    }

    @Test
    fun `performProviderAuth() should return transaction when repository performProviderAuth() was success`() {
        val transactionMock = mockk<Transaction>(relaxed = true)
        every { threeDsConfigurationRepository.getProtocolVersion() }.returns(
            flowOf(ProtocolVersion.V_210)
        )
        coEvery { threeDsServiceRepository.performProviderAuth(any(), any()) }.returns(
            flowOf(transactionMock)
        )
        runBlockingTest {
            val transaction = interactor.authenticateSdk().first()
            assertEquals(transactionMock, transaction)
        }
        coVerify { threeDsServiceRepository.performProviderAuth(any(), any()) }
    }

    @Test
    fun `performProviderAuth() should dispatch token with error event when repository performProviderAuth() failed and Intent was CHECKOUT`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to perform provider auth.")

        every { threeDsConfigurationRepository.getProtocolVersion() }.returns(
            flowOf(ProtocolVersion.V_210)
        )
        coEvery { threeDsServiceRepository.performProviderAuth(any(), any()) }.returns(
            flow {
                throw exception
            }
        )

        val token = slot<PaymentMethodTokenInternal>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.authenticateSdk().first()
            }
        }

        coVerify { threeDsServiceRepository.performProviderAuth(any(), any()) }
        verify { postTokenizationEventResolver.resolve(capture(token)) }

        assertNotNull(token.captured.threeDSecureAuthentication)
        assertNotNull("CLIENT_ERROR", token.captured.threeDSecureAuthentication?.reasonCode)
        assertNotNull(exception.message, token.captured.threeDSecureAuthentication?.reasonText)
    }

    @Test
    fun `performProviderAuth() should dispatch resume error events when repository performProviderAuth() failed and Intent was 3DS_AUTHENTICATION`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to perform provider auth.")

        every { threeDsConfigurationRepository.getProtocolVersion() }.returns(
            flowOf(ProtocolVersion.V_210)
        )
        coEvery { threeDsServiceRepository.performProviderAuth(any(), any()) }.returns(
            flow {
                throw exception
            }
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        val event = slot<Throwable>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.authenticateSdk().first()
            }
        }

        verify {
            checkoutErrorEventResolver.resolve(
                capture(event),
                ErrorMapperType.PAYMENT_RESUME
            )
        }

        assertEquals(exception.javaClass, event.captured.javaClass)
        assertEquals(exception.message, event.captured.message)
    }

    @Test
    fun `beginRemoteAuth() should dispatch token event when repository begin3DSAuth() was success and response code is not CHALLENGE`() {
        val beginAuthResponse = mockk<BeginAuthResponse>(relaxed = true)
        val authDetails = mockk<BasePaymentToken.AuthenticationDetails>(relaxed = true)
        val threeDsParams = mockk<ThreeDsParams>(relaxed = true)

        every { beginAuthResponse.token.threeDSecureAuthentication }.returns(authDetails)
        every { beginAuthResponse.token }.returns(paymentMethodTokenInternal)
        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            flowOf(
                beginAuthResponse
            )
        )

        val token = slot<PaymentMethodTokenInternal>()

        runBlockingTest {
            val response =
                interactor.beginRemoteAuth(threeDsParams).flowOn(testCoroutineDispatcher).first()
            assertEquals(beginAuthResponse, response)
        }

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }
        verify { postTokenizationEventResolver.resolve(capture(token)) }

        assertEquals(beginAuthResponse.token, token.captured)
    }

    @Test
    fun `beginRemoteAuth() should not dispatch events when repository begin3DSAuth() was success`() {
        val beginAuthResponse = mockk<BeginAuthResponse>(relaxed = true)
        val threeDsParams = mockk<ThreeDsParams>(relaxed = true)

        every { beginAuthResponse.authentication.responseCode } returns ResponseCode.CHALLENGE
        every { beginAuthResponse.token }.returns(paymentMethodTokenInternal)

        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            flowOf(
                beginAuthResponse
            )
        )
        runBlockingTest {
            val response = interactor.beginRemoteAuth(threeDsParams).first()
            assertEquals(beginAuthResponse, response)
        }

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }
    }

    @Test
    fun `beginRemoteAuth() should dispatch token with error event when repository begin3DSAuth() failed and Intent was CHECKOUT`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to execute 3DS auth.")

        val threeDsParams = mockk<ThreeDsParams>(relaxed = true)
        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            flow {
                throw Exception()
            }
        )

        val token = slot<PaymentMethodTokenInternal>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.beginRemoteAuth(threeDsParams).first()
            }
        }

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }
        verify { postTokenizationEventResolver.resolve(capture(token)) }

        assertNotNull(token.captured.threeDSecureAuthentication)
        assertNotNull("CLIENT_ERROR", token.captured.threeDSecureAuthentication?.reasonCode)
        assertNotNull(exception.message, token.captured.threeDSecureAuthentication?.reasonText)
    }

    @Test
    fun `beginRemoteAuth() should dispatch resume error events when repository begin3DSAuth() failed and Intent was 3DS_AUTHENTICATION`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to execute 3DS auth.")

        val threeDsParams = mockk<ThreeDsParams>(relaxed = true)
        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            flow {
                throw exception
            }
        )

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        val event = slot<Throwable>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.beginRemoteAuth(threeDsParams).first()
            }
        }

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }
        verify {
            checkoutErrorEventResolver.resolve(
                capture(event),
                ErrorMapperType.PAYMENT_RESUME
            )
        }

        assertEquals(exception.javaClass, event.captured.javaClass)
        assertEquals(exception.message, event.captured.message)
    }

    @Test
    fun `beginRemoteAuth() should dispatch token error event with responseCode SKIPPED when repository begin3DSAuth() failed and Intent was CHECKOUT`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to execute 3DS auth.")

        val threeDsParams = mockk<ThreeDsParams>(relaxed = true)
        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            flow {
                throw exception
            }
        )
        val token = slot<PaymentMethodTokenInternal>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.beginRemoteAuth(threeDsParams).first()
            }
        }

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }

        verify { postTokenizationEventResolver.resolve(capture(token)) }

        assertEquals(ResponseCode.SKIPPED, token.captured.threeDSecureAuthentication?.responseCode)
    }

    @Test
    fun `performChallenge() should continue when repository performChallenge() was success`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val authResponse = mockk<BeginAuthResponse>(relaxed = true)
        val challengeStatusData = mockk<ChallengeStatusData>()

        every { authResponse.token }.returns(paymentMethodTokenInternal)

        coEvery { threeDsServiceRepository.performChallenge(any(), any(), any(), any()) }.returns(
            flowOf(
                challengeStatusData
            ).flowOn(testCoroutineDispatcher)
        )

        runBlockingTest {
            val statusData =
                interactor.performChallenge(activity, transaction, authResponse)
                    .flowOn(testCoroutineDispatcher).first()
            assertEquals(challengeStatusData, statusData)
        }
        coVerify { threeDsServiceRepository.performChallenge(any(), any(), any(), any()) }
    }

    @Test
    fun `performChallenge() should dispatch token error event with responseCode SKIPPED when repository performChallenge() failed and Intent was CHECKOUT`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val authResponse = mockk<BeginAuthResponse>(relaxed = true)
        val challengeStatusData = mockk<ChallengeStatusData>()

        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to perform 3DS challenge.")

        every { authResponse.token }.returns(paymentMethodTokenInternal)

        coEvery {
            threeDsServiceRepository.performChallenge(any(), any(), any(), any())
        }.returns(
            flow {
                throw exception
            }
        )

        val token = slot<PaymentMethodTokenInternal>()

        assertThrows<Exception> {
            runBlockingTest {
                val statusData =
                    interactor.performChallenge(activity, transaction, authResponse)
                        .flowOn(testCoroutineDispatcher).first()
                assertEquals(challengeStatusData, statusData)
            }
        }

        coVerify { threeDsServiceRepository.performChallenge(any(), any(), any(), any()) }
        verify { postTokenizationEventResolver.resolve(capture(token)) }

        assertEquals(ResponseCode.SKIPPED, token.captured.threeDSecureAuthentication?.responseCode)
    }

    @Test
    fun `performChallenge() should dispatch resume error events when repository performChallenge() failed and Intent was 3DS_AUTHENTICATION`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val authResponse = mockk<BeginAuthResponse>(relaxed = true)
        val challengeStatusData = mockk<ChallengeStatusData>()

        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to perform 3DS challenge.")

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        coEvery {
            threeDsServiceRepository.performChallenge(any(), any(), any(), any())
        }.returns(
            flow {
                throw exception
            }
        )

        val event = slot<Throwable>()

        assertThrows<Exception> {
            runBlockingTest {
                val statusData =
                    interactor.performChallenge(activity, transaction, authResponse)
                        .flowOn(testCoroutineDispatcher).first()
                assertEquals(challengeStatusData, statusData)
            }
        }

        coVerify { threeDsServiceRepository.performChallenge(any(), any(), any(), any()) }
        verify { clientTokenRepository.getClientTokenIntent() }
        verify {
            checkoutErrorEventResolver.resolve(
                capture(event),
                ErrorMapperType.PAYMENT_RESUME
            )
        }

        assertEquals(exception.javaClass, event.captured.javaClass)
        assertEquals(exception.message, event.captured.message)
    }

    @Test
    fun `continueRemoteAuth() should dispatch token event when repository continue3DSAuth() was success and Intent was CHECKOUT`() {
        val postAuthResponse = mockk<PostAuthResponse>(relaxed = true)

        every { postAuthResponse.token }.returns(paymentMethodTokenInternal)

        coEvery { threeDsRepository.continue3DSAuth(any()) }.returns(
            flowOf(postAuthResponse)
        )

        val token = slot<PaymentMethodTokenInternal>()

        runBlockingTest {
            val response = interactor.continueRemoteAuth("").flowOn(testCoroutineDispatcher).first()
            assertEquals(postAuthResponse, response)
        }

        coVerify { threeDsRepository.continue3DSAuth(any()) }

        verify { postTokenizationEventResolver.resolve(capture(token)) }

        assertEquals(postAuthResponse.token, token.captured)
    }

    @Test
    fun `continueRemoteAuth() should dispatch resume events when repository continue3DSAuth() was success and Intent was 3DS_AUTHENTICATION`() {
        val postAuthResponse = mockk<PostAuthResponse>(relaxed = true)

        coEvery { threeDsRepository.continue3DSAuth(any()) }.returns(
            flowOf(postAuthResponse)
        )

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        val paymentInstrumentType = slot<String>()

        runBlockingTest {
            val response = interactor.continueRemoteAuth("").flowOn(testCoroutineDispatcher).first()
            assertEquals(postAuthResponse, response)
        }

        coVerify { threeDsRepository.continue3DSAuth(any()) }
        verify { clientTokenRepository.getClientTokenIntent() }
        verify { resumeEventResolver.resolve(capture(paymentInstrumentType), "") }

        assertEquals(postAuthResponse.token.paymentInstrumentType, paymentInstrumentType.captured)
    }

    @Test
    fun `continueRemoteAuth() should dispatch token error event when repository continue3DSAuth() failed and Intent was CHECKOUT`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to continue 3DS auth.")

        coEvery { threeDsRepository.continue3DSAuth(any()) }.returns(
            flow {
                throw exception
            }
        )

        val token = slot<PaymentMethodTokenInternal>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.continueRemoteAuth("").first()
            }
        }

        coVerify { threeDsRepository.continue3DSAuth(any()) }
        verify { clientTokenRepository.getClientTokenIntent() }
        verify { postTokenizationEventResolver.resolve(capture(token)) }

        assertNotNull(token.captured.threeDSecureAuthentication)
        assertNotNull("CLIENT_ERROR", token.captured.threeDSecureAuthentication?.reasonCode)
        assertNotNull(exception.message, token.captured.threeDSecureAuthentication?.reasonText)
    }

    @Test
    fun `continueRemoteAuth() should dispatch resume error events when repository continue3DSAuth() failed and Intent was 3DS_AUTHENTICATION`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to continue 3DS auth.")
        coEvery { threeDsRepository.continue3DSAuth(any()) }.returns(
            flow {
                throw exception
            }
        )

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        val event = slot<Throwable>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.continueRemoteAuth("").first()
            }
        }

        coVerify { threeDsRepository.continue3DSAuth(any()) }
        verify { clientTokenRepository.getClientTokenIntent() }
        verify {
            checkoutErrorEventResolver.resolve(
                capture(event),
                ErrorMapperType.PAYMENT_RESUME
            )
        }

        assertEquals(exception.javaClass, event.captured.javaClass)
        assertEquals(exception.message, event.captured.message)
    }

    @Test
    fun `cleanup() should call repository performCleanup()`() {
        every { threeDsServiceRepository.performCleanup() }.returns(Unit)

        runBlockingTest {
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
                paymentInstrumentData = PaymentInstrumentData(
                    network = "VISA",
                    binData = BinData("VISA")
                ),
                isVaulted = false
            )
    }
}
