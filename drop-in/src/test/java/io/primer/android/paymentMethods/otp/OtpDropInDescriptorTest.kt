@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.otp

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.R
import io.primer.android.assets.ui.model.Brand
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.fragments.forms.DynamicFormFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertIs

class OtpDropInDescriptorTest {
    private val brand = mockk<Brand>(relaxed = true)

    @Test
    fun `paymentMethodType returns ADYEN_BLIK when payment method type is ADYEN_BLIK`() {
        val descriptor = createDescriptor(paymentMethodType = PaymentMethodType.ADYEN_BLIK)

        assertEquals(PaymentMethodType.ADYEN_BLIK.name, descriptor.paymentMethodType)
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when payment method type is ADYEN_BLIK`() {
        mockkObject(DynamicFormFragment.Companion)
        val fragment = mockk<DynamicFormFragment>()
        every { DynamicFormFragment.newInstance() } returns fragment
        val descriptor =
            createDescriptor(
                paymentMethodType = PaymentMethodType.ADYEN_BLIK,
                isStandalonePaymentMethod = true,
            )

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertEquals(fragment, selectedBehaviour.factory.invoke())
        assertFalse(selectedBehaviour.returnToPreviousOnBack)
        verify {
            DynamicFormFragment.newInstance()
        }
        unmockkObject(DynamicFormFragment.Companion)
    }

    @Test
    fun `fragmentFactory throws error for unsupported payment method type`() {
        assertThrows<IllegalStateException> {
            createDescriptor(PaymentMethodType.XENDIT_OVO)
        }
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when isStandalonePaymentMethod is true`() {
        val descriptor =
            createDescriptor(
                paymentMethodType = PaymentMethodType.ADYEN_BLIK,
                isStandalonePaymentMethod = true,
            )

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertFalse(selectedBehaviour.returnToPreviousOnBack)
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when isStandalonePaymentMethod is false`() {
        val descriptor =
            createDescriptor(
                paymentMethodType = PaymentMethodType.ADYEN_BLIK,
                isStandalonePaymentMethod = false,
            )

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertTrue(selectedBehaviour.returnToPreviousOnBack)
    }

    @Test
    fun `behaviours returns correct list`() {
        val descriptor = createDescriptor(paymentMethodType = PaymentMethodType.ADYEN_BLIK)

        val behaviours = descriptor.behaviours

        assertEquals(emptyList<PaymentMethodBehaviour>(), behaviours)
    }

    @Test
    fun `loadingState returns correct LoadingState when payment method type is ADYEN_BLIK`() {
        every { brand.iconResId } returns R.drawable.ic_logo_blik_square
        val descriptor =
            createDescriptor(
                paymentMethodType = PaymentMethodType.ADYEN_BLIK,
                isDarkMode = false,
            )

        val loadingState = descriptor.loadingState

        assertEquals(R.drawable.ic_logo_blik_square, loadingState.imageResIs)
        assertEquals(R.string.payment_method_blik_loading_placeholder, loadingState.textResId)
    }

    @Test
    fun `uiType returns correct PaymentMethodUiType when payment method type is ADYEN_BLIK`() {
        val descriptor =
            createDescriptor(
                paymentMethodType = PaymentMethodType.ADYEN_BLIK,
            )

        assertEquals(PaymentMethodUiType.FORM, descriptor.uiType)
    }

    private fun createDescriptor(
        paymentMethodType: PaymentMethodType,
        isDarkMode: Boolean = false,
        isStandalonePaymentMethod: Boolean = false,
    ) = OtpDropInDescriptor(
        paymentMethodType = paymentMethodType.name,
        uiOptions =
            UiOptions(
                isDarkMode = isDarkMode,
                isInitScreenEnabled = false,
                isStandalonePaymentMethod = isStandalonePaymentMethod,
            ),
    )
}
