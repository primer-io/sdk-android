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
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
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
    internal lateinit var threeDsAppUrlRepository: ThreeDsAppUrlRepository

    @RelaxedMockK
    internal lateinit var threeDsConfigValidator: ThreeDsConfigValidator

    @RelaxedMockK
    internal lateinit var logger: Logger

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
                threeDsAppUrlRepository,
                threeDsConfigurationRepository,
                threeDsConfigValidator,
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
    fun `validate() should dispatch error events when validation failed`() {
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
    fun `initialize() should dispatch error events when repository service initialize() was success and config repository getConfiguration() failed`() {
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
    fun `initialize() should dispatch error events when service repository initialize() failed and config repository getConfiguration() was success`() {
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
    fun `performProviderAuth() should dispatch error events when repository performProviderAuth() failed`() {
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
    fun `beginRemoteAuth() should dispatch events when repository begin3DSAuth() was success and response code is not CHALLENGE`() {
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
    fun `beginRemoteAuth() should not dispatch events when repository begin3DSAuth() was success and response code is CHALLENGE`() {
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
    fun `beginRemoteAuth() should dispatch error events when repository begin3DSAuth() failed`() {
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
    fun `beginRemoteAuth() should dispatch error events with responseCode SKIPPED when repository begin3DSAuth() failed`() {
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
    fun `performChallenge() should dispatch error events with responseCode SKIPPED when repository performChallenge() was success`() {
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
    fun `continueRemoteAuth() should dispatch events when repository continue3DSAuth() was success`() {
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
    fun `continueRemoteAuth() should dispatch error events when repository continue3DSAuth() failed`() {
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
        verify { eventDispatcher.dispatchEvents(capture(events)) }

        assert(events.captured.first().type == CheckoutEventType.TOKENIZE_SUCCESS)
        assert(events.captured[1].type == CheckoutEventType.TOKEN_ADDED_TO_VAULT)
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
