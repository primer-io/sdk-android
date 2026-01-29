package io.primer.android.internal.domain.usecase

import io.mockk.mockk
import io.mockk.verify
import io.primer.android.internal.domain.repositories.HeadlessRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HeadlessCleanupUseCaseTest {

    private lateinit var useCase: HeadlessCleanupUseCase
    private lateinit var mockHeadlessRepository: HeadlessRepository

    @BeforeEach
    fun setUp() {
        mockHeadlessRepository = mockk(relaxed = true)
        useCase = HeadlessCleanupUseCase(mockHeadlessRepository)
    }

    @Test
    fun `invoke should call headlessRepository cleanup`() {
        useCase()

        verify(exactly = 1) { mockHeadlessRepository.cleanup() }
    }
}
