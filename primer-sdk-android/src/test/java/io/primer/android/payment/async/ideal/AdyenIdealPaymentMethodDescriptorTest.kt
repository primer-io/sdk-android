@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.payment.async.ideal

import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SDKCapability
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AdyenIdealPaymentMethodDescriptorTest {

    private val descriptor = AdyenIdealPaymentMethodDescriptor(
        options = mockk(),
        localConfig = mockk(relaxed = true),
        config = mockk(relaxed = true)
    )

    @Test
    fun `behaviours should contain AsyncPaymentMethodBehaviour`() {
        assertEquals(
            expected = 1,
            actual = descriptor.behaviours.size
        )
        assert(descriptor.behaviours.single() is AsyncPaymentMethodBehaviour)
    }

    @Test
    fun `sdkCapabilities should contain DROP_IN and HEADLESS`() {
        assertEquals(
            expected = listOf(SDKCapability.DROP_IN, SDKCapability.HEADLESS),
            actual = descriptor.sdkCapabilities
        )
    }

    @Test
    fun `type should should be FORM`() {
        assertEquals(
            expected = PaymentMethodUiType.FORM,
            actual = descriptor.type
        )
    }

    @Test
    fun `headlessDefinition should contain FORM_WITH_REDIRECT payment method manager category`() {
        assertEquals(
            expected = listOf(PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT),
            actual = descriptor.headlessDefinition.paymentMethodManagerCategories
        )
    }
}
