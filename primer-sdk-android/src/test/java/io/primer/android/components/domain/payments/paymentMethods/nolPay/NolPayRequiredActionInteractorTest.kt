package io.primer.android.components.domain.payments.paymentMethods.nolPay

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayRequiredActionInteractor
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.token.repository.ClientTokenRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
internal class NolPayRequiredActionInteractorTest {

    @MockK
    private lateinit var clientTokenRepository: ClientTokenRepository

    @MockK
    private lateinit var errorEventResolver: BaseErrorEventResolver

    private lateinit var interactor: NolPayRequiredActionInteractor

    @Test
    fun `invoke() should return NolPayRequiredAction when all arguments are non-null`() = runTest {
        interactor = NolPayRequiredActionInteractor(
            clientTokenRepository = clientTokenRepository,
            errorEventResolver = errorEventResolver,
            dispatcher = StandardTestDispatcher(testScheduler)
        )
        coEvery { clientTokenRepository.getTransactionNo() } returns "transactionNo"
        coEvery { clientTokenRepository.getStatusUrl() } returns "statusUrl"
        coEvery { clientTokenRepository.getRedirectUrl() } returns "redirectUrl"

        val result = interactor.invoke(None()).getOrThrow()

        assertEquals("transactionNo", result.transactionNumber)
        assertEquals("statusUrl", result.statusUrl)
        assertEquals("redirectUrl", result.completeUrl)
        assertEquals(PaymentMethodType.NOL_PAY.name, result.paymentMethodType)
    }

    @Test
    fun `invoke() should throw IllegalArgumentException when getTransactionNo returns null`() = runTest {
        interactor = NolPayRequiredActionInteractor(
            clientTokenRepository = clientTokenRepository,
            errorEventResolver = errorEventResolver,
            dispatcher = StandardTestDispatcher(testScheduler)
        )
        every { errorEventResolver.resolve(any(), any()) } just Runs
        coEvery { clientTokenRepository.getTransactionNo() } returns null
        coEvery { clientTokenRepository.getStatusUrl() } returns "statusUrl"
        coEvery { clientTokenRepository.getRedirectUrl() } returns "redirectUrl"

        val exception = interactor.invoke(None()).exceptionOrNull()

        assertEquals(
            "Required value for resumeToken.transactionNo was null.",
            exception?.message
        )
        verify {
            errorEventResolver.resolve(any(), ErrorMapperType.NOL_PAY)
        }
    }

    @Test
    fun `invoke() should throw IllegalArgumentException when getStatusUrl returns null`() = runTest {
        interactor = NolPayRequiredActionInteractor(
            clientTokenRepository = clientTokenRepository,
            errorEventResolver = errorEventResolver,
            dispatcher = StandardTestDispatcher(testScheduler)
        )
        every { errorEventResolver.resolve(any(), any()) } just Runs
        coEvery { clientTokenRepository.getTransactionNo() } returns "transactionNo"
        coEvery { clientTokenRepository.getStatusUrl() } returns null
        coEvery { clientTokenRepository.getRedirectUrl() } returns "redirectUrl"

        val exception = interactor.invoke(None()).exceptionOrNull()

        assertEquals(
            "Required value for resumeToken.statusUrl was null.",
            exception?.message
        )
        verify {
            errorEventResolver.resolve(any(), ErrorMapperType.NOL_PAY)
        }
    }

    @Test
    fun `invoke() should throw IllegalArgumentException when getRedirectUrl returns null`() = runTest {
        interactor = NolPayRequiredActionInteractor(
            clientTokenRepository = clientTokenRepository,
            errorEventResolver = errorEventResolver,
            dispatcher = StandardTestDispatcher(testScheduler)
        )
        every { errorEventResolver.resolve(any(), any()) } just Runs
        coEvery { clientTokenRepository.getTransactionNo() } returns "transactionNo"
        coEvery { clientTokenRepository.getStatusUrl() } returns "statusUrl"
        coEvery { clientTokenRepository.getRedirectUrl() } returns null

        val exception = interactor.invoke(None()).exceptionOrNull()

        assertEquals(
            "Required value for resumeToken.completeUrl was null.",
            exception?.message
        )
        verify {
            errorEventResolver.resolve(any(), ErrorMapperType.NOL_PAY)
        }
    }
}
