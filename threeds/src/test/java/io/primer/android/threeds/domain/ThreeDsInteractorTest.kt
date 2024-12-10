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
import io.mockk.verify
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.data.tokenization.models.BinData
import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.data.model.ResponseCode
import io.primer.android.data.tokenization.models.TokenType
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.threeds.InstantExecutorExtension
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.data.models.postAuth.PostAuthResponse
import io.primer.android.threeds.domain.interactor.DefaultThreeDsInteractor
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor
import io.primer.android.threeds.domain.models.BaseThreeDsParams
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsAuthParams
import io.primer.android.threeds.domain.models.ThreeDsInitParams
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.domain.repository.ThreeDsAppUrlRepository
import io.primer.android.threeds.domain.repository.ThreeDsConfigurationRepository
import io.primer.android.threeds.domain.repository.ThreeDsRepository
import io.primer.android.threeds.domain.repository.ThreeDsServiceRepository
import io.primer.android.threeds.errors.domain.exception.ThreeDsLibraryNotFoundException
import io.primer.android.threeds.errors.domain.exception.ThreeDsLibraryVersionMismatchException
import io.primer.android.threeds.helpers.ProtocolVersion
import io.primer.android.threeds.helpers.ThreeDsLibraryVersionValidator
import io.primer.android.threeds.helpers.ThreeDsSdkClassValidator
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
    internal lateinit var threeDsSdkClassValidator: ThreeDsSdkClassValidator

    @RelaxedMockK
    internal lateinit var threeDsLibraryVersionValidator: ThreeDsLibraryVersionValidator

    @RelaxedMockK
    internal lateinit var threeDsServiceRepository: ThreeDsServiceRepository

    @RelaxedMockK
    internal lateinit var threeDsRepository: ThreeDsRepository

    @RelaxedMockK
    internal lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository

    @RelaxedMockK
    internal lateinit var threeDsConfigurationRepository: ThreeDsConfigurationRepository

    @RelaxedMockK
    internal lateinit var threeDsAppUrlRepository: ThreeDsAppUrlRepository

    @RelaxedMockK
    internal lateinit var errorMapperRegistry: ErrorMapperRegistry

    @RelaxedMockK
    internal lateinit var analyticsRepository: AnalyticsRepository

    @RelaxedMockK
    internal lateinit var logReporter: LogReporter

    private lateinit var interactor: ThreeDsInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor = DefaultThreeDsInteractor(
            threeDsSdkClassValidator = threeDsSdkClassValidator,
            threeDsLibraryVersionValidator = threeDsLibraryVersionValidator,
            threeDsServiceRepository = threeDsServiceRepository,
            threeDsRepository = threeDsRepository,
            tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
            threeDsAppUrlRepository = threeDsAppUrlRepository,
            threeDsConfigurationRepository = threeDsConfigurationRepository,
            errorMapperRegistry = errorMapperRegistry,
            analyticsRepository = analyticsRepository,
            logReporter = logReporter
        )

        every {
            tokenizedPaymentMethodRepository.getPaymentMethod()
        }.returns(paymentMethodTokenInternal)
    }

    @Test
    fun `initialize() should return error when repository service initialize() failed because library is not included`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)

        every { threeDsSdkClassValidator.is3dsSdkIncluded() }.returns(false)

        runTest {
            val capturedException = requireNotNull(interactor.initialize(initParams).exceptionOrNull())
            assertEquals(ThreeDsLibraryNotFoundException::class, capturedException::class)
            assertEquals(ThreeDsSdkClassValidator.THREE_DS_CLASS_NOT_LOADED_ERROR, capturedException.message)
        }

        verify { threeDsSdkClassValidator.is3dsSdkIncluded() }
    }

    @Test
    fun `initialize() should return error when repository service initialize() failed because imported library has version mismatch`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)

        every { threeDsSdkClassValidator.is3dsSdkIncluded() }.returns(true)
        every { threeDsLibraryVersionValidator.isValidVersion() }.returns(false)

        runTest {
            val capturedException = requireNotNull(interactor.initialize(initParams).exceptionOrNull())
            assertEquals(ThreeDsLibraryVersionMismatchException::class, capturedException::class)
        }

        verify { threeDsSdkClassValidator.is3dsSdkIncluded() }
        verify { threeDsLibraryVersionValidator.isValidVersion() }
    }

    @Test
    fun `initialize() should continue when service repository initialize() and config repository getConfiguration() was success`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)
        val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)

        every { threeDsSdkClassValidator.is3dsSdkIncluded() }.returns(true)
        every { threeDsLibraryVersionValidator.isValidVersion() }.returns(true)

        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            Result.success(keysParams)
        )
        coEvery { threeDsServiceRepository.initializeProvider(any(), any(), any()) }.returns(
            Result.success(Unit)
        )

        runTest {
            interactor.initialize(initParams)
        }

        verify { threeDsSdkClassValidator.is3dsSdkIncluded() }
        verify { threeDsLibraryVersionValidator.isValidVersion() }

        coVerify { threeDsConfigurationRepository.getConfiguration() }
        coVerify { threeDsServiceRepository.initializeProvider(any(), any(), any()) }
    }

    @Test
    fun `initialize() should return error when repository service initialize() was success and config repository getConfiguration() failed`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Config failed.")

        every { threeDsSdkClassValidator.is3dsSdkIncluded() }.returns(true)
        every { threeDsLibraryVersionValidator.isValidVersion() }.returns(true)

        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            Result.failure(exception)
        )

        runTest {
            val capturedException = requireNotNull(interactor.initialize(initParams).exceptionOrNull())
            assertEquals(exception.javaClass, capturedException.javaClass)
            assertEquals(exception.message, capturedException.message)
        }

        verify { threeDsSdkClassValidator.is3dsSdkIncluded() }
        verify { threeDsLibraryVersionValidator.isValidVersion() }

        coVerify { threeDsConfigurationRepository.getConfiguration() }
    }

    @Test
    fun `initialize() should return error when service repository initialize() failed and config repository getConfiguration() was success`() {
        val initParams = mockk<ThreeDsInitParams>(relaxed = true)
        val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("3DS init failed.")

        every { threeDsSdkClassValidator.is3dsSdkIncluded() }.returns(true)
        every { threeDsLibraryVersionValidator.isValidVersion() }.returns(true)

        coEvery { threeDsConfigurationRepository.getConfiguration() }.returns(
            Result.success(keysParams)
        )
        coEvery { threeDsServiceRepository.initializeProvider(any(), any(), any()) }.returns(
            Result.failure(exception)
        )

        runTest {
            val capturedException = requireNotNull(interactor.initialize(initParams).exceptionOrNull())
            assertEquals(exception.javaClass, capturedException.javaClass)
            assertEquals(exception.message, capturedException.message)
        }

        verify { threeDsSdkClassValidator.is3dsSdkIncluded() }
        verify { threeDsLibraryVersionValidator.isValidVersion() }

        coVerify { threeDsServiceRepository.initializeProvider(any(), any(), any()) }
        coVerify { threeDsConfigurationRepository.getConfiguration() }
    }

    @Test
    fun `performProviderAuth() should return transaction when repository performProviderAuth() was success`() {
        val transactionMock = mockk<Transaction>(relaxed = true)
        val authParams = mockk<ThreeDsAuthParams>(relaxed = true)
        every { authParams.protocolVersions }.returns(listOf(ProtocolVersion.V_210))

        coEvery { threeDsConfigurationRepository.getPreAuthConfiguration(any()) }.returns(
            Result.success(authParams)
        )
        coEvery { threeDsServiceRepository.performProviderAuth(any(), any(), any()) }.returns(
            Result.success(transactionMock)
        )
        runTest {
            val transaction = interactor.authenticateSdk(
                authParams.protocolVersions.map { protocolVersion -> protocolVersion.versionNumber }
            ).getOrThrow()
            assertEquals(transactionMock, transaction)
        }

        coVerify { threeDsServiceRepository.performProviderAuth(any(), any(), any()) }
    }

    @Test
    fun `performProviderAuth() should return error when repository performProviderAuth() failed`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to perform provider auth.")

        val authParams = mockk<ThreeDsAuthParams>(relaxed = true)
        every { authParams.protocolVersions }.returns(listOf(ProtocolVersion.V_210))

        coEvery { threeDsConfigurationRepository.getPreAuthConfiguration(any()) }.returns(
            Result.success(authParams)
        )

        coEvery { threeDsServiceRepository.performProviderAuth(any(), any(), any()) }.returns(
            Result.failure(exception)
        )

        runTest {
            val capturedException = requireNotNull(
                interactor.authenticateSdk(
                    authParams.protocolVersions.map { protocolVersion -> protocolVersion.versionNumber }
                ).exceptionOrNull()
            )
            assertEquals(exception.javaClass, capturedException.javaClass)
            assertEquals(exception.message, capturedException.message)
        }
    }

    @Test
    fun `beginRemoteAuth() should return success response when repository begin3DSAuth() was success`() {
        val beginAuthResponse = mockk<BeginAuthResponse>(relaxed = true)
        val threeDsParams = mockk<BaseThreeDsParams>(relaxed = true)

        every { beginAuthResponse.authentication.responseCode } returns ResponseCode.AUTH_SUCCESS
        every { beginAuthResponse.token }.returns(paymentMethodTokenInternal)

        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            Result.success(beginAuthResponse)
        )

        runTest {
            val response = interactor.beginRemoteAuth(threeDsParams).getOrThrow()
            assertEquals(beginAuthResponse, response)
        }

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }
    }

    @Test
    fun `beginRemoteAuth() should return error when repository begin3DSAuth() failed`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to execute 3DS auth.")

        val threeDsParams = mockk<BaseThreeDsParams>(relaxed = true)
        coEvery { threeDsRepository.begin3DSAuth(any(), any()) }.returns(
            Result.failure(exception)
        )

        runTest {
            val capturedException = requireNotNull(
                interactor.beginRemoteAuth(threeDsParams)
                    .exceptionOrNull()
            )

            assertEquals(exception.javaClass, capturedException.javaClass)
            assertEquals(exception.message, capturedException.message)
        }

        coVerify { threeDsRepository.begin3DSAuth(any(), any()) }
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
    fun `performChallenge() should dispatch error events when repository performChallenge() failed`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val authResponse = mockk<BeginAuthResponse>(relaxed = true)
        val challengeStatusData = mockk<ChallengeStatusData>()

        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to perform 3DS challenge.")

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
    fun `continueRemoteAuth() should return resume response when repository continue3DSAuth() was success`() {
        val postAuthResponse = mockk<PostAuthResponse>(relaxed = true)

        val authParams = mockk<ThreeDsAuthParams>(relaxed = true)
        every { authParams.protocolVersions }.returns(listOf(ProtocolVersion.V_210))

        coEvery { threeDsConfigurationRepository.getPreAuthConfiguration(any()) }.returns(
            Result.success(authParams)
        )

        coEvery { threeDsRepository.continue3DSAuth(any(), any()) }.returns(
            Result.success(postAuthResponse)
        )

        runTest {
            val response = interactor.continueRemoteAuth(
                ChallengeStatusData("", "Y"),
                authParams.protocolVersions.map { protocolVersion -> protocolVersion.versionNumber }
            ).getOrThrow()
            assertEquals(postAuthResponse, response)
        }

        coVerify { threeDsRepository.continue3DSAuth(any(), any()) }
    }

    @Test
    fun `continueRemoteAuthWithException() should return resume respinse when repository continue3DSAuth() was success`() {
        val postAuthResponse = mockk<PostAuthResponse>(relaxed = true)

        val authParams = mockk<ThreeDsAuthParams>(relaxed = true)
        every { authParams.protocolVersions }.returns(listOf(ProtocolVersion.V_210))

        coEvery { threeDsConfigurationRepository.getPreAuthConfiguration(any()) }.returns(
            Result.success(authParams)
        )

        val threeDsException = mockk<Exception>(relaxed = true)
        coEvery { threeDsRepository.continue3DSAuth(any(), any()) }.returns(
            Result.success(postAuthResponse)
        )

        runTest {
            val response = interactor.continueRemoteAuthWithException(
                threeDsException,
                authParams.protocolVersions.map { protocolVersion -> protocolVersion.versionNumber }
            ).getOrThrow()
            assertEquals(postAuthResponse, response)
        }

        coVerify { threeDsConfigurationRepository.getPreAuthConfiguration(any()) }
        coVerify { threeDsRepository.continue3DSAuth(any(), any()) }
    }

    @Test
    fun `continueRemoteAuth() should return error when repository continue3DSAuth() failed`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to continue 3DS auth.")

        val authParams = mockk<ThreeDsAuthParams>(relaxed = true)
        every { authParams.protocolVersions }.returns(listOf(ProtocolVersion.V_210))

        coEvery { threeDsConfigurationRepository.getPreAuthConfiguration(any()) }.returns(
            Result.success(authParams)
        )
        coEvery { threeDsRepository.continue3DSAuth(any(), any()) }.returns(
            Result.failure(exception)
        )

        runTest {
            val capturedException = requireNotNull(
                interactor.continueRemoteAuth(
                    ChallengeStatusData("", "Y"),
                    authParams.protocolVersions.map { protocolVersion -> protocolVersion.versionNumber }
                ).exceptionOrNull()
            )

            assertEquals(exception.javaClass, capturedException.javaClass)
            assertEquals(exception.message, capturedException.message)
        }

        coVerify { threeDsConfigurationRepository.getPreAuthConfiguration(any()) }
        coVerify { threeDsRepository.continue3DSAuth(any(), any()) }
    }

    @Test
    fun `continueRemoteAuthWithException() should return resume error when repository continue3DSAuth() failed`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to continue 3DS auth.")

        val authParams = mockk<ThreeDsAuthParams>(relaxed = true)
        every { authParams.protocolVersions }.returns(listOf(ProtocolVersion.V_210))

        coEvery { threeDsConfigurationRepository.getPreAuthConfiguration(any()) }.returns(
            Result.success(authParams)
        )
        coEvery { threeDsRepository.continue3DSAuth(any(), any()) }.returns(
            Result.failure(exception)
        )

        runTest {
            val capturedException = requireNotNull(
                interactor.continueRemoteAuthWithException(
                    exception,
                    authParams.protocolVersions.map { protocolVersion -> protocolVersion.versionNumber }
                ).exceptionOrNull()
            )

            assertEquals(exception.javaClass, capturedException.javaClass)
            assertEquals(exception.message, capturedException.message)
        }

        coVerify { threeDsConfigurationRepository.getPreAuthConfiguration(any()) }
        coVerify { threeDsRepository.continue3DSAuth(any(), any()) }
        verify {
            errorMapperRegistry.getPrimerError(
                exception
            )
        }
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
