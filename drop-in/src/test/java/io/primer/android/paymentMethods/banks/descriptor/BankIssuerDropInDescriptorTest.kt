@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.banks.descriptor

import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.fragments.bank.DotPayBankSelectionFragment
import io.primer.android.ui.fragments.bank.IdealBankSelectionFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertIs

class BankIssuerDropInDescriptorTest {
    private val defaultUiOptions =
        UiOptions(
            isStandalonePaymentMethod = true,
            isInitScreenEnabled = false,
            isDarkMode = null,
        )

    @Test
    fun `fragmentFactory returns IdealBankSelectionFragment when payment method type is ADYEN_IDEAL`() {
        val descriptor = createDescriptor(paymentMethodType = PaymentMethodType.ADYEN_IDEAL)

        assertEquals(IdealBankSelectionFragment::newInstance, descriptor.fragmentFactory)
    }

    @Test
    fun `fragmentFactory returns DotPayBankSelectionFragment when payment method type is ADYEN_IDEAL`() {
        val descriptor = createDescriptor(paymentMethodType = PaymentMethodType.ADYEN_DOTPAY)

        assertEquals(DotPayBankSelectionFragment::newInstance, descriptor.fragmentFactory)
    }

    @Test
    fun `descriptor creation throws exception when payment method type is KLARNA`() {
        assertThrows<IllegalStateException>("Unsupported payment method type KLARNA") {
            createDescriptor(paymentMethodType = PaymentMethodType.KLARNA)
        }
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when payment method type is ADYEN_IDEAL and isStandalonePaymentMethod is false`() {
        val descriptor =
            createDescriptor(
                paymentMethodType = PaymentMethodType.ADYEN_IDEAL,
                uiOptions = defaultUiOptions.copy(isStandalonePaymentMethod = false),
            )

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertTrue(selectedBehaviour.returnToPreviousOnBack)
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when payment method type is ADYEN_IDEAL and isStandalonePaymentMethod is true`() {
        val descriptor =
            createDescriptor(
                paymentMethodType = PaymentMethodType.ADYEN_IDEAL,
                uiOptions = defaultUiOptions.copy(isStandalonePaymentMethod = true),
            )

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertFalse(selectedBehaviour.returnToPreviousOnBack)
    }

    @Test
    fun `uiType returns PaymentMethodUiType FORM when payment method type is ADYEN_IDEAL`() {
        val descriptor = createDescriptor(paymentMethodType = PaymentMethodType.ADYEN_IDEAL)
        assertEquals(PaymentMethodUiType.FORM, descriptor.uiType)
    }

    @Test
    fun `behaviours returns empty list when payment method type is ADYEN_IDEAL`() {
        val descriptor = createDescriptor(paymentMethodType = PaymentMethodType.ADYEN_IDEAL)
        assertTrue(descriptor.behaviours.isEmpty())
    }

    @Test
    fun `loadingState returns null when payment method type is ADYEN_IDEAL`() {
        val descriptor = createDescriptor(paymentMethodType = PaymentMethodType.ADYEN_IDEAL)
        assertNull(descriptor.loadingState)
    }

    @Test
    fun `selectedBehaviour returns NewFragmentBehaviour when payment method type is ADYEN_DOTPAY`() {
        val descriptor = createDescriptor(paymentMethodType = PaymentMethodType.ADYEN_DOTPAY)

        assertIs<NewFragmentBehaviour>(descriptor.selectedBehaviour)
    }

    @Test
    fun `uiType returns PaymentMethodUiType FORM when payment method type is ADYEN_DOTPAY`() {
        val descriptor = createDescriptor(paymentMethodType = PaymentMethodType.ADYEN_DOTPAY)
        assertEquals(PaymentMethodUiType.FORM, descriptor.uiType)
    }

    @Test
    fun `behaviours returns empty list when payment method type is ADYEN_DOTPAY`() {
        val descriptor = createDescriptor(paymentMethodType = PaymentMethodType.ADYEN_DOTPAY)
        assertTrue(descriptor.behaviours.isEmpty())
    }

    @Test
    fun `loadingState returns null when payment method type is ADYEN_DOTPAY`() {
        val descriptor = createDescriptor(paymentMethodType = PaymentMethodType.ADYEN_DOTPAY)
        assertNull(descriptor.loadingState)
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when payment method type is ADYEN_DOTPAY and isStandalonePaymentMethod is false`() {
        val descriptor =
            createDescriptor(
                paymentMethodType = PaymentMethodType.ADYEN_DOTPAY,
                uiOptions = defaultUiOptions.copy(isStandalonePaymentMethod = false),
            )

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertTrue(selectedBehaviour.returnToPreviousOnBack)
    }

    @Test
    fun `selectedBehaviour returns correct NewFragmentBehaviour when payment method type is ADYEN_DOTPAY and isStandalonePaymentMethod is true`() {
        val descriptor =
            createDescriptor(
                paymentMethodType = PaymentMethodType.ADYEN_DOTPAY,
                uiOptions = defaultUiOptions.copy(isStandalonePaymentMethod = true),
            )

        val selectedBehaviour = descriptor.selectedBehaviour

        assertIs<NewFragmentBehaviour>(selectedBehaviour)
        assertFalse(selectedBehaviour.returnToPreviousOnBack)
    }

    private fun createDescriptor(
        paymentMethodType: PaymentMethodType,
        uiOptions: UiOptions? = null,
    ) = BankIssuerDropInDescriptor(
        paymentMethodType = paymentMethodType.name,
        uiOptions = uiOptions ?: this.defaultUiOptions,
    )
}
