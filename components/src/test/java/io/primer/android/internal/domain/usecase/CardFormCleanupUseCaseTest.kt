package io.primer.android.internal.domain.usecase

import io.mockk.mockk
import io.mockk.verify
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CardFormCleanupUseCaseTest {

    private lateinit var useCase: CardFormCleanupUseCase
    private lateinit var mockRawDataManagerRepository: RawDataManagerRepository

    @BeforeEach
    fun setUp() {
        mockRawDataManagerRepository = mockk(relaxed = true)
        useCase = CardFormCleanupUseCase(mockRawDataManagerRepository)
    }

    @Test
    fun `cleanup should call rawDataManagerRepository cleanup`() {
        useCase.cleanup()

        verify(exactly = 1) { mockRawDataManagerRepository.cleanup() }
    }
}
