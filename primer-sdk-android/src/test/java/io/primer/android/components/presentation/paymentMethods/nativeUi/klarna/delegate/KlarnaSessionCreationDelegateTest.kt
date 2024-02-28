package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.KlarnaSessionInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class KlarnaSessionCreationDelegateTest {
    @MockK
    private lateinit var klarnaSessionInteractor: KlarnaSessionInteractor

    private lateinit var delegate: KlarnaSessionCreationDelegate

    @BeforeEach
    fun setUp() {
        delegate = KlarnaSessionCreationDelegate(interactor = klarnaSessionInteractor)
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(klarnaSessionInteractor)
    }

    @Test
    fun `createSession() should return session emitted by session interactor when called`() = runTest {
        val klarnaSession = mockk<KlarnaSession>()
        every { klarnaSessionInteractor.execute(any()) } returns flowOf(klarnaSession)

        val result = delegate.createSession().getOrNull()

        assertEquals(klarnaSession, result)
        coVerify(exactly = 1) {
            klarnaSessionInteractor.execute(any())
        }
    }

    @Test
    fun `createSession() should return error emitted by session interactor when called`() = runTest {
        val exception = Exception()
        every { klarnaSessionInteractor.execute(any()) } returns flow { throw exception }

        val result = delegate.createSession().exceptionOrNull()

        assertEquals(exception, result)
        coVerify(exactly = 1) {
            klarnaSessionInteractor.execute(any())
        }
    }
}
