package io.primer.android.klarna.implementation.session.domain

import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataResponse
import io.primer.android.klarna.implementation.session.domain.models.KlarnaCustomerTokenParam
import io.primer.android.klarna.implementation.session.domain.repository.KlarnaCustomerTokenRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class KlarnaCustomerTokenInteractorTest {
    @MockK
    private lateinit var klarnaCustomerTokenRepository: KlarnaCustomerTokenRepository

    @InjectMockKs
    private lateinit var interactor: KlarnaCustomerTokenInteractor

    @Test
    fun `performAction() returns response when repository call succeeds`() =
        runTest {
            val response = mockk<CreateCustomerTokenDataResponse>()
            val params = mockk<KlarnaCustomerTokenParam>()
            coEvery {
                klarnaCustomerTokenRepository.createCustomerToken(params)
            } returns Result.success(response)

            val result = interactor.invoke(params)

            assertEquals(response, result.getOrThrow())
        }

    @Test
    fun `performAction() returns error when repository call fails`() =
        runTest {
            val exception = Exception()
            val params = mockk<KlarnaCustomerTokenParam>()
            coEvery {
                klarnaCustomerTokenRepository.createCustomerToken(params)
            } returns Result.failure(exception)

            val result = interactor.invoke(params)

            assertEquals(exception, result.exceptionOrNull())
        }
}
