package io.primer.android.threeds.data.error

import io.mockk.MockKAnnotations
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.domain.error.models.ThreeDsError
import io.primer.android.domain.exception.ThreeDsLibraryNotFoundException
import io.primer.android.domain.exception.ThreeDsLibraryVersionMismatchException
import io.primer.android.threeds.data.exception.ThreeDsChallengeCancelledException
import io.primer.android.threeds.data.exception.ThreeDsChallengeTimedOutException
import io.primer.android.threeds.data.exception.ThreeDsConfigurationException
import io.primer.android.threeds.data.exception.ThreeDsRuntimeFailedException
import io.primer.android.threeds.data.exception.ThreeDsInitException
import io.primer.android.threeds.data.exception.ThreeDsMissingDirectoryServerException
import io.primer.android.threeds.data.exception.ThreeDsProtocolFailedException
import io.primer.android.threeds.data.exception.ThreeDsUnknownProtocolException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class ThreeDsErrorMapperTest {

    private lateinit var mapper: ThreeDsErrorMapper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mapper = ThreeDsErrorMapper()
    }

    @Test
    fun `getPrimerError(ThreeDsLibraryNotFoundException) should return ThreeDsError_ThreeDsLibraryMissingError`() {
        val exception = mockk<ThreeDsLibraryNotFoundException>(relaxed = true)
        runTest {
            val error = mapper.getPrimerError(exception)
            assertEquals<Class<*>>(
                ThreeDsError.ThreeDsLibraryMissingError::class.java,
                error.javaClass
            )
        }
    }

    @Test
    fun `getPrimerError(ThreeDsLibraryVersionMismatchException) should return ThreeDsError_ThreeDsLibraryVersionError`() {
        val exception = mockk<ThreeDsLibraryVersionMismatchException>(relaxed = true)
        runTest {
            val error = mapper.getPrimerError(exception)
            assertEquals<Class<*>>(
                ThreeDsError.ThreeDsLibraryVersionError::class.java,
                error.javaClass
            )
        }
    }

    @Test
    fun `getPrimerError(ThreeDsConfigurationException) should return ThreeDsError_ThreeDsConfigurationError`() {
        val exception = mockk<ThreeDsConfigurationException>(relaxed = true)
        runTest {
            val error = mapper.getPrimerError(exception)
            assertEquals<Class<*>>(
                ThreeDsError.ThreeDsConfigurationError::class.java,
                error.javaClass
            )
        }
    }

    @Test
    fun `getPrimerError(ThreeDsLibraryVersionError) should return ThreeDsError_ThreeDsConfigurationError`() {
        val exception = mockk<ThreeDsConfigurationException>(relaxed = true)
        runTest {
            val error = mapper.getPrimerError(exception)
            assertEquals<Class<*>>(
                ThreeDsError.ThreeDsConfigurationError::class.java,
                error.javaClass
            )
        }
    }

    @Test
    fun `getPrimerError(ThreeDsInitException) should return ThreeDsError_ThreeDsInitError`() {
        val exception = mockk<ThreeDsInitException>(relaxed = true)
        runTest {
            val error = mapper.getPrimerError(exception)
            assertEquals<Class<*>>(
                ThreeDsError.ThreeDsInitError::class.java,
                error.javaClass
            )
        }
    }

    @Test
    fun `getPrimerError(ThreeDsChallengeTimedOutException) should return ThreeDsError_ThreeDsChallengeTimedOutError`() {
        val exception = mockk<ThreeDsChallengeTimedOutException>(relaxed = true)
        runTest {
            val error = mapper.getPrimerError(exception)
            assertEquals<Class<*>>(
                ThreeDsError.ThreeDsChallengeTimedOutError::class.java,
                error.javaClass
            )
        }
    }

    @Test
    fun `getPrimerError(ThreeDsChallengeCancelledException) should return ThreeDsError_ThreeDsChallengeCancelledError`() {
        val exception = mockk<ThreeDsChallengeCancelledException>(relaxed = true)
        runTest {
            val error = mapper.getPrimerError(exception)
            assertEquals<Class<*>>(
                ThreeDsError.ThreeDsChallengeCancelledError::class.java,
                error.javaClass
            )
        }
    }

    @Test
    fun `getPrimerError(ThreeDsFailedException) should return ThreeDsError_ThreeDsChallengeFailedError`() {
        val exception = mockk<ThreeDsRuntimeFailedException>(relaxed = true)
        runTest {
            val error = mapper.getPrimerError(exception)
            assertEquals<Class<*>>(
                ThreeDsError.ThreeDsChallengeFailedError::class.java,
                error.javaClass
            )
        }
    }

    @Test
    fun `getPrimerError(ThreeDsProtocolFailedException) should return ThreeDsError_ThreeDsChallengeProtocolFailedError`() {
        val exception = mockk<ThreeDsProtocolFailedException>(relaxed = true)
        runTest {
            val error = mapper.getPrimerError(exception)
            assertEquals<Class<*>>(
                ThreeDsError.ThreeDsChallengeProtocolFailedError::class.java,
                error.javaClass
            )
        }
    }

    @Test
    fun `getPrimerError(ThreeDsMissingDirectoryServerException) should return ThreeDsError_ThreeDsMissingDirectoryServerIdError`() {
        val exception = mockk<ThreeDsMissingDirectoryServerException>(relaxed = true)
        runTest {
            val error = mapper.getPrimerError(exception)
            assertEquals<Class<*>>(
                ThreeDsError.ThreeDsMissingDirectoryServerIdError::class.java,
                error.javaClass
            )
        }
    }

    @Test
    fun `getPrimerError(ThreeDsUnknownProtocolException) should return ThreeDsError_ThreeDsUnknownProtocolError`() {
        val exception = mockk<ThreeDsUnknownProtocolException>(relaxed = true)
        runTest {
            val error = mapper.getPrimerError(exception)
            assertEquals<Class<*>>(
                ThreeDsError.ThreeDsUnknownProtocolError::class.java,
                error.javaClass
            )
        }
    }
}
