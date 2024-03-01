package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.FinalizeKlarnaSessionDataResponse
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.FinalizeKlarnaSessionRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class FinalizeKlarnaSessionInteractorTest {
    @MockK
    private lateinit var finalizeKlarnaSessionRepository: FinalizeKlarnaSessionRepository

    @MockK
    private lateinit var baseErrorEventResolver: BaseErrorEventResolver

    @InjectMockKs
    private lateinit var interactor: FinalizeKlarnaSessionInteractor

    @Test
    fun `performAction() returns response when repository call succeeds`() = runTest {
        val response = mockk<FinalizeKlarnaSessionDataResponse>()
        val params = mockk<KlarnaCustomerTokenParam>()
        coEvery {
            finalizeKlarnaSessionRepository.finalize(params)
        } returns Result.success(response)

        val result = interactor.invoke(params)

        assertEquals(response, result.getOrThrow())
        verify(exactly = 0) {
            baseErrorEventResolver.resolve(any(), any())
        }
    }

    @Test
    fun `performAction() returns error when repository call fails`() = runTest {
        every { baseErrorEventResolver.resolve(any(), any()) } just Runs
        val exception = Exception()
        val params = mockk<KlarnaCustomerTokenParam>()
        coEvery {
            finalizeKlarnaSessionRepository.finalize(params)
        } returns Result.failure(exception)

        val result = interactor.invoke(params)

        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) {
            baseErrorEventResolver.resolve(exception, ErrorMapperType.SESSION_CREATE)
        }
    }
}
