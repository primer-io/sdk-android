package io.primer.android.threeds.data.models.postAuth

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.domain.error.models.ThreeDsError
import io.primer.android.threeds.data.models.postAuth.error.ChallengeProtocolContinueAuthErrorDataRequest
import io.primer.android.threeds.data.models.postAuth.error.ChallengeRuntimeContinueAuthErrorDataRequest
import io.primer.android.threeds.data.models.postAuth.error.PreChallengeContinueAuthErrorDataRequest
import io.primer.android.threeds.domain.models.FailureThreeDsContinueAuthParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class BaseFailureContinueAuthDataRequestTest {

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun `toContinueAuthDataRequest(ThreeDsError_ThreeDsLibraryMissingError) should map to MissingDependencyFailureContinueAuthDataRequest`() {
        val error = mockk<ThreeDsError.ThreeDsLibraryMissingError>(relaxed = true)
        val params = mockk<FailureThreeDsContinueAuthParams>(relaxed = true)

        every { params.error }.returns(error)
        runTest {
            val continueAuthDataRequest = params.toContinueAuthDataRequest()
            assertEquals<Class<*>>(
                MissingDependencyFailureContinueAuthDataRequest::class.java,
                continueAuthDataRequest.javaClass,
            )
        }
    }

    @Test
    fun `toContinueAuthDataRequest(ThreeDsError_ThreeDsLibraryVersionError) should map to DefaultFailureContinueAuthDataRequest`() {
        val error = mockk<ThreeDsError.ThreeDsLibraryVersionError>(relaxed = true)
        val params = mockk<FailureThreeDsContinueAuthParams>(relaxed = true)

        every { params.error }.returns(error)
        runTest {
            val continueAuthDataRequest = params.toContinueAuthDataRequest()
            assertEquals<Class<*>>(
                DefaultFailureContinueAuthDataRequest::class.java,
                continueAuthDataRequest.javaClass,
            )
        }
    }

    @Test
    fun `toContinueAuthDataRequest(ThreeDsError_ThreeDsLibraryMissingError) should map to PreChallengeContinueAuthErrorDataRequest error`() {
        val error = mockk<ThreeDsError.ThreeDsLibraryMissingError>(relaxed = true)
        val params = mockk<FailureThreeDsContinueAuthParams>(relaxed = true)

        every { params.error }.returns(error)
        runTest {
            val continueAuthDataRequest = params.toContinueAuthDataRequest()
            assertEquals<Class<*>>(
                PreChallengeContinueAuthErrorDataRequest::class.java,
                continueAuthDataRequest.error.javaClass,
            )
        }
    }

    @Test
    fun `toContinueAuthDataRequest(ThreeDsError_ThreeDsChallengeFailedError) should map to ChallengeRuntimeContinueAuthErrorDataRequest error`() {
        val error = mockk<ThreeDsError.ThreeDsChallengeFailedError>(relaxed = true)
        val params = mockk<FailureThreeDsContinueAuthParams>(relaxed = true)

        every { params.error }.returns(error)
        runTest {
            val continueAuthDataRequest = params.toContinueAuthDataRequest()
            assertEquals<Class<*>>(
                ChallengeRuntimeContinueAuthErrorDataRequest::class.java,
                continueAuthDataRequest.error.javaClass,
            )
        }
    }

    @Test
    fun `toContinueAuthDataRequest(ThreeDsError_ThreeDsChallengeCancelledError) should map to ChallengeRuntimeContinueAuthErrorDataRequest error`() {
        val error = mockk<ThreeDsError.ThreeDsChallengeCancelledError>(relaxed = true)
        val params = mockk<FailureThreeDsContinueAuthParams>(relaxed = true)

        every { params.error }.returns(error)
        runTest {
            val continueAuthDataRequest = params.toContinueAuthDataRequest()
            assertEquals<Class<*>>(
                ChallengeRuntimeContinueAuthErrorDataRequest::class.java,
                continueAuthDataRequest.error.javaClass,
            )
        }
    }

    @Test
    fun `toContinueAuthDataRequest(ThreeDsError_ThreeDsChallengeTimedOutError) should map to ChallengeRuntimeContinueAuthErrorDataRequest error`() {
        val error = mockk<ThreeDsError.ThreeDsChallengeTimedOutError>(relaxed = true)
        val params = mockk<FailureThreeDsContinueAuthParams>(relaxed = true)

        every { params.error }.returns(error)
        runTest {
            val continueAuthDataRequest = params.toContinueAuthDataRequest()
            assertEquals<Class<*>>(
                ChallengeRuntimeContinueAuthErrorDataRequest::class.java,
                continueAuthDataRequest.error.javaClass,
            )
        }
    }

    @Test
    fun `toContinueAuthDataRequest(ThreeDsError_ThreeDsChallengeProtocolFailedError) should map to ChallengeProtocolContinueAuthErrorDataRequest error`() {
        val error = mockk<ThreeDsError.ThreeDsChallengeProtocolFailedError>(relaxed = true)
        val params = mockk<FailureThreeDsContinueAuthParams>(relaxed = true)

        every { params.error }.returns(error)
        runTest {
            val continueAuthDataRequest = params.toContinueAuthDataRequest()
            assertEquals<Class<*>>(
                ChallengeProtocolContinueAuthErrorDataRequest::class.java,
                continueAuthDataRequest.error.javaClass,
            )
        }
    }

    @Test
    fun `toContinueAuthDataRequest(ThreeDsError_ThreeDsConfigurationError) should map to PreChallengeContinueAuthErrorDataRequest error`() {
        val error = mockk<ThreeDsError.ThreeDsConfigurationError>(relaxed = true)
        val params = mockk<FailureThreeDsContinueAuthParams>(relaxed = true)

        every { params.error }.returns(error)
        runTest {
            val continueAuthDataRequest = params.toContinueAuthDataRequest()
            assertEquals<Class<*>>(
                PreChallengeContinueAuthErrorDataRequest::class.java,
                continueAuthDataRequest.error.javaClass,
            )
        }
    }

    @Test
    fun `toContinueAuthDataRequest(ThreeDsError_ThreeDsInitError) should map to PreChallengeContinueAuthErrorDataRequest error`() {
        val error = mockk<ThreeDsError.ThreeDsInitError>(relaxed = true)
        val params = mockk<FailureThreeDsContinueAuthParams>(relaxed = true)

        every { params.error }.returns(error)
        runTest {
            val continueAuthDataRequest = params.toContinueAuthDataRequest()
            assertEquals<Class<*>>(
                PreChallengeContinueAuthErrorDataRequest::class.java,
                continueAuthDataRequest.error.javaClass,
            )
        }
    }

    @Test
    fun `toContinueAuthDataRequest(ThreeDsError_ThreeDsLibraryVersionError) should map to PreChallengeContinueAuthErrorDataRequest error`() {
        val error = mockk<ThreeDsError.ThreeDsLibraryVersionError>(relaxed = true)
        val params = mockk<FailureThreeDsContinueAuthParams>(relaxed = true)

        every { params.error }.returns(error)
        runTest {
            val continueAuthDataRequest = params.toContinueAuthDataRequest()
            assertEquals<Class<*>>(
                PreChallengeContinueAuthErrorDataRequest::class.java,
                continueAuthDataRequest.error.javaClass,
            )
        }
    }

    @Test
    fun `toContinueAuthDataRequest(ThreeDsError_ThreeDsMissingDirectoryServerIdError) should map to PreChallengeContinueAuthErrorDataRequest error`() {
        val error = mockk<ThreeDsError.ThreeDsMissingDirectoryServerIdError>(relaxed = true)
        val params = mockk<FailureThreeDsContinueAuthParams>(relaxed = true)

        every { params.error }.returns(error)
        runTest {
            val continueAuthDataRequest = params.toContinueAuthDataRequest()
            assertEquals<Class<*>>(
                PreChallengeContinueAuthErrorDataRequest::class.java,
                continueAuthDataRequest.error.javaClass,
            )
        }
    }

    @Test
    fun `toContinueAuthDataRequest(ThreeDsError_ThreeDsUnknownProtocolError) should map to PreChallengeContinueAuthErrorDataRequest error`() {
        val error = mockk<ThreeDsError.ThreeDsUnknownProtocolError>(relaxed = true)
        val params = mockk<FailureThreeDsContinueAuthParams>(relaxed = true)

        every { params.error }.returns(error)
        runTest {
            val continueAuthDataRequest = params.toContinueAuthDataRequest()
            assertEquals<Class<*>>(
                PreChallengeContinueAuthErrorDataRequest::class.java,
                continueAuthDataRequest.error.javaClass,
            )
        }
    }

    @Test
    fun `toContinueAuthDataRequest(ThreeDsError_ThreeDsUnknownError) should map to PreChallengeContinueAuthErrorDataRequest error`() {
        val error = mockk<ThreeDsError.ThreeDsUnknownError>(relaxed = true)
        val params = mockk<FailureThreeDsContinueAuthParams>(relaxed = true)

        every { params.error }.returns(error)
        runTest {
            val continueAuthDataRequest = params.toContinueAuthDataRequest()
            assertEquals<Class<*>>(
                PreChallengeContinueAuthErrorDataRequest::class.java,
                continueAuthDataRequest.error.javaClass,
            )
        }
    }
}
