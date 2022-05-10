package io.primer.android.viewmodel

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
import io.primer.android.InstantExecutorExtension
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.PostAuthResponse
import io.primer.android.threeds.data.models.ResponseCode
import io.primer.android.threeds.domain.interactor.ThreeDsInteractor
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.helpers.ProtocolVersion
import io.primer.android.threeds.presentation.ThreeDsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.component.KoinApiExtension
import org.mockito.Mockito.mock
import kotlin.Exception
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
@KoinApiExtension
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class ThreeDsViewModelTest {

    @RelaxedMockK
    internal lateinit var threeDsInteractor: ThreeDsInteractor

    @RelaxedMockK
    internal lateinit var analyticsInteractor: AnalyticsInteractor

    @RelaxedMockK
    internal lateinit var checkoutConfig: PrimerConfig

    private lateinit var viewModel: ThreeDsViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = ThreeDsViewModel(threeDsInteractor, analyticsInteractor, checkoutConfig)
    }

    @Test
    fun `startThreeDsFlow() should receive init event when initialize was success`() {
        val observer = viewModel.threeDsInitEvent.test()
        coEvery { threeDsInteractor.initialize(any()) }.returns(flowOf(Unit))

        runTest {
            viewModel.startThreeDsFlow()
        }

        coVerify { threeDsInteractor.initialize(any()) }
        observer.assertValue(Unit)
    }

    @Test
    fun `startThreeDsFlow() should receive finished event when initialize failed`() {
        val observer = viewModel.threeDsFinishedEvent.test()
        coEvery { threeDsInteractor.initialize(any()) }.returns(flow { throw Exception() })

        runTest {
            viewModel.startThreeDsFlow()
        }

        coVerify { threeDsInteractor.initialize(any()) }
        observer.assertValue(Unit)
    }

    @Test
    fun `performAuthorization() should receive finished event when interactor authenticateSdk() failed`() {
        val observer = viewModel.threeDsFinishedEvent.test()
        coEvery {
            threeDsInteractor.authenticateSdk()
        }.returns(flow { throw Exception() })

        runTest {
            viewModel.performAuthorization()
        }

        coVerify { threeDsInteractor.authenticateSdk() }

        observer.assertValue(Unit)
    }

    @Test
    fun `performAuthorization() should receive finished event when interactor beginRemoteAuth() failed`() {
        val transaction = mockk<Transaction>(relaxed = true)
        val requestParameters = mock(AuthenticationRequestParameters::class.java)

        val observer = viewModel.threeDsFinishedEvent.test()

        `when`(requestParameters.messageVersion).thenReturn(ProtocolVersion.V_210.versionNumber)
        every { transaction.authenticationRequestParameters }.returns(requestParameters)
        coEvery {
            threeDsInteractor.authenticateSdk()
        }.returns(flowOf(transaction))

        coEvery {
            threeDsInteractor.beginRemoteAuth(any())
        }.returns(flow { throw Exception() })

        runTest {
            viewModel.performAuthorization()
        }

        coVerify { threeDsInteractor.authenticateSdk() }
        coVerify { threeDsInteractor.beginRemoteAuth(any()) }

        observer.assertValue(Unit)
    }

    @Test
    fun `performAuthorization() should receive challenge required event when interactor beginRemoteAuth() was success and response code is CHALLENGE`() {
        val transaction = mockk<Transaction>(relaxed = true)
        val authResponse = mockk<BeginAuthResponse>(relaxed = true)
        val requestParameters = mock(AuthenticationRequestParameters::class.java)

        val observer = viewModel.challengeRequiredEvent.test()

        `when`(requestParameters.messageVersion).thenReturn(ProtocolVersion.V_210.versionNumber)
        every { transaction.authenticationRequestParameters }.returns(requestParameters)
        every { authResponse.authentication.responseCode }.returns(ResponseCode.CHALLENGE)
        coEvery {
            threeDsInteractor.authenticateSdk()
        }.returns(flowOf(transaction))

        coEvery {
            threeDsInteractor.beginRemoteAuth(any())
        }.returns(flowOf(authResponse))

        runTest {
            viewModel.performAuthorization()
        }

        coVerify { threeDsInteractor.authenticateSdk() }
        coVerify { threeDsInteractor.beginRemoteAuth(any()) }

        assertEquals(transaction, observer.value().transaction)
        assertEquals(authResponse, observer.value().authData)
    }

    @Test
    fun `performAuthorization() should receive finished event when interactor beginRemoteAuth() was success and response code is not CHALLENGE`() {
        val transaction = mockk<Transaction>(relaxed = true)
        val requestParameters = mock(AuthenticationRequestParameters::class.java)

        val authResponse = mockk<BeginAuthResponse>(relaxed = true)
        val observer = viewModel.threeDsFinishedEvent.test()

        `when`(requestParameters.messageVersion).thenReturn(ProtocolVersion.V_210.versionNumber)
        every { transaction.authenticationRequestParameters }.returns(requestParameters)

        coEvery {
            threeDsInteractor.authenticateSdk()
        }.returns(flowOf(transaction))

        coEvery {
            threeDsInteractor.beginRemoteAuth(any())
        }.returns(flowOf(authResponse))

        runTest {
            viewModel.performAuthorization()
        }

        coVerify { threeDsInteractor.authenticateSdk() }
        coVerify { threeDsInteractor.beginRemoteAuth(any()) }

        observer.assertValue(Unit)
    }

    @Test
    fun `performChallenge() should receive challenge status event when interactor performChallenge() was success`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val response = mockk<BeginAuthResponse>(relaxed = true)

        val challengeStatusData = mockk<ChallengeStatusData>(relaxed = true)

        val observer = viewModel.challengeStatusChangedEvent.test()
        coEvery {
            threeDsInteractor.performChallenge(
                any(),
                any(),
                any()
            )
        }.returns(flowOf(challengeStatusData))

        runTest {
            viewModel.performChallenge(activity, transaction, response)
        }

        coVerify {
            threeDsInteractor.performChallenge(
                any(),
                any(),
                any()
            )
        }

        observer.assertValue(challengeStatusData)
    }

    @Test
    fun `performChallenge() should receive finished event when interactor performChallenge() failed`() {
        val activity = mockk<Activity>(relaxed = true)
        val transaction = mockk<Transaction>(relaxed = true)
        val response = mockk<BeginAuthResponse>(relaxed = true)

        val observer = viewModel.threeDsFinishedEvent.test()
        coEvery {
            threeDsInteractor.performChallenge(
                any(),
                any(),
                any()
            )
        }.returns(flow { throw Exception() })

        runTest {
            viewModel.performChallenge(activity, transaction, response)
        }

        observer.assertValue(Unit)
    }

    @Test
    fun `continueRemoteAuth() should receive finished event when interactor continueRemoteAuth() was success`() {
        val response = mockk<PostAuthResponse>(relaxed = true)

        val observer = viewModel.threeDsFinishedEvent.test()
        coEvery {
            threeDsInteractor.continueRemoteAuth(any())
        }.returns(flowOf(response))

        runTest {
            viewModel.continueRemoteAuth("")
        }

        coVerify { threeDsInteractor.continueRemoteAuth(any()) }

        observer.assertValue(Unit)
    }

    @Test
    fun `continueRemoteAuth() should receive finished event when interactor continueRemoteAuth() failed`() {
        val observer = viewModel.threeDsFinishedEvent.test()
        coEvery {
            threeDsInteractor.continueRemoteAuth(any())
        }.returns(flow { throw Exception() })

        runTest {
            viewModel.continueRemoteAuth("")
        }

        coVerify { threeDsInteractor.continueRemoteAuth(any()) }

        observer.assertValue(Unit)
    }
}
