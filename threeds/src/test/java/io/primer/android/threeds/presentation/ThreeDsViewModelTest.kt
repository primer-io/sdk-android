package io.primer.android.threeds.presentation

import android.app.Activity
import com.jraska.livedata.test
import com.netcetera.threeds.sdk.api.transaction.AuthenticationRequestParameters
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.payments.core.tokenization.data.model.ResponseCode
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.data.models.postAuth.PostAuthResponse
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class ThreeDsViewModelTest {
    @RelaxedMockK
    internal lateinit var threeDsInteractor: ThreeDsInteractor

    @RelaxedMockK
    internal lateinit var analyticsInteractor: AnalyticsInteractor

    @RelaxedMockK
    internal lateinit var checkoutConfig: PrimerSettings

    private lateinit var viewModel: ThreeDsViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel =
            ThreeDsViewModel(
                threeDsInteractor = threeDsInteractor,
                analyticsInteractor = analyticsInteractor,
                settings = checkoutConfig,
            )
    }

    @Test
    fun `startThreeDsFlow() should receive init event when initialize was success`() {
        val observer = viewModel.threeDsInitEvent.test()
        coEvery { threeDsInteractor.initialize(any()) }.returns(Result.success(Unit))

        runTest {
            viewModel.startThreeDsFlow()
        }

        coVerify { threeDsInteractor.initialize(any()) }
        observer.assertValue(Unit)
    }

    @Test
    fun `startThreeDsFlow() should receive error event when initialize failed`() {
        val observer = viewModel.threeDsErrorEvent.test()
        val exception = mockk<Exception>()
        coEvery { threeDsInteractor.initialize(any()) }.returns(Result.failure(exception))

        runTest {
            viewModel.startThreeDsFlow()
        }

        coVerify { threeDsInteractor.initialize(any()) }
        observer.assertValue(exception)
    }

    @Test
    fun `performAuthorization() should receive error event when interactor authenticateSdk() failed`() {
        val activity = mockk<Activity>(relaxed = true)
        val observer = viewModel.threeDsErrorEvent.test()
        val exception = mockk<Exception>()

        val supportedProtocolVersions = listOf(ProtocolVersion.V_210.versionNumber)

        coEvery {
            threeDsInteractor.authenticateSdk(any(), any())
        }.returns(Result.failure(exception))

        runTest {
            viewModel.performAuthorization(
                activity = activity,
                supportedThreeDsProtocolVersions = supportedProtocolVersions,
                paymentMethodToken = PAYMENT_METHOD_TOKEN,
                cardNetwork = CardNetwork.Type.MASTERCARD,
            )
        }

        coVerify { threeDsInteractor.authenticateSdk(any(), any()) }

        observer.assertValue(exception)
    }

    @Test
    fun `performAuthorization() should receive error event when interactor beginRemoteAuth() failed`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val requestParameters = mock(AuthenticationRequestParameters::class.java)
        val exception = mockk<Exception>()

        val supportedProtocolVersions = listOf(ProtocolVersion.V_210.versionNumber)

        val observer = viewModel.threeDsErrorEvent.test()

        `when`(requestParameters.messageVersion).thenReturn(ProtocolVersion.V_210.versionNumber)
        every { transaction.authenticationRequestParameters }.returns(requestParameters)
        coEvery {
            threeDsInteractor.authenticateSdk(supportedThreeDsProtocolVersions = any(), cardNetwork = any())
        }.returns(Result.success(transaction))

        coEvery {
            threeDsInteractor.beginRemoteAuth(threeDsParams = any(), paymentMethodToken = any())
        }.returns(Result.failure(exception))

        runTest {
            viewModel.performAuthorization(
                activity = activity,
                supportedThreeDsProtocolVersions = supportedProtocolVersions,
                paymentMethodToken = PAYMENT_METHOD_TOKEN,
                cardNetwork = CardNetwork.Type.MASTERCARD,
            )
        }

        coVerify { threeDsInteractor.authenticateSdk(any(), any()) }
        coVerify { threeDsInteractor.beginRemoteAuth(any(), any()) }

        observer.assertValue(exception)
    }

    @Test
    fun `performAuthorization() should receive challenge required event when interactor beginRemoteAuth() was success and response code is CHALLENGE`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val authResponse = mockk<BeginAuthResponse>(relaxed = true)
        val requestParameters = mock(AuthenticationRequestParameters::class.java)

        val supportedProtocolVersions = listOf(ProtocolVersion.V_210.versionNumber)

        val observer = viewModel.challengeRequiredEvent.test()

        `when`(requestParameters.messageVersion).thenReturn(ProtocolVersion.V_210.versionNumber)
        every { transaction.authenticationRequestParameters }.returns(requestParameters)
        every { authResponse.authentication.responseCode }.returns(ResponseCode.CHALLENGE)
        coEvery {
            threeDsInteractor.authenticateSdk(any(), any())
        }.returns(Result.success(transaction))

        coEvery {
            threeDsInteractor.beginRemoteAuth(any(), any())
        }.returns(Result.success(authResponse))

        runTest {
            viewModel.performAuthorization(
                activity = activity,
                supportedThreeDsProtocolVersions = supportedProtocolVersions,
                paymentMethodToken = PAYMENT_METHOD_TOKEN,
                cardNetwork = CardNetwork.Type.MASTERCARD,
            )
        }

        coVerify { threeDsInteractor.authenticateSdk(any(), any()) }
        coVerify { threeDsInteractor.beginRemoteAuth(any(), any()) }

        assertEquals(transaction, observer.value().transaction)
        assertEquals(authResponse, observer.value().authData)
    }

    @Test
    fun `performAuthorization() should receive finished event when interactor beginRemoteAuth() was success and response code is not CHALLENGE`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val requestParameters = mock(AuthenticationRequestParameters::class.java)

        val authResponse = mockk<BeginAuthResponse>(relaxed = true)
        val observer = viewModel.threeDsFinishedEvent.test()

        `when`(requestParameters.messageVersion).thenReturn(ProtocolVersion.V_210.versionNumber)
        every { transaction.authenticationRequestParameters }.returns(requestParameters)

        val supportedProtocolVersions = listOf(ProtocolVersion.V_210.versionNumber)

        coEvery {
            threeDsInteractor.authenticateSdk(any(), any())
        }.returns(Result.success(transaction))

        coEvery {
            threeDsInteractor.beginRemoteAuth(any(), any())
        }.returns(Result.success(authResponse))

        runTest {
            viewModel.performAuthorization(
                activity = activity,
                supportedThreeDsProtocolVersions = supportedProtocolVersions,
                paymentMethodToken = PAYMENT_METHOD_TOKEN,
                cardNetwork = CardNetwork.Type.MASTERCARD,
            )
        }

        coVerify { threeDsInteractor.authenticateSdk(any(), any()) }
        coVerify { threeDsInteractor.beginRemoteAuth(any(), any()) }

        assertEquals(authResponse.resumeToken, observer.value())
    }

    @Test
    fun `performChallenge() should not run in case challenge is in progress`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val response = mockk<BeginAuthResponse>(relaxed = true)

        viewModel.challengeInProgress = true

        runTest {
            viewModel.performChallenge(activity, transaction, response)
        }

        verify(exactly = 0) {
            threeDsInteractor.performChallenge(any(), any(), any())
        }
    }

    @Test
    fun `performChallenge() should receive challenge status event when interactor performChallenge() was success`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val response = mockk<BeginAuthResponse>(relaxed = true)

        val challengeStatusData = mockk<ChallengeStatusData>(relaxed = true)

        val observer = viewModel.threeDsStatusChangedEvent.test()
        coEvery {
            threeDsInteractor.performChallenge(
                any(),
                any(),
                any(),
            )
        }.returns(flowOf(challengeStatusData))

        runTest {
            viewModel.performChallenge(activity, transaction, response)
        }

        coVerify {
            threeDsInteractor.performChallenge(
                any(),
                any(),
                any(),
            )
        }

        observer.assertValue(challengeStatusData)
    }

    @Test
    fun `performChallenge() should receive error event when interactor performChallenge() failed`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val response = mockk<BeginAuthResponse>(relaxed = true)
        val exception = mockk<Exception>()

        val observer = viewModel.threeDsErrorEvent.test()
        coEvery {
            threeDsInteractor.performChallenge(
                any(),
                any(),
                any(),
            )
        }.returns(flow { throw exception })

        runTest {
            viewModel.performChallenge(activity, transaction, response)
        }

        observer.assertValue(exception)
    }

    @Test
    fun `continueRemoteAuth() should receive finished event when interactor continueRemoteAuth() was success`() {
        val response = mockk<PostAuthResponse>(relaxed = true)

        val observer = viewModel.threeDsFinishedEvent.test()
        coEvery {
            threeDsInteractor.continueRemoteAuth(any(), any())
        }.returns(Result.success(response))

        runTest {
            viewModel.continueRemoteAuth(
                ChallengeStatusData("", "Y"),
                listOf(ProtocolVersion.V_210.versionNumber),
            )
        }

        coVerify { threeDsInteractor.continueRemoteAuth(any(), any()) }

        assertEquals(response.resumeToken, observer.value())
    }

    @Test
    fun `continueRemoteAuth() should receive finished event when interactor continueRemoteAuth() failed`() {
        val exception = mockk<Exception>(relaxed = true)
        val observer = viewModel.threeDsErrorEvent.test()
        coEvery {
            threeDsInteractor.continueRemoteAuth(any(), any())
        }.returns(Result.failure(exception))

        runTest {
            viewModel.continueRemoteAuth(
                ChallengeStatusData("", "Y"),
                listOf(ProtocolVersion.V_210.versionNumber),
            )
        }

        coVerify { threeDsInteractor.continueRemoteAuth(any(), any()) }

        assertEquals(exception, observer.value())
    }

    @Test
    fun `continueRemoteAuthWithException() should receive finished event when interactor continueRemoteAuthWithException() was success`() {
        val response = mockk<PostAuthResponse>(relaxed = true)

        val observer = viewModel.threeDsFinishedEvent.test()
        coEvery {
            threeDsInteractor.continueRemoteAuthWithException(any(), any(), any())
        }.returns(Result.success(response))

        runTest {
            viewModel.continueRemoteAuthWithException(
                Exception(),
                RESUME_TOKEN,
                listOf(ProtocolVersion.V_210.versionNumber),
                PAYMENT_METHOD_TOKEN,
            )
        }

        coVerify { threeDsInteractor.continueRemoteAuthWithException(any(), any(), any()) }

        assertEquals(response.resumeToken, observer.value())
    }

    @Test
    fun `continueRemoteAuthWithException() should receive finished event when interactor continueRemoteAuthWithException() failed`() {
        val exception = mockk<Exception>(relaxed = true)

        val observer = viewModel.threeDsFinishedEvent.test()
        coEvery {
            threeDsInteractor.continueRemoteAuthWithException(any(), any(), any())
        }.returns(Result.failure(exception))

        runTest {
            viewModel.continueRemoteAuthWithException(
                Exception(),
                RESUME_TOKEN,
                listOf(ProtocolVersion.V_210.versionNumber),
                PAYMENT_METHOD_TOKEN,
            )
        }

        coVerify { threeDsInteractor.continueRemoteAuthWithException(any(), any(), any()) }

        assertEquals(RESUME_TOKEN, observer.value())
    }

    @Test
    fun `onCleared() should call interactor cleanup()`() {
        runTest {
            viewModel.onCleared()
        }

        verify { threeDsInteractor.cleanup() }
    }

    private companion object {

        const val PAYMENT_METHOD_TOKEN = "token"
        const val RESUME_TOKEN = "resume_token"
    }
}
