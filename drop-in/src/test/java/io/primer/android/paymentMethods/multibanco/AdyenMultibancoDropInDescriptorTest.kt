@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.multibanco

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.R
import io.primer.android.assets.ui.model.Brand
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.fragments.SuccessType
import io.primer.android.ui.fragments.multibanko.MultibancoConditionsFragment
import io.primer.android.ui.fragments.multibanko.MultibancoPaymentFragment
import io.primer.android.viewmodel.ViewStatus
import io.primer.android.vouchers.multibanco.MultibancoCheckoutAdditionalInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class AdyenMultibancoDropInDescriptorTest {
    private val brand = mockk<Brand>(relaxed = true)
    private val brandRegistry =
        mockk<BrandRegistry> {
            every { getBrand(any()) } returns brand
        }

    @Test
    fun `paymentMethodType returns ADYEN_MULTIBANCO`() {
        val descriptor = createDescriptor()

        assertEquals(PaymentMethodType.ADYEN_MULTIBANCO.name, descriptor.paymentMethodType)
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when isStandalonePaymentMethod is true and session intent is CHECKOUT`() {
        mockkObject(MultibancoConditionsFragment.Companion)
        val fragment = mockk<MultibancoConditionsFragment>()
        every { MultibancoConditionsFragment.newInstance(any(), any()) } returns fragment
        val descriptor =
            createDescriptor(isStandalonePaymentMethod = true, sessionIntent = PrimerSessionIntent.CHECKOUT)

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertEquals(fragment, selectedBehaviour.factory.invoke())
        assertFalse(selectedBehaviour.returnToPreviousOnBack)
        verify {
            MultibancoConditionsFragment.newInstance(
                sessionIntent = PrimerSessionIntent.CHECKOUT,
                paymentMethodType = PaymentMethodType.ADYEN_MULTIBANCO.name,
            )
        }
        unmockkObject(MultibancoConditionsFragment.Companion)
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when isStandalonePaymentMethod is false and session intent is CHECKOUT`() {
        mockkObject(MultibancoConditionsFragment.Companion)
        val fragment = mockk<MultibancoConditionsFragment>()
        every { MultibancoConditionsFragment.newInstance(any(), any()) } returns fragment
        val descriptor =
            createDescriptor(isStandalonePaymentMethod = false, sessionIntent = PrimerSessionIntent.CHECKOUT)

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertEquals(fragment, selectedBehaviour.factory.invoke())
        assertTrue(selectedBehaviour.returnToPreviousOnBack)
        verify {
            MultibancoConditionsFragment.newInstance(
                sessionIntent = PrimerSessionIntent.CHECKOUT,
                paymentMethodType = PaymentMethodType.ADYEN_MULTIBANCO.name,
            )
        }
        unmockkObject(MultibancoConditionsFragment.Companion)
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when session intent is VAULT`() {
        mockkObject(MultibancoConditionsFragment.Companion)
        val fragment = mockk<MultibancoConditionsFragment>()
        every { MultibancoConditionsFragment.newInstance(any(), any()) } returns fragment
        val descriptor = createDescriptor(sessionIntent = PrimerSessionIntent.VAULT)

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertEquals(fragment, selectedBehaviour.factory.invoke())
        assertTrue(selectedBehaviour.returnToPreviousOnBack)
        verify {
            MultibancoConditionsFragment.newInstance(
                sessionIntent = PrimerSessionIntent.VAULT,
                paymentMethodType = PaymentMethodType.ADYEN_MULTIBANCO.name,
            )
        }
        unmockkObject(MultibancoConditionsFragment.Companion)
    }

    @Test
    fun `createSuccessBehavior() returns correct NewFragmentBehaviour`() {
        mockkObject(MultibancoPaymentFragment.Companion)
        val entity = "entity"
        val reference = "reference"
        val expiresAt = "expiresAt"
        val fragment = mockk<MultibancoPaymentFragment>()
        every { MultibancoPaymentFragment.newInstance(any(), any(), any()) } returns fragment
        val descriptor = createDescriptor()

        val successBehavior =
            descriptor.createSuccessBehavior(
                ViewStatus.ShowSuccess(
                    successType = SuccessType.PAYMENT_SUCCESS,
                    checkoutAdditionalInfo =
                        MultibancoCheckoutAdditionalInfo(
                            expiresAt = expiresAt,
                            reference = reference,
                            entity = entity,
                        ),
                ),
            )

        assertIs<NewFragmentBehaviour>(successBehavior)
        assertEquals(fragment, successBehavior.factory.invoke())
        assertFalse(successBehavior.returnToPreviousOnBack)
        verify {
            MultibancoPaymentFragment.newInstance(entity = entity, reference = reference, expiresAt = expiresAt)
        }
        unmockkObject(MultibancoPaymentFragment.Companion)
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
        every { brand.iconDarkResId } returns R.drawable.ic_logo_multibanco_dark
        every { brandRegistry.getBrand(PaymentMethodType.ADYEN_MULTIBANCO.name) } returns brand
        val descriptor = createDescriptor(isDarkMode = true)

        val loadingState = descriptor.loadingState

        assertEquals(R.drawable.ic_logo_multibanco_dark, loadingState.imageResIs)
        assertEquals(null, loadingState.textResId)
        assertEquals(null, loadingState.args)
        verify { brandRegistry.getBrand(PaymentMethodType.ADYEN_MULTIBANCO.name) }
        verify { brand.iconDarkResId }
    }

    @Test
    fun `loadingState returns correct LoadingState in light mode`() {
        every { brand.iconResId } returns R.drawable.ic_logo_multibanco_light
        every { brandRegistry.getBrand(PaymentMethodType.ADYEN_MULTIBANCO.name) } returns brand
        val descriptor = createDescriptor(isDarkMode = false)

        val loadingState = descriptor.loadingState

        assertEquals(R.drawable.ic_logo_multibanco_light, loadingState.imageResIs)
        assertEquals(null, loadingState.textResId)
        assertEquals(null, loadingState.args)
        verify { brandRegistry.getBrand(PaymentMethodType.ADYEN_MULTIBANCO.name) }
        verify { brand.iconResId }
    }

    @Test
    fun `uiType returns correct PaymentMethodUiType`() {
        val descriptor = createDescriptor()

        assertEquals(PaymentMethodUiType.FORM, descriptor.uiType)
    }

    private fun createDescriptor(
        isDarkMode: Boolean = false,
        isStandalonePaymentMethod: Boolean = false,
        sessionIntent: PrimerSessionIntent = PrimerSessionIntent.CHECKOUT,
    ) = AdyenMultibancoDropInDescriptor(
        uiOptions =
            UiOptions(
                isDarkMode = isDarkMode,
                isInitScreenEnabled = false,
                isStandalonePaymentMethod = isStandalonePaymentMethod,
            ),
        brandRegistry = brandRegistry,
        sessionIntent = sessionIntent,
    )
}
