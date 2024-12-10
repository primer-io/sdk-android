package io.primer.android.paymentMethods.stripe.ach.descriptors

import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class StripeAchDropInDescriptorTest {
    private val paymentMethodType = PaymentMethodType.STRIPE_ACH.name

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when isStandalonePaymentMethod is true`() {
        val descriptor = createDescriptor(isStandalonePaymentMethod = true)
        assertIs<NewFragmentBehaviour>(descriptor.selectedBehaviour)
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when isStandalonePaymentMethod is false`() {
        val descriptor = createDescriptor(isStandalonePaymentMethod = false)
        assertIs<NewFragmentBehaviour>(descriptor.selectedBehaviour)
    }

    @Test
    fun `uiType returns PaymentMethodUiType FORM`() {
        val descriptor = createDescriptor()
        assertEquals(PaymentMethodUiType.FORM, descriptor.uiType)
    }

    @Test
    fun `behaviours returns empty list`() {
        val descriptor = createDescriptor()
        assertTrue(descriptor.behaviours.isEmpty())
    }

    @Test
    fun `loadingState returns null`() {
        val descriptor = createDescriptor()
        assertNull(descriptor.loadingState)
    }

    private fun createDescriptor(isStandalonePaymentMethod: Boolean = false) = StripeAchDropInDescriptor(
        paymentMethodType = paymentMethodType,
        uiOptions = UiOptions(
            isStandalonePaymentMethod = isStandalonePaymentMethod,
            isInitScreenEnabled = false,
            isDarkMode = null
        )
    )
}
