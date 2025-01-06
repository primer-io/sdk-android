package io.primer.android.phoneNumber

import io.mockk.EqMatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class PhoneNumberDescriptorFactoryTest {
    private val localConfig = mockk<PrimerConfig>()
    private val paymentMethodRemoteConfig = mockk<PaymentMethodConfigDataResponse>()
    private val paymentMethod =
        mockk<PaymentMethod> {
            every { type } returns "PAYMENT_TYPE"
        }
    private val paymentMethodCheckers = mockk<PaymentMethodCheckerRegistry>()
    private val factory = PhoneNumberDescriptorFactory()

    @Test
    fun `create() returns PhoneNumberDescriptor with correct parameters`() {
        mockkConstructor(PhoneNumberDescriptor::class)
        val result = factory.create(localConfig, paymentMethodRemoteConfig, paymentMethod, paymentMethodCheckers)

        assertIs<PhoneNumberDescriptor>(result)
        verify {
            constructedWith<PhoneNumberDescriptor>(
                EqMatcher(localConfig),
                EqMatcher(paymentMethodRemoteConfig),
                EqMatcher("PAYMENT_TYPE"),
            )
        }
        unmockkConstructor(PhoneNumberDescriptor::class)
    }
}
