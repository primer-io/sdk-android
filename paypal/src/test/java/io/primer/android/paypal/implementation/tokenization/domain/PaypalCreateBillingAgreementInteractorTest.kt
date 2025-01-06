package io.primer.android.paypal.implementation.tokenization.domain

import io.mockk.coEvery
import io.mockk.mockk
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalBillingAgreement
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateBillingAgreementParams
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalCreateBillingAgreementRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class PaypalCreateBillingAgreementInteractorTest {
    private lateinit var interactor: PaypalCreateBillingAgreementInteractor
    private val createBillingAgreementRepository: PaypalCreateBillingAgreementRepository = mockk()

    @BeforeEach
    fun setUp() {
        interactor =
            PaypalCreateBillingAgreementInteractor(
                createBillingAgreementRepository,
            )
    }

    @Test
    fun `performAction() should call createBillingAgreement with the provided params`() =
        runTest {
            // Given
            val params = mockk<PaypalCreateBillingAgreementParams>(relaxed = true)

            val expectedResult = mockk<PaypalBillingAgreement>(relaxed = true)

            coEvery {
                createBillingAgreementRepository.createBillingAgreement(params)
            } returns Result.success(expectedResult)

            // When
            val result = interactor(params)

            // Then
            assertEquals(Result.success(expectedResult), result)
        }
}
