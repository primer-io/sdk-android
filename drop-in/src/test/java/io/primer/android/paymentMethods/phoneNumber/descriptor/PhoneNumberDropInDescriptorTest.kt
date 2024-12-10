@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.phoneNumber.descriptor

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.R
import io.primer.android.assets.ui.model.Brand
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.PaymentMethodBehaviour
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

class PhoneNumberDropInDescriptorTest {
    private val brand = mockk<Brand>(relaxed = true)
    private val brandRegistry = mockk<BrandRegistry> {
        every { getBrand(any()) } returns brand
    }

    @Test
    fun `paymentMethodType returns ADYEN_MBWAY when payment method type is ADYEN_MBWAY`() {
        val descriptor = createDescriptor(paymentMethodType = PaymentMethodType.ADYEN_MBWAY)

        assertEquals(PaymentMethodType.ADYEN_MBWAY.name, descriptor.paymentMethodType)
    }

//    @Test
//    fun `fragmentFactory returns DynamicFormFragment when payment method type is XENDIT_OVO`() {
//        val descriptor = createDescriptor(PaymentMethodType.XENDIT_OVO)
//
//        assertEquals(DynamicFormFragment::newInstance, descriptor.fragmentFactory)
//    }

    @Test
    fun `fragmentFactory returns DynamicFormFragment when payment method type is ADYEN_MBWAY`() {
        val descriptor = createDescriptor(PaymentMethodType.ADYEN_MBWAY)

        assertEquals(DynamicFormFragment::newInstance, descriptor.fragmentFactory)
    }

    @Test
    fun `fragmentFactory throws error for unsupported payment method type`() {
        assertThrows<IllegalStateException> {
            createDescriptor(PaymentMethodType.XENDIT_OVO)
        }
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when isStandalonePaymentMethod is true`() {
        val descriptor = createDescriptor(
            paymentMethodType = PaymentMethodType.ADYEN_MBWAY,
            isStandalonePaymentMethod = true
        )

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertFalse(selectedBehaviour.returnToPreviousOnBack)
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when isStandalonePaymentMethod is false`() {
        val descriptor = createDescriptor(
            paymentMethodType = PaymentMethodType.ADYEN_MBWAY,
            isStandalonePaymentMethod = false
        )

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertTrue(selectedBehaviour.returnToPreviousOnBack)
    }

    @Test
    fun `behaviours returns correct list`() {
        val descriptor = createDescriptor(paymentMethodType = PaymentMethodType.ADYEN_MBWAY)

        val behaviours = descriptor.behaviours

        assertEquals(emptyList<PaymentMethodBehaviour>(), behaviours)
    }

    @Test
    fun `loadingState returns correct LoadingState in dark mode when payment method type is ADYEN_MBWAY`() {
        every { brand.iconDarkResId } returns R.drawable.ic_logo_mbway_dark
        every { brandRegistry.getBrand(PaymentMethodType.ADYEN_MBWAY.name) } returns brand
        val descriptor = createDescriptor(
            paymentMethodType = PaymentMethodType.ADYEN_MBWAY,
            isDarkMode = true
        )

        val loadingState = descriptor.loadingState

        assertEquals(R.drawable.ic_logo_mbway_dark, loadingState.imageResIs)
        assertEquals(R.string.completeYourPaymentInTheApp, loadingState.textResId)
        assertEquals(PaymentMethodType.ADYEN_MBWAY.name, loadingState.args)
        verify { brandRegistry.getBrand(PaymentMethodType.ADYEN_MBWAY.name) }
        verify { brand.iconDarkResId }
    }

    @Test
    fun `loadingState returns correct LoadingState in light mode when payment method type is ADYEN_MBWAY`() {
        every { brand.iconResId } returns R.drawable.ic_logo_mbway_light
        every { brandRegistry.getBrand(PaymentMethodType.ADYEN_MBWAY.name) } returns brand
        val descriptor = createDescriptor(
            paymentMethodType = PaymentMethodType.ADYEN_MBWAY,
            isDarkMode = false
        )

        val loadingState = descriptor.loadingState

        assertEquals(R.drawable.ic_logo_mbway_light, loadingState.imageResIs)
        assertEquals(R.string.completeYourPaymentInTheApp, loadingState.textResId)
        assertEquals(PaymentMethodType.ADYEN_MBWAY.name, loadingState.args)
        verify { brandRegistry.getBrand(PaymentMethodType.ADYEN_MBWAY.name) }
        verify { brand.iconResId }
    }

//    @Test
//    fun `loadingState returns correct LoadingState in dark mode when payment method type is XENDIT_OVO`() {
//        val descriptor = createDescriptor(
//            paymentMethodType = PaymentMethodType.XENDIT_OVO,
//            isDarkMode = true
//        )
//
//        val loadingState = descriptor.loadingState
//
//        assertEquals(R.drawable.ic_logo_mbway_dark, loadingState.imageResIs)
//        assertEquals(R.string.completeYourPaymentInTheApp, loadingState.textResId)
//        assertEquals(PaymentMethodType.XENDIT_OVO.name, loadingState.args)
//    }

//    @Test
//    fun `loadingState returns correct LoadingState in light mode when payment method type is XENDIT_OVO`() {
//        val descriptor = createDescriptor(
//            paymentMethodType = PaymentMethodType.XENDIT_OVO,
//            isDarkMode = false
//        )
//
//        val loadingState = descriptor.loadingState
//
//        assertEquals(R.drawable.ic_logo_mbway_light, loadingState.imageResIs)
//        assertEquals(R.string.completeYourPaymentInTheApp, loadingState.textResId)
//        assertEquals(PaymentMethodType.XENDIT_OVO.name, loadingState.args)
//    }

    @Test
    fun `uiType returns correct PaymentMethodUiType when payment method type is ADYEN_MBWAY`() {
        val descriptor = createDescriptor(
            paymentMethodType = PaymentMethodType.ADYEN_MBWAY
        )

        assertEquals(PaymentMethodUiType.FORM, descriptor.uiType)
    }

//    @Test
//    fun `uiType returns correct PaymentMethodUiType when payment method type is XENDIT_OVO`() {
//        val descriptor = createDescriptor(
//            paymentMethodType = PaymentMethodType.XENDIT_OVO
//        )
//
//        assertEquals(PaymentMethodUiType.FORM, descriptor.uiType)
//    }

    private fun createDescriptor(
        paymentMethodType: PaymentMethodType,
        isDarkMode: Boolean = false,
        isStandalonePaymentMethod: Boolean = false
    ) = PhoneNumberDropInDescriptor(
        paymentMethodType = paymentMethodType.name,
        uiOptions = UiOptions(
            isDarkMode = isDarkMode,
            isInitScreenEnabled = false,
            isStandalonePaymentMethod = isStandalonePaymentMethod
        ),
        brandRegistry = brandRegistry,
        paymentMethodName = paymentMethodType.name
    )
}
