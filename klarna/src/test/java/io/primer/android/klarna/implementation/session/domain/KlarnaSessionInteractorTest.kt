package io.primer.android.klarna.implementation.session.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSession
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSessionParams
import io.primer.android.klarna.implementation.session.domain.repository.KlarnaSessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class KlarnaSessionInteractorTest {
    @MockK
    private lateinit var klarnaSessionRepository: KlarnaSessionRepository

    @InjectMockKs
    private lateinit var interactor: KlarnaSessionInteractor

    @Test
    fun `performAction() returns response when repository call succeeds`() = runTest {
        val session = mockk<KlarnaSession>()
        coEvery {
            klarnaSessionRepository.createSession(surcharge = any(), primerSessionIntent = any())
        } returns Result.success(session)

        val result = interactor.invoke(KlarnaSessionParams(1, PrimerSessionIntent.VAULT))

        assertEquals(session, result.getOrThrow())
        coVerify(exactly = 1) {
            klarnaSessionRepository.createSession(1, PrimerSessionIntent.VAULT)
        }
    }

    @Test
    fun `performAction() returns error when repository call fails`() = runTest {
        val exception = Exception()
        coEvery {
            klarnaSessionRepository.createSession(surcharge = any(), primerSessionIntent = any())
        } returns Result.failure(exception)

        val result = interactor.invoke(KlarnaSessionParams(1, PrimerSessionIntent.VAULT))

        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            klarnaSessionRepository.createSession(1, PrimerSessionIntent.VAULT)
        }
    }
}
