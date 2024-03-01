package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSessionParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaSessionRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
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

    @MockK
    private lateinit var baseErrorEventResolver: BaseErrorEventResolver

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
        verify(exactly = 0) {
            baseErrorEventResolver.resolve(any(), any())
        }
    }

    @Test
    fun `performAction() returns error when repository call fails`() = runTest {
        every { baseErrorEventResolver.resolve(any(), any()) } just Runs
        val exception = Exception()
        coEvery {
            klarnaSessionRepository.createSession(surcharge = any(), primerSessionIntent = any())
        } returns Result.failure(exception)

        val result = interactor.invoke(KlarnaSessionParams(1, PrimerSessionIntent.VAULT))

        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            klarnaSessionRepository.createSession(1, PrimerSessionIntent.VAULT)
        }
        verify(exactly = 1) {
            baseErrorEventResolver.resolve(exception, ErrorMapperType.SESSION_CREATE)
        }
    }
}
