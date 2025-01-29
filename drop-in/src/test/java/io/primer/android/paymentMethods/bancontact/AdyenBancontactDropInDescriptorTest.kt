@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.bancontact

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.R
import io.primer.android.assets.ui.model.Brand
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.fragments.bancontact.BancontactCardFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertIs

@ExtendWith(MockKExtension::class)
class AdyenBancontactDropInDescriptorTest {
    private val brand = mockk<Brand>(relaxed = true)

    private val brandRegistry =
        mockk<BrandRegistry> {
            every { getBrand(any()) } returns brand
        }

    @Test
    fun `paymentMethodType should return ADYEN_BANCONTACT_CARD`() {
        val descriptor = createDescriptor()

        assertEquals(PaymentMethodType.ADYEN_BANCONTACT_CARD.name, descriptor.paymentMethodType)
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when isStandalonePaymentMethod is true`() {
        mockkObject(BancontactCardFragment.Companion)
        val fragment = mockk<BancontactCardFragment>()
        every { BancontactCardFragment.newInstance() } returns fragment
        val descriptor =
            createDescriptor(isStandalonePaymentMethod = true)

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertEquals(fragment, selectedBehaviour.factory.invoke())
        assertFalse(selectedBehaviour.returnToPreviousOnBack)
        verify {
            BancontactCardFragment.newInstance()
        }
        unmockkObject(BancontactCardFragment.Companion)
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when isStandalonePaymentMethod is false`() {
        mockkObject(BancontactCardFragment.Companion)
        val fragment = mockk<BancontactCardFragment>()
        every { BancontactCardFragment.newInstance() } returns fragment
        val descriptor =
            createDescriptor(isStandalonePaymentMethod = false)

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertEquals(fragment, selectedBehaviour.factory.invoke())
        assertTrue(selectedBehaviour.returnToPreviousOnBack)
        verify {
            BancontactCardFragment.newInstance()
        }
        unmockkObject(BancontactCardFragment.Companion)
    }

    @Test
    fun `behaviours returns correct list`() {
        mockkObject(PaymentMethodLoadingFragment)
        every { PaymentMethodLoadingFragment.Companion.newInstance() } returns mockk()
        val descriptor = createDescriptor()

        val behaviour = descriptor.behaviours.single()

        assertIs<NewFragmentBehaviour>(behaviour)
        behaviour.factory.invoke()
        verify { PaymentMethodLoadingFragment.newInstance() }
        unmockkObject(PaymentMethodLoadingFragment)
    }

    @Test
    fun `loadingState returns correct LoadingState in dark mode`() {
        every { brand.iconDarkResId } returns R.drawable.ic_logo_bancontact_dark
        every { brandRegistry.getBrand(PaymentMethodType.ADYEN_BANCONTACT_CARD.name) } returns brand
        val descriptor = createDescriptor(isDarkMode = true)

        val loadingState = descriptor.loadingState

        assertEquals(R.drawable.ic_logo_bancontact_dark, loadingState.imageResIs)
        assertEquals(null, loadingState.textResId)
        assertEquals(null, loadingState.args)
        verify { brandRegistry.getBrand(PaymentMethodType.ADYEN_BANCONTACT_CARD.name) }
        verify { brand.iconDarkResId }
    }

    @Test
    fun `loadingState returns correct LoadingState in light mode`() {
        every { brand.iconResId } returns R.drawable.ic_logo_bancontact
        every { brandRegistry.getBrand(PaymentMethodType.ADYEN_BANCONTACT_CARD.name) } returns brand
        val descriptor = createDescriptor(isDarkMode = false)

        val loadingState = descriptor.loadingState

        assertEquals(R.drawable.ic_logo_bancontact, loadingState.imageResIs)
        assertEquals(null, loadingState.textResId)
        assertEquals(null, loadingState.args)
        verify { brandRegistry.getBrand(PaymentMethodType.ADYEN_BANCONTACT_CARD.name) }
        verify { brand.iconResId }
    }

    @Test
    fun `uiType should return PaymentMethodUiType FORM`() {
        val descriptor = createDescriptor()

        assertEquals(PaymentMethodUiType.FORM, descriptor.uiType)
    }

    private fun createDescriptor(
        isDarkMode: Boolean = false,
        isStandalonePaymentMethod: Boolean = false,
    ) = AdyenBancontactDropInDescriptor(
        uiOptions =
        UiOptions(
            isDarkMode = isDarkMode,
            isInitScreenEnabled = false,
            isStandalonePaymentMethod = isStandalonePaymentMethod,
        ),
        brandRegistry = brandRegistry,
    )
}
