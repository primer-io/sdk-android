package io.primer.android.internal.domain.usecase

import io.mockk.mockk
import io.mockk.verify
import io.primer.android.internal.domain.repositories.KlarnaRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KlarnaCleanupUseCaseTest {

    private lateinit var useCase: KlarnaCleanupUseCase
    private lateinit var mockKlarnaRepository: KlarnaRepository

    @BeforeEach
    fun setUp() {
        mockKlarnaRepository = mockk(relaxed = true)
        useCase = KlarnaCleanupUseCase(mockKlarnaRepository)
    }

    @Test
    fun `cleanup should call klarnaRepository cleanup`() {
        useCase.cleanup()

        verify(exactly = 1) { mockKlarnaRepository.cleanup() }
    }
}
