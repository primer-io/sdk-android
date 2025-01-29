@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.paypal

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.assets.ui.model.Brand
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.LoadingState
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertIs

@ExtendWith(MockKExtension::class)
class TestPayPalDropInPaymentMethodDescriptorPrimerSandbox {
    @MockK
    private lateinit var brand: Brand

    private val paymentMethodType = PaymentMethodType.PRIMER_TEST_PAYPAL.name

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
    fun `uiType returns PaymentMethodUiType SIMPLE_BUTTON`() {
        val descriptor = createDescriptor()
        assertEquals(PaymentMethodUiType.SIMPLE_BUTTON, descriptor.uiType)
    }

    @Test
    fun `behaviours returns empty list when isInitScreenEnabled is false and isStandalonePaymentMethod is true`() {
        val descriptor = createDescriptor(isStandalonePaymentMethod = true, isInitScreenEnabled = false)
        assertTrue(descriptor.behaviours.isEmpty())
    }

    @Test
    fun `behaviours returns correct list when isInitScreenEnabled is true and isStandalonePaymentMethod is true`() {
        mockkObject(PaymentMethodLoadingFragment)
        every { PaymentMethodLoadingFragment.Companion.newInstance() } returns mockk()
        val descriptor = createDescriptor(isStandalonePaymentMethod = true, isInitScreenEnabled = true)
        val behaviour = descriptor.behaviours.single()

        assertIs<NewFragmentBehaviour>(behaviour)
        behaviour.factory.invoke()
        verify { PaymentMethodLoadingFragment.newInstance() }
        unmockkObject(PaymentMethodLoadingFragment)
    }

    @Test
    fun `behaviours returns correct list when isInitScreenEnabled is false and isStandalonePaymentMethod is false`() {
        mockkObject(PaymentMethodLoadingFragment)
        every { PaymentMethodLoadingFragment.Companion.newInstance() } returns mockk()
        val descriptor = createDescriptor(isStandalonePaymentMethod = false, isInitScreenEnabled = false)
        val behaviour = descriptor.behaviours.single()

        assertIs<NewFragmentBehaviour>(behaviour)
        behaviour.factory.invoke()
        verify { PaymentMethodLoadingFragment.newInstance() }
        unmockkObject(PaymentMethodLoadingFragment)
    }

    @Test
    fun `behaviours returns correct list when isInitScreenEnabled is true and isStandalonePaymentMethod is false`() {
        mockkObject(PaymentMethodLoadingFragment)
        every { PaymentMethodLoadingFragment.Companion.newInstance() } returns mockk()
        val descriptor = createDescriptor(isStandalonePaymentMethod = false, isInitScreenEnabled = true)
        val behaviour = descriptor.behaviours.single()

        assertIs<NewFragmentBehaviour>(behaviour)
        behaviour.factory.invoke()
        verify { PaymentMethodLoadingFragment.newInstance() }
        unmockkObject(PaymentMethodLoadingFragment)
    }

    @Test
    fun `loadingState returns loading state with brand logo`() {
        every { brand.logoResId } returns 1
        val descriptor = createDescriptor()
        assertEquals(LoadingState(1), descriptor.loadingState)
    }

    private fun createDescriptor(
        isStandalonePaymentMethod: Boolean = false,
        isInitScreenEnabled: Boolean = false,
    ) = TestPayPalDropInPaymentMethodDescriptor(
        paymentMethodType = paymentMethodType,
        uiOptions =
        UiOptions(
            isStandalonePaymentMethod = isStandalonePaymentMethod,
            isInitScreenEnabled = isInitScreenEnabled,
            isDarkMode = null,
        ),
        brand = brand,
    )
}
