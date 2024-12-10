package io.primer.android.payments.core.tokenization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.data.tokenization.models.TokenType
import io.primer.android.payments.core.tokenization.data.model.toPaymentMethodToken
import io.primer.android.payments.core.tokenization.domain.TokenizationInteractor
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class PaymentMethodTokenizationDelegateTest {

    @Test
    fun `tokenize should call tokenizationInteractor with the expected parameters when result success`() {
        // Mock dependencies
        val paymentMethodTokenInternal = PaymentMethodTokenInternal(
            "mockToken",
            "mockPaymentInstrumentType",
            "mockPaymentMethodType",
            null,
            null,
            null,
            true,
            "mockAnalyticsId",
            TokenType.SINGLE_USE
        )

        val tokenizationParams = TokenizationParams(mockk<BasePaymentInstrumentParams>(), PrimerSessionIntent.CHECKOUT)
        val tokenizationInteractor = mockk<TokenizationInteractor<BasePaymentInstrumentParams>>()
        coEvery { tokenizationInteractor.invoke(tokenizationParams) } returns Result.success(paymentMethodTokenInternal)

        val delegate = object :
            PaymentMethodTokenizationDelegate<TokenizationInputable, BasePaymentInstrumentParams>(
                tokenizationInteractor
            ) {
            override suspend fun mapTokenizationData(input: TokenizationInputable) = Result.success(tokenizationParams)
        }

        val expectedResult = paymentMethodTokenInternal.toPaymentMethodToken()

        // Perform the action
        runTest {
            val result = delegate.tokenize(mockk<TokenizationInputable>())
            assertTrue { result.isSuccess }
            assertEquals(expectedResult, result.getOrNull())
        }
        // Verify interactions
        coVerify { tokenizationInteractor.invoke(tokenizationParams) }
    }
}
