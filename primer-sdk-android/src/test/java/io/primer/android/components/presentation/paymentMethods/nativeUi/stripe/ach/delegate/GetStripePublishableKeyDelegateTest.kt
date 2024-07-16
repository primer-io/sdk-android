package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.exception.StripeIllegalValueKey
import io.primer.android.data.base.exceptions.IllegalValueException
import io.primer.android.data.settings.PrimerSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class GetStripePublishableKeyDelegateTest {
    @MockK
    private lateinit var primerSettings: PrimerSettings

    @InjectMockKs
    private lateinit var delegate: GetStripePublishableKeyDelegate

    @Test
    fun `invoke() should return publishable key if it exists in Stripe options`() {
        every { primerSettings.paymentMethodOptions.stripeOptions.publishableKey } returns "pk"

        val result = delegate.invoke()

        assertEquals(Result.success("pk"), result)
    }

    @Test
    fun `invoke() should return failure if key doesn't exist in Stripe options`() {
        every { primerSettings.paymentMethodOptions.stripeOptions.publishableKey } returns null

        val result = delegate.invoke().exceptionOrNull() as? IllegalValueException

        assertInstanceOf(IllegalValueException::class.java, result)
        assertEquals(
            StripeIllegalValueKey.MISSING_PUBLISHABLE_KEY,
            result?.key
        )
    }
}
