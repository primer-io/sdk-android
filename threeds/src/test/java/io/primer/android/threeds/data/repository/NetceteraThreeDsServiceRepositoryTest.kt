package io.primer.android.threeds.data.repository

import android.app.Activity
import android.content.Context
import android.os.Build
import com.netcetera.threeds.sdk.api.ThreeDS2Service
import com.netcetera.threeds.sdk.api.exceptions.InvalidInputException
import com.netcetera.threeds.sdk.api.security.Warning
import com.netcetera.threeds.sdk.api.transaction.Transaction
import com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeStatusReceiver
import com.netcetera.threeds.sdk.api.transaction.challenge.ErrorMessage
import com.netcetera.threeds.sdk.api.transaction.challenge.events.CompletionEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.ProtocolErrorEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.RuntimeErrorEvent
import com.netcetera.threeds.sdk.api.ui.logic.UiCustomization
import com.netcetera.threeds.sdk.api.utils.DsRidValues
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.Environment
import io.primer.android.core.utils.DeviceInfo
import io.primer.android.threeds.InstantExecutorExtension
import io.primer.android.threeds.data.exception.ThreeDsChallengeCancelledException
import io.primer.android.threeds.data.exception.ThreeDsChallengeTimedOutException
import io.primer.android.threeds.data.exception.ThreeDsConfigurationException
import io.primer.android.threeds.data.exception.ThreeDsInitException
import io.primer.android.threeds.data.exception.ThreeDsInvalidStatusException
import io.primer.android.threeds.data.exception.ThreeDsMissingDirectoryServerException
import io.primer.android.threeds.data.exception.ThreeDsProtocolFailedException
import io.primer.android.threeds.data.exception.ThreeDsRuntimeFailedException
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.util.Locale
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class NetceteraThreeDsServiceRepositoryTest {
    @RelaxedMockK
    lateinit var context: Context

    @RelaxedMockK
    lateinit var threeDS2Service: ThreeDS2Service

    // Mock dependencies for the method
    @RelaxedMockK
    lateinit var mockActivity: Activity

    @RelaxedMockK
    lateinit var mockTransaction: Transaction

    @RelaxedMockK
    lateinit var mockAuthResponse: BeginAuthResponse

    private val mockThreeDsAppURL = "https://mock.threeds.app/url"
    private val mockInitProtocolVersion = "2.1.0"

    private lateinit var repository: NetceteraThreeDsServiceRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkObject(DeviceInfo)
        repository = NetceteraThreeDsServiceRepository(context = context, lazyOf(threeDS2Service))
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `threeDsSdkVersion should return null when threeDS2Service_sdkVersion throws NoClassDefFoundError`() {
        every { threeDS2Service.sdkVersion } throws NoClassDefFoundError()
        assertEquals(null, repository.threeDsSdkVersion)
    }

    @Test
    fun `threeDsSdkVersion should return null when threeDS2Service_sdkVersion throws Exception`() {
        every { threeDS2Service.sdkVersion } throws Exception()
        assertEquals(null, repository.threeDsSdkVersion)
    }

    @Test
    fun `initializeProvider should return ThreeDsConfigurationException KEYS_CONFIG_ERROR message when ThreeDsKeysParams are missing`() =
        runTest {
            val result =
                repository.initializeProvider(
                    is3DSSanityCheckEnabled = false,
                    locale = Locale.getDefault(),
                    threeDsKeysParams = null,
                )
            val exception = requireNotNull(result.exceptionOrNull())
            assertEquals(ThreeDsConfigurationException::class, exception::class)
            assertEquals(NetceteraThreeDsServiceRepository.KEYS_CONFIG_ERROR, exception.message)
        }

    @Test
    fun `initializeProvider should return ThreeDsConfigurationException API_KEY_CONFIG_ERROR message when ThreeDsKeysParams licence is missing`() =
        runTest {
            val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)
            every { keysParams.apiKey }.returns(null)

            val result =
                repository.initializeProvider(
                    is3DSSanityCheckEnabled = false,
                    locale = Locale.getDefault(),
                    threeDsKeysParams = keysParams,
                )

            val exception = requireNotNull(result.exceptionOrNull())
            assertEquals(ThreeDsConfigurationException::class, exception::class)
            assertEquals(NetceteraThreeDsServiceRepository.API_KEY_CONFIG_ERROR, exception.message)
        }

    @Test
    fun `initializeProvider should return ThreeDsInitException with correct message when threeDS2Service initialize fails`() =
        runTest {
            val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)
            val exception = mockk<InvalidInputException>(relaxed = true)
            every { exception.message } returns "Failed to initialize 3DS SDK"

            every {
                threeDS2Service.initialize(
                    any(),
                    any(),
                    any(),
                    any<Map<UiCustomization.UiCustomizationType, UiCustomization>>(),
                )
            } throws exception

            val result =
                repository.initializeProvider(
                    is3DSSanityCheckEnabled = false,
                    locale = Locale.getDefault(),
                    threeDsKeysParams = keysParams,
                )

            val resultException = requireNotNull(result.exceptionOrNull())
            assertEquals(ThreeDsInitException::class, resultException::class)
            assertEquals(exception.message, resultException.message)
        }

    @Test
    fun `initializeProvider should strip extensions from Locale in Android O and later`() =
        runTest {
            val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)
            every { DeviceInfo.isSdkVersionAtLeast(Build.VERSION_CODES.O) } returns true
            val locale =
                Locale.Builder()
                    .setLanguageTag("en-US")
                    .setExtension('u', "mu-celsius")
                    .build()

            every {
                threeDS2Service.initialize(
                    any(),
                    any(),
                    any(),
                    any<Map<UiCustomization.UiCustomizationType, UiCustomization>>(),
                )
            } returns Unit

            val result =
                repository.initializeProvider(
                    is3DSSanityCheckEnabled = false,
                    locale = locale,
                    threeDsKeysParams = keysParams,
                )

            result.getOrThrow()

            verify {
                threeDS2Service.initialize(
                    context,
                    any(),
                    "en_US",
                    emptyMap<UiCustomization.UiCustomizationType, UiCustomization>(),
                )
            }
        }

    @Test
    fun `initializeProvider should not strip extensions from Locale in Android versions older than O`() =
        runTest {
            val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)
            every { DeviceInfo.isSdkVersionAtLeast(Build.VERSION_CODES.O) } returns false
            val locale =
                Locale.Builder()
                    .setLanguageTag("en-US")
                    .setExtension('u', "mu-celsius")
                    .build()

            every {
                threeDS2Service.initialize(
                    any(),
                    any(),
                    any(),
                    any<Map<UiCustomization.UiCustomizationType, UiCustomization>>(),
                )
            } returns Unit

            val result =
                repository.initializeProvider(
                    is3DSSanityCheckEnabled = false,
                    locale = locale,
                    threeDsKeysParams = keysParams,
                )

            result.getOrThrow()

            verify {
                threeDS2Service.initialize(
                    context,
                    any(),
                    "en_US_#u-mu-celsius",
                    emptyMap<UiCustomization.UiCustomizationType, UiCustomization>(),
                )
            }
        }

    @Test
    fun `initializeProvider should continue flow when is3DSSanityCheckEnabled is not enabled `() =
        runTest {
            val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)

            every {
                threeDS2Service.initialize(
                    any(),
                    any(),
                    any(),
                    any<Map<UiCustomization.UiCustomizationType, UiCustomization>>(),
                )
            } returns Unit

            val result =
                repository.initializeProvider(
                    is3DSSanityCheckEnabled = false,
                    locale = Locale.getDefault(),
                    threeDsKeysParams = keysParams,
                )

            assertEquals(Unit, result.getOrThrow())
        }

    @Test
    fun `initializeProvider should continue flow when is3DSSanityCheckEnabled is enabled and there are no warnings and initializing provider is successful`() =
        runTest {
            val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)
            every {
                threeDS2Service.initialize(
                    any(),
                    any(),
                    any(),
                    any<Map<UiCustomization.UiCustomizationType, UiCustomization>>(),
                )
            } returns Unit

            every { threeDS2Service.warnings }.returns(listOf())

            val result =
                repository.initializeProvider(
                    is3DSSanityCheckEnabled = true,
                    locale = Locale.getDefault(),
                    threeDsKeysParams = keysParams,
                )
            assertEquals(Unit, result.getOrThrow())

            verify { threeDS2Service.warnings }
        }

    @Test
    fun `initializeProvider should return ThreeDsInitException with correct message when is3DSSanityCheckEnabled is enabled`() =
        runTest {
            val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)
            val warning = mock(Warning::class.java)

            Mockito.`when`(warning.id).thenReturn("S01")

            every { threeDS2Service.warnings }.returns(listOf(warning))

            every {
                threeDS2Service.initialize(
                    any(),
                    any(),
                    any(),
                    any<Map<UiCustomization.UiCustomizationType, UiCustomization>>(),
                )
            } returns Unit

            val result =
                repository.initializeProvider(
                    is3DSSanityCheckEnabled = true,
                    locale = Locale.getDefault(),
                    threeDsKeysParams = keysParams,
                )
            val exception = result.exceptionOrNull()

            verify { threeDS2Service.warnings }

            assertEquals(
                listOf(warning).joinToString(",") { "${it.severity}  ${it.message}" },
                exception?.message,
            )
        }

    @Test
    fun `performProviderAuth should return Transaction object`() {
        val cardNetwork = mockk<CardNetwork.Type>(relaxed = true)
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        val transactionMock = mockk<Transaction>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val result =
                repository.performProviderAuth(cardNetwork, protocolVersion, environment)
            assertEquals(transactionMock, result.getOrThrow())
        }

        verify { threeDS2Service.createTransaction(any(), any()) }
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-VISA for CardNetwork-VISA`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val result =
                repository.performProviderAuth(CardNetwork.Type.VISA, protocolVersion, environment)
            assertEquals(transactionMock, result.getOrThrow())
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.VISA, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-MASTERCARD for CardNetwork-MASTERCARD`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val result =
                repository.performProviderAuth(CardNetwork.Type.MASTERCARD, protocolVersion, environment)
            assertEquals(transactionMock, result.getOrThrow())
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.MASTERCARD, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-MASTERCARD for CardNetwork-MAESTRO`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val result =
                repository.performProviderAuth(CardNetwork.Type.MAESTRO, protocolVersion, environment)
            assertEquals(transactionMock, result.getOrThrow())
        }
        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.MASTERCARD, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-AMEX for CardNetwork-AMEX`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val result =
                repository.performProviderAuth(CardNetwork.Type.AMEX, protocolVersion, environment)
            assertEquals(transactionMock, result.getOrThrow())
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.AMEX, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-JCB for CardNetwork-JCB`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val result =
                repository.performProviderAuth(CardNetwork.Type.JCB, protocolVersion, environment)
            assertEquals(transactionMock, result.getOrThrow())
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.JCB, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-DINERS for CardNetwork-DINERS_CLUB`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val result =
                repository.performProviderAuth(CardNetwork.Type.DINERS_CLUB, protocolVersion, environment)
            assertEquals(transactionMock, result.getOrThrow())
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.DINERS, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-DINERS for CardNetwork-DISCOVER`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val result =
                repository.performProviderAuth(CardNetwork.Type.DISCOVER, protocolVersion, environment)
            assertEquals(transactionMock, result.getOrThrow())
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.DINERS, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-UNION for CardNetwork-UNIONPAY`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val result =
                repository.performProviderAuth(CardNetwork.Type.UNIONPAY, protocolVersion, environment)
            assertEquals(transactionMock, result.getOrThrow())
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.UNION, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server TEST_SCHEME_ID for any other card network if environment is SANDBOX`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val result =
                repository.performProviderAuth(CardNetwork.Type.OTHER, protocolVersion, Environment.SANDBOX)
            assertEquals(transactionMock, result.getOrThrow())
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(NetceteraThreeDsServiceRepository.TEST_SCHEME_ID, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should throw ThreeDsMissingDirectoryServerException for any other card network if environment is PRODUCTION`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val result =
                repository.performProviderAuth(CardNetwork.Type.OTHER, protocolVersion, Environment.PRODUCTION)

            val resultException = requireNotNull(result.exceptionOrNull())
            assertEquals(ThreeDsMissingDirectoryServerException::class.java, resultException::class.java)
        }
    }

    @Test
    fun `performCleanup should invoke threeDS2Service-cleanup`() {
        every { threeDS2Service.cleanup(any()) }.returns(Unit)

        runTest {
            repository.performCleanup()
        }

        verify { threeDS2Service.cleanup(any()) }
    }

    @Test
    fun `performChallenge should return ChallengeStatusData when transaction status is SUCCESS`() =
        runTest {
            // Mock successful completion event
            val mockCompletionEvent = mockk<CompletionEvent>()
            every { mockCompletionEvent.transactionStatus } returns ChallengeStatusData.TRANSACTION_STATUS_SUCCESS

            // Mock expected ChallengeStatusData
            val expectedStatusData = ChallengeStatusData("", ChallengeStatusData.TRANSACTION_STATUS_SUCCESS)

            // Mock doChallenge to invoke the completion callback
            every {
                mockTransaction.doChallenge(any(), any(), any(), any())
            } answers {
                val callback = args[2] as ChallengeStatusReceiver
                callback.completed(mockCompletionEvent)
            }

            // Call performChallenge and collect the flow
            val flow =
                repository.performChallenge(
                    mockActivity,
                    mockTransaction,
                    mockAuthResponse,
                    mockThreeDsAppURL,
                    mockInitProtocolVersion,
                )
            var result: ChallengeStatusData? = null
            flow.collect {
                result = it
            }

            // Assert that the emitted result matches the expected data
            assertEquals(expectedStatusData, result)
        }

    @Test
    fun `performChallenge should throw ThreeDsInvalidStatusException when transaction status is not SUCCESS`() =
        runTest {
            // Mock successful completion event
            val mockCompletionEvent = mockk<CompletionEvent>()
            every { mockCompletionEvent.transactionStatus } returns "smth else"
            every { mockCompletionEvent.sdkTransactionID } returns "mockTransactionID"

            // Mock doChallenge to invoke the completion callback
            every {
                mockTransaction.doChallenge(any(), any(), any(), any())
            } answers {
                val callback = args[2] as ChallengeStatusReceiver
                callback.completed(mockCompletionEvent)
            }

            assertThrows<ThreeDsInvalidStatusException> {
                // Call performChallenge and collect the flow
                val flow =
                    repository.performChallenge(
                        mockActivity,
                        mockTransaction,
                        mockAuthResponse,
                        mockThreeDsAppURL,
                        mockInitProtocolVersion,
                    )
                flow.collect {}
            }
        }

    @Test
    fun `performChallenge should throw ThreeDsChallengeCancelledException when transaction status is cancelled`() =
        runTest {
            // Mock cancellation scenario
            every {
                mockTransaction.doChallenge(any(), any(), any(), any())
            } answers {
                val callback = args[2] as ChallengeStatusReceiver
                callback.cancelled()
            }

            assertThrows<ThreeDsChallengeCancelledException> {
                // Call performChallenge and collect the flow
                val flow =
                    repository.performChallenge(
                        mockActivity,
                        mockTransaction,
                        mockAuthResponse,
                        mockThreeDsAppURL,
                        mockInitProtocolVersion,
                    )
                flow.collect {}
            }
        }

    @Test
    fun `performChallenge should throw ThreeDsChallengeTimedOutException when transaction status is timedout`() =
        runTest {
            // Mock timeout scenario
            every {
                mockTransaction.doChallenge(any(), any(), any(), any())
            } answers {
                val callback = args[2] as ChallengeStatusReceiver
                callback.timedout()
            }

            assertThrows<ThreeDsChallengeTimedOutException> {
                // Call performChallenge and collect the flow
                val flow =
                    repository.performChallenge(
                        mockActivity,
                        mockTransaction,
                        mockAuthResponse,
                        mockThreeDsAppURL,
                        mockInitProtocolVersion,
                    )
                flow.collect {}
            }
        }

    @Test
    fun `performChallenge should throw ThreeDsProtocolFailedException when protocol error occurs`() =
        runTest {
            // Mock protocol error scenario
            val mockErrorEvent = mockk<ProtocolErrorEvent>()
            every { mockErrorEvent.errorMessage } returns
                mockk<ErrorMessage> {
                    every { errorCode } returns "mockErrorCode"
                    every { errorDetails } returns "mockErrorDetails"
                    every { errorDescription } returns "mockErrorDescription"
                    every { errorMessageType } returns "mockErrorMessageType"
                    every { errorComponent } returns "mockErrorComponent"
                    every { transactionID } returns "mockTransactionID"
                    every { messageVersionNumber } returns "mockMessageVersionNumber"
                }

            every {
                mockTransaction.doChallenge(any(), any(), any(), any())
            } answers {
                val callback = args[2] as ChallengeStatusReceiver
                callback.protocolError(mockErrorEvent)
            }

            assertThrows<ThreeDsProtocolFailedException> {
                // Call performChallenge and collect the flow
                val flow =
                    repository.performChallenge(
                        mockActivity,
                        mockTransaction,
                        mockAuthResponse,
                        mockThreeDsAppURL,
                        mockInitProtocolVersion,
                    )
                flow.collect {}
            }
        }

    @Test
    fun `performChallenge should throw ThreeDsRuntimeFailedException when runtime error occurs`() =
        runTest {
            // Mock runtime error scenario
            val mockErrorEvent = mockk<RuntimeErrorEvent>()
            every { mockErrorEvent.errorCode } returns "mockErrorCode"
            every { mockErrorEvent.errorMessage } returns "mockErrorMessage"

            every {
                mockTransaction.doChallenge(any(), any(), any(), any())
            } answers {
                val callback = args[2] as ChallengeStatusReceiver
                callback.runtimeError(mockErrorEvent)
            }

            assertThrows<ThreeDsRuntimeFailedException> {
                // Call performChallenge and collect the flow
                val flow =
                    repository.performChallenge(
                        mockActivity,
                        mockTransaction,
                        mockAuthResponse,
                        mockThreeDsAppURL,
                        mockInitProtocolVersion,
                    )
                flow.collect {}
            }
        }
}
