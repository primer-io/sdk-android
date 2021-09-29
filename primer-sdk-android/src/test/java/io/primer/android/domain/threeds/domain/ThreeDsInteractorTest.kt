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
import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.DefaultLogger
import io.primer.android.model.dto.BinData
import io.primer.android.model.dto.PaymentInstrumentData
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.model.dto.TokenType
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.PostAuthResponse
import io.primer.android.threeds.data.models.ResponseCode
import io.primer.android.threeds.domain.interactor.DefaultThreeDsInteractor
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsConfigParams
import io.primer.android.threeds.domain.models.ThreeDsInitParams
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.domain.models.ThreeDsParams
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import io.primer.android.threeds.domain.respository.ThreeDsAppUrlRepository
import io.primer.android.threeds.domain.respository.ThreeDsConfigurationRepository
import io.primer.android.threeds.domain.respository.ThreeDsRepository
import io.primer.android.threeds.domain.respository.ThreeDsServiceRepository
import io.primer.android.threeds.domain.validation.ThreeDsConfigValidator
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.Exception

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
    internal lateinit var threeDsConfigValidator: ThreeDsConfigValidator

    @RelaxedMockK
    internal lateinit var resumeHandlerFactory: ResumeHandlerFactory

    @RelaxedMockK
    internal lateinit var logger: DefaultLogger

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

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
                threeDsConfigValidator,
                resumeHandlerFactory,
                eventDispatcher,
                logger,
                testCoroutineDispatcher
            )

        every {
            paymentMethodRepository.getPaymentMethod()
        }.returns(paymentMethodTokenInternal)
    }

    @Test
    fun `validate() should continue when config validation was success`() {
        val configParams = mockk<ThreeDsConfigParams>(relaxed = true)
        coEvery { threeDsConfigValidator.validate(any()) }.returns(flowOf(Unit))
        runBlockingTest {
            interactor.validate(configParams).first()
        }
        coVerify { threeDsConfigValidator.validate(any()) }
    }

    @Test
    fun `validate() should dispatch tokenize error events when validation failed and Intent was CHECKOUT`() {
        val configParams = mockk<ThreeDsConfigParams>(relaxed = true)
        coEvery { threeDsConfigValidator.validate(any()) }.returns(
            flow {
                throw Exception("Validation failed.")
            }
        )

        val events = slot<List<CheckoutEvent>>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.validate(configParams).first()
            }
        }

        coVerify { threeDsConfigValidator.validate(any()) }
        verify { eventDispatcher.dispatchEvents(capture(events)) }

        assert(events.captured.first().type == CheckoutEventType.TOKENIZE_SUCCESS)
        assert(events.captured[1].type == CheckoutEventType.TOKEN_ADDED_TO_VAULT)
    }

    @Test
    fun `validate() should dispatch resume error events when validation failed and Intent was 3DS_AUTHENTICATION`() {
        val configParams = mockk<ThreeDsConfigParams>(relaxed = true)
        coEvery { threeDsConfigValidator.validate(any()) }.returns(
            flow {
                throw Exception("Validation failed.")
            }
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        val event = slot<CheckoutEvent>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.validate(configParams).first()
            }
        }

        coVerify { threeDsConfigValidator.validate(any()) }
        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.RESUME_ERR0R)
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
    fun `initialize() should dispatch tokenize error events when repository service initialize() was success and config repository getConfiguration() failed and Intent was CHECKOUT`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)

        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            flow {
                throw Exception("Config missing.")
            }
        )

        val events = slot<List<CheckoutEvent>>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.initialize(initParams).first()
            }
        }

        coVerify { threeDsConfigurationRepository.getConfiguration() }
        verify { eventDispatcher.dispatchEvents(capture(events)) }

        assert(events.captured.first().type == CheckoutEventType.TOKENIZE_SUCCESS)
        assert(events.captured[1].type == CheckoutEventType.TOKEN_ADDED_TO_VAULT)
    }

    @Test
    fun `initialize() should dispatch resume error events when repository service initialize() was success and config repository getConfiguration() failed and Intent was 3DS_AUTHENTICATION`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)

        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            flow {
                throw Exception("Config missing.")
            }
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        val event = slot<CheckoutEvent>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.initialize(initParams).first()
            }
        }

        coVerify { threeDsConfigurationRepository.getConfiguration() }
        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.RESUME_ERR0R)
    }

    @Test
    fun `initialize() should dispatch tokenize error events when service repository initialize() failed and config repository getConfiguration() was success and Intent was CHECKOUT`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)
        val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)

        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            flowOf(keysParams)
        )
        coEvery { threeDsServiceRepository.initializeProvider(any(), any(), any()) }.returns(
            flow {
                throw Exception("3DS init failed.")
            }
        )

        val events = slot<List<CheckoutEvent>>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.initialize(initParams).first()
            }
        }

        coVerify { threeDsServiceRepository.initializeProvider(any(), any(), any()) }
        coVerify { threeDsConfigurationRepository.getConfiguration() }
        verify { eventDispatcher.dispatchEvents(capture(events)) }

        assert(events.captured.first().type == CheckoutEventType.TOKENIZE_SUCCESS)
        assert(events.captured[1].type == CheckoutEventType.TOKEN_ADDED_TO_VAULT)
    }

    @Test
    fun `initialize() should dispatch resume error events when service repository initialize() failed and config repository getConfiguration() was success and Intent was 3DS_AUTHENTICATION`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)
        val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)

        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            flowOf(keysParams)
        )
        coEvery { threeDsServiceRepository.initializeProvider(any(), any(), any()) }.returns(
            flow {
                throw Exception("3DS init failed.")
            }
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        val event = slot<CheckoutEvent>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.initialize(initParams).first()
            }
        }

        coVerify { threeDsServiceRepository.initializeProvider(any(), any(), any()) }
        coVerify { threeDsConfigurationRepository.getConfiguration() }
        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.RESUME_ERR0R)
    }

    @Test
    fun `performProviderAuth() should return transaction when repository performProviderAuth() was success`() {
        val transactionMock = mockk<Transaction>(relaxed = true)
        coEvery { threeDsServiceRepository.performProviderAuth(any(), any()) }.returns(
            flowOf(transactionMock)
        )
        runBlockingTest {
            val transaction = interactor.authenticateSdk(ProtocolVersion.V_210).first()
            assertEquals(transactionMock, transaction)
        }
        coVerify { threeDsServiceRepository.performProviderAuth(any(), any()) }
    }

    @Test
    fun `performProviderAuth() should dispatch token error events when repository performProviderAuth() failed and Intent was CHECKOUT`() {
        coEvery { threeDsServiceRepository.performProviderAuth(any(), any()) }.returns(
            flow {
                throw Exception("3DS init failed.")
            }
        )

        val events = slot<List<CheckoutEvent>>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.authenticateSdk(ProtocolVersion.V_210).first()
            }
        }

        coVerify { threeDsServiceRepository.performProviderAuth(any(), any()) }
        verify { eventDispatcher.dispatchEvents(capture(events)) }

        assert(events.captured.first().type == CheckoutEventType.TOKENIZE_SUCCESS)
        assert(events.captured[1].type == CheckoutEventType.TOKEN_ADDED_TO_VAULT)
    }

    @Test
    fun `performProviderAuth() should dispatch resume error events when repository performProviderAuth() failed and Intent was 3DS_AUTHENTICATION`() {
        coEvery { threeDsServiceRepository.performProviderAuth(any(), any()) }.returns(
            flow {
                throw Exception("3DS init failed.")
            }
        )
        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        val events = slot<CheckoutEvent>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.authenticateSdk(ProtocolVersion.V_210).first()
            }
        }

        coVerify { threeDsServiceRepository.performProviderAuth(any(), any()) }
        verify { eventDispatcher.dispatchEvent(capture(events)) }

        assert(events.captured.type == CheckoutEventType.RESUME_ERR0R)
    }

    @Test
    fun `beginRemoteAuth() should dispatch token events when repository begin3DSAuth() was success and response code is not CHALLENGE`() {
        val beginAuthResponse = mockk<BeginAuthResponse>(relaxed = true)
        val authDetails = mockk<PaymentMethodTokenInternal.AuthenticationDetails>(relaxed = true)
        val threeDsParams = mockk<ThreeDsParams>(relaxed = true)

        every { beginAuthResponse.token.threeDSecureAuthentication }.returns(authDetails)
        every { beginAuthResponse.token }.returns(paymentMethodTokenInternal)
        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            flowOf(
                beginAuthResponse
            )
        )

        val events = slot<List<CheckoutEvent>>()

        runBlockingTest {
            val response =
                interactor.beginRemoteAuth(threeDsParams).flowOn(testCoroutineDispatcher).first()
            assertEquals(beginAuthResponse, response)
        }

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }
        verify { eventDispatcher.dispatchEvents(capture(events)) }

        assert(events.captured.first().type == CheckoutEventType.TOKENIZE_SUCCESS)
        assert(events.captured[1].type == CheckoutEventType.TOKEN_ADDED_TO_VAULT)
    }

    @Test
    fun `beginRemoteAuth() should not dispatch events when repository begin3DSAuth() was success`() {
        val beginAuthResponse = mockk<BeginAuthResponse>(relaxed = true)
        val threeDsParams = mockk<ThreeDsParams>(relaxed = true)

        every { beginAuthResponse.authentication.responseCode } returns (ResponseCode.CHALLENGE)
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
    fun `beginRemoteAuth() should dispatch token error events when repository begin3DSAuth() failed and Intent was CHECKOUT`() {
        val threeDsParams = mockk<ThreeDsParams>(relaxed = true)
        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            flow {
                throw Exception()
            }
        )

        val events = slot<List<CheckoutEvent>>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.beginRemoteAuth(threeDsParams).first()
            }
        }

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }
        verify { eventDispatcher.dispatchEvents(capture(events)) }

        assert(events.captured.first().type == CheckoutEventType.TOKENIZE_SUCCESS)
        assert(events.captured[1].type == CheckoutEventType.TOKEN_ADDED_TO_VAULT)
    }

    @Test
    fun `beginRemoteAuth() should dispatch token error events when repository begin3DSAuth() failed and Intent was 3DS_AUTHENTICATION`() {
        val threeDsParams = mockk<ThreeDsParams>(relaxed = true)
        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            flow {
                throw Exception()
            }
        )

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        val events = slot<CheckoutEvent>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.beginRemoteAuth(threeDsParams).first()
            }
        }

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }
        verify { eventDispatcher.dispatchEvent(capture(events)) }

        assert(events.captured.type == CheckoutEventType.RESUME_ERR0R)
    }

    @Test
    fun `beginRemoteAuth() should dispatch error events with responseCode SKIPPED when repository begin3DSAuth() failed and Intent was CHECKOUT`() {
        val threeDsParams = mockk<ThreeDsParams>(relaxed = true)
        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            flow {
                throw Exception()
            }
        )
        val events = slot<List<CheckoutEvent>>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.beginRemoteAuth(threeDsParams).first()
            }
        }

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }
        verify { eventDispatcher.dispatchEvents(capture(events)) }

        assert(events.captured.first() is CheckoutEvent.TokenizationSuccess)
        assert(
            (events.captured.first() as CheckoutEvent.TokenizationSuccess)
                .data.threeDSecureAuthentication?.responseCode == ResponseCode.SKIPPED
        )

        assert(events.captured[1] is CheckoutEvent.TokenAddedToVault)
        assert(
            (events.captured[1] as CheckoutEvent.TokenAddedToVault)
                .data.threeDSecureAuthentication?.responseCode == ResponseCode.SKIPPED
        )
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
    fun `performChallenge() should dispatch token error events with responseCode SKIPPED when repository performChallenge() failed and Intent was CHECKOUT`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val authResponse = mockk<BeginAuthResponse>(relaxed = true)
        val challengeStatusData = mockk<ChallengeStatusData>()

        every { authResponse.token }.returns(paymentMethodTokenInternal)

        coEvery {
            threeDsServiceRepository.performChallenge(any(), any(), any(), any())
        }.returns(
            flow {
                throw Exception()
            }
        )

        val events = slot<List<CheckoutEvent>>()

        assertThrows<Exception> {
            runBlockingTest {
                val statusData =
                    interactor.performChallenge(activity, transaction, authResponse)
                        .flowOn(testCoroutineDispatcher).first()
                assertEquals(challengeStatusData, statusData)
            }
        }

        coVerify { threeDsServiceRepository.performChallenge(any(), any(), any(), any()) }
        verify { eventDispatcher.dispatchEvents(capture(events)) }

        assert(events.captured.first().type == CheckoutEventType.TOKENIZE_SUCCESS)
        assert(events.captured[1].type == CheckoutEventType.TOKEN_ADDED_TO_VAULT)
    }

    @Test
    fun `performChallenge() should dispatch resume error events when repository performChallenge() failed and Intent was 3DS_AUTHENTICATION`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val authResponse = mockk<BeginAuthResponse>(relaxed = true)
        val challengeStatusData = mockk<ChallengeStatusData>()

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        coEvery {
            threeDsServiceRepository.performChallenge(any(), any(), any(), any())
        }.returns(
            flow {
                throw Exception()
            }
        )

        val events = slot<CheckoutEvent>()

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
        verify { eventDispatcher.dispatchEvent(capture(events)) }

        assert(events.captured.type == CheckoutEventType.RESUME_ERR0R)
    }

    @Test
    fun `continueRemoteAuth() should dispatch token events when repository continue3DSAuth() was success and Intent was CHECKOUT`() {
        val postAuthResponse = mockk<PostAuthResponse>(relaxed = true)

        every { postAuthResponse.token }.returns(paymentMethodTokenInternal)

        coEvery { threeDsRepository.continue3DSAuth(any()) }.returns(
            flowOf(postAuthResponse)
        )

        val events = slot<List<CheckoutEvent>>()

        runBlockingTest {
            val response = interactor.continueRemoteAuth("").flowOn(testCoroutineDispatcher).first()
            assertEquals(postAuthResponse, response)
        }

        coVerify { threeDsRepository.continue3DSAuth(any()) }
        verify { eventDispatcher.dispatchEvents(capture(events)) }

        assert(events.captured.first().type == CheckoutEventType.TOKENIZE_SUCCESS)
        assert(events.captured[1].type == CheckoutEventType.TOKEN_ADDED_TO_VAULT)
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

        val events = slot<CheckoutEvent>()

        runBlockingTest {
            val response = interactor.continueRemoteAuth("").flowOn(testCoroutineDispatcher).first()
            assertEquals(postAuthResponse, response)
        }

        coVerify { threeDsRepository.continue3DSAuth(any()) }
        verify { clientTokenRepository.getClientTokenIntent() }
        verify { eventDispatcher.dispatchEvent(capture(events)) }

        assert(events.captured.type == CheckoutEventType.RESUME_SUCCESS)
    }

    @Test
    fun `continueRemoteAuth() should dispatch token error events when repository continue3DSAuth() failed and Intent was CHECKOUT`() {
        coEvery { threeDsRepository.continue3DSAuth(any()) }.returns(
            flow {
                throw Exception()
            }
        )

        val events = slot<List<CheckoutEvent>>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.continueRemoteAuth("").first()
            }
        }

        coVerify { threeDsRepository.continue3DSAuth(any()) }
        verify { clientTokenRepository.getClientTokenIntent() }
        verify { eventDispatcher.dispatchEvents(capture(events)) }

        assert(events.captured.first().type == CheckoutEventType.TOKENIZE_SUCCESS)
        assert(events.captured[1].type == CheckoutEventType.TOKEN_ADDED_TO_VAULT)
    }

    @Test
    fun `continueRemoteAuth() should dispatch resume error events when repository continue3DSAuth() failed and Intent was 3DS_AUTHENTICATION`() {
        coEvery { threeDsRepository.continue3DSAuth(any()) }.returns(
            flow {
                throw Exception()
            }
        )

        every { clientTokenRepository.getClientTokenIntent() }.returns(
            ClientTokenIntent.`3DS_AUTHENTICATION`
        )

        val events = slot<CheckoutEvent>()

        assertThrows<Exception> {
            runBlockingTest {
                interactor.continueRemoteAuth("").first()
            }
        }

        coVerify { threeDsRepository.continue3DSAuth(any()) }
        verify { clientTokenRepository.getClientTokenIntent() }
        verify { eventDispatcher.dispatchEvent(capture(events)) }

        assert(events.captured.type == CheckoutEventType.RESUME_ERR0R)
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
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                TokenType.MULTI_USE,
                "PAYMENT_CARD",
                PaymentInstrumentData(network = "VISA", binData = BinData("VISA"))
            )
    }
}
