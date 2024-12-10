package io.primer.android.paymentMethods.nativeUi.descriptors

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.R
import io.primer.android.assets.ui.model.Brand
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.payment.NativeUiPaymentMethodManagerCancellationBehaviour
import io.primer.android.payment.NativeUiSelectedPaymentMethodManagerBehaviour
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class NativeUiDropInDescriptorTest {

    private val paymentMethodType = "OMISE_PROMPTPAY"
    private val uiOptions = UiOptions(isDarkMode = false, isStandalonePaymentMethod = true, isInitScreenEnabled = false)
    private val primerSessionIntent = mockk<PrimerSessionIntent>()
    private val brandRegistry = mockk<BrandRegistry>()

    private val nativeUiDropInDescriptor = NativeUiDropInDescriptor(
        paymentMethodType = paymentMethodType,
        uiOptions = uiOptions,
        primerSessionIntent = primerSessionIntent,
        brandRegistry = brandRegistry
    )

    @Test
    fun `paymentMethodType returns OMISE_PROMPTPAY when payment method type is OMISE_PROMPTPAY`() {
        assertEquals(PaymentMethodType.OMISE_PROMPTPAY.name, nativeUiDropInDescriptor.paymentMethodType)
    }

    @Test
    fun `selectedBehaviour returns correct SelectedPaymentMethodManagerBehaviour`() {
        val selectedBehaviour =
            nativeUiDropInDescriptor.selectedBehaviour as NativeUiSelectedPaymentMethodManagerBehaviour

        assertEquals(paymentMethodType, selectedBehaviour.paymentMethodType)
        assertEquals(primerSessionIntent, selectedBehaviour.sessionIntent)
    }

    @Test
    fun `cancelBehaviour returns NativeUiPaymentMethodManagerCancellationBehaviour`() {
        val cancelBehaviour = nativeUiDropInDescriptor.cancelBehaviour

        assertInstanceOf(NativeUiPaymentMethodManagerCancellationBehaviour::class.java, cancelBehaviour)
    }

    @Test
    fun `behaviours returns empty list when isInitScreenEnabled is false and isStandalonePaymentMethod is true`() {
        val uiOptions = mockk<UiOptions> {
            every { isInitScreenEnabled } returns false
            every { isStandalonePaymentMethod } returns true
        }

        val descriptor = NativeUiDropInDescriptor(
            paymentMethodType = paymentMethodType,
            uiOptions = uiOptions,
            primerSessionIntent = primerSessionIntent,
            brandRegistry = brandRegistry
        )

        assertEquals(emptyList<PaymentMethodBehaviour>(), descriptor.behaviours)
    }

    @Test
    fun `behaviours returns list with NewFragmentBehaviour when isInitScreenEnabled is true or isStandalonePaymentMethod is false`() {
        mockkObject(PaymentMethodLoadingFragment)
        every { PaymentMethodLoadingFragment.Companion.newInstance(popBackStackToRoot = true) } returns mockk()
        val uiOptions = mockk<UiOptions> {
            every { isInitScreenEnabled } returns true
            every { isStandalonePaymentMethod } returns false
        }

        val descriptor = NativeUiDropInDescriptor(
            paymentMethodType = paymentMethodType,
            uiOptions = uiOptions,
            primerSessionIntent = primerSessionIntent,
            brandRegistry = brandRegistry
        )

        val behaviour = descriptor.behaviours.single()
        assertIs<NewFragmentBehaviour>(behaviour)
        behaviour.factory.invoke()
        verify { PaymentMethodLoadingFragment.newInstance(popBackStackToRoot = true) }
        unmockkObject(PaymentMethodLoadingFragment)
    }

    @Test
    fun `uiType returns correct PaymentMethodUiType`() {
        val uiType = nativeUiDropInDescriptor.uiType

        assertEquals(PaymentMethodUiType.SIMPLE_BUTTON, uiType)
    }

    @Test
    fun `loadingState returns correct LoadingState`() {
        val brand = mockk<Brand>()
        every { brand.logoResId } returns R.drawable.ic_logo_promptpay_light
        every { brandRegistry.getBrand(paymentMethodType) } returns brand

        val loadingState = nativeUiDropInDescriptor.loadingState

        verify { brandRegistry.getBrand(paymentMethodType) }
        assertEquals(R.drawable.ic_logo_promptpay_light, loadingState.imageResIs)
    }
}
