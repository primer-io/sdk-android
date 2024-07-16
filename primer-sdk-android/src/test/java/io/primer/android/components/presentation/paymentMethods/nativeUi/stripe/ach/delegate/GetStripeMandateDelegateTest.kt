package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate

import android.content.res.Resources
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.R
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.exception.StripeIllegalValueKey
import io.primer.android.data.base.exceptions.IllegalValueException
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.PrimerStripeOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class GetStripeMandateDelegateTest {
    @MockK
    private lateinit var resources: Resources

    @MockK
    private lateinit var primerSettings: PrimerSettings

    @InjectMockKs
    private lateinit var delegate: GetStripeMandateDelegate

    @AfterEach
    fun tearDown() {
        confirmVerified(resources, primerSettings)
    }

    @Test
    fun `invoke() should return full mandate if it exists in Stripe options`() {
        val fullMandate = mockk<PrimerStripeOptions.MandateData.FullMandateData> {
            every { value } returns 1
        }
        every { resources.getString(1) } returns "full mandate"
        every { primerSettings.paymentMethodOptions.stripeOptions.mandateData } returns fullMandate

        val result = delegate.invoke()

        Assertions.assertEquals(Result.success("full mandate"), result)
        verify {
            resources.getString(1)
            primerSettings.paymentMethodOptions.stripeOptions.mandateData
        }
    }

    @Test
    fun `invoke() should return template mandate if substitution data exists in Stripe options`() {
        every {
            resources.getString(R.string.stripe_ach_mandate_template, any())
        } returns "This is a template [Merchant name]"
        val templateMandate = mockk<PrimerStripeOptions.MandateData.TemplateMandateData> {
            every { merchantName } returns "Merchant name"
        }
        every { primerSettings.paymentMethodOptions.stripeOptions.mandateData } returns templateMandate

        val result = delegate.invoke()

        Assertions.assertEquals(Result.success("This is a template [Merchant name]"), result)
        verify {
            resources.getString(R.string.stripe_ach_mandate_template, "Merchant name")
            primerSettings.paymentMethodOptions.stripeOptions.mandateData
        }
    }

    @Test
    fun `invoke() should return failure if the mandate data doesn't exist in Stripe options`() {
        every { primerSettings.paymentMethodOptions.stripeOptions.mandateData } returns null

        val result = delegate.invoke().exceptionOrNull() as? IllegalValueException

        Assertions.assertInstanceOf(IllegalValueException::class.java, result)
        Assertions.assertEquals(
            StripeIllegalValueKey.MISSING_MANDATE_DATA,
            result?.key
        )
        verify {
            primerSettings.paymentMethodOptions.stripeOptions.mandateData
        }
    }
}
