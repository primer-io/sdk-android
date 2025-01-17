package io.primer.android.paymentMethods.core

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import io.primer.android.core.domain.None
import io.primer.android.paymentMethods.core.domain.repository.PrimerHeadlessRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class PrimerHeadlessSdkCleanupInteractorTest {
    @MockK
    private lateinit var headlessRepository: PrimerHeadlessRepository
    private lateinit var cleanupInteractor: PrimerHeadlessSdkCleanupInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        cleanupInteractor = PrimerHeadlessSdkCleanupInteractor(headlessRepository)
    }

    @Test
    fun `execute should call cleanup on headlessRepository`() {
        every { headlessRepository.cleanup() } just Runs
        // Act
        cleanupInteractor.execute(None)

        // Assert
        verify { headlessRepository.cleanup() }
    }

    @Test
    fun `execute should not throw any exceptions`() {
        every { headlessRepository.cleanup() } just Runs

        assertDoesNotThrow {
            cleanupInteractor.execute(None)
        }
    }

    @Test
    fun `cleanup should not be called if execute is not invoked`() {
        every { headlessRepository.cleanup() } just Runs

        // Assert
        verify(exactly = 0) { headlessRepository.cleanup() }
    }
}
