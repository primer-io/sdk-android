package io.primer.android.payments.core.tokenization.data.repository

import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class TokenizedPaymentMethodDataRepositoryTest {
    private lateinit var repository: TokenizedPaymentMethodRepository

    @BeforeEach
    fun setUp() {
        repository = TokenizedPaymentMethodDataRepository()
    }

    @Test
    fun `test setPaymentMethod and getPaymentMethod`() {
        // Given
        val paymentMethodTokenInternal = mockk<PaymentMethodTokenInternal>()

        // When
        repository.setPaymentMethod(paymentMethodTokenInternal)
        val retrievedPaymentMethod = repository.getPaymentMethod()

        // Then
        assertEquals(paymentMethodTokenInternal, retrievedPaymentMethod)
    }

    @Test
    fun `test setPaymentMethod`() {
        // Given
        val paymentMethodTokenInternal = mockk<PaymentMethodTokenInternal>()

        // When
        repository.setPaymentMethod(paymentMethodTokenInternal)

        // Then
        assertEquals(paymentMethodTokenInternal, repository.getPaymentMethod())
    }

    @Test
    fun `test getPaymentMethod`() {
        // Given
        val paymentMethodTokenInternal = mockk<PaymentMethodTokenInternal>()
        repository.setPaymentMethod(paymentMethodTokenInternal)

        // When
        val retrievedPaymentMethod = repository.getPaymentMethod()

        // Then
        assertEquals(paymentMethodTokenInternal, retrievedPaymentMethod)
    }
}
