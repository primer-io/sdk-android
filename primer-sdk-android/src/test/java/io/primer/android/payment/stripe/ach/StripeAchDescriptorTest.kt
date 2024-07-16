package io.primer.android.payment.stripe.ach

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.payments.additionalInfo.AchAdditionalInfoResolver
import io.primer.android.domain.payments.create.repository.PaymentResultRepository
import io.primer.android.events.EventDispatcher
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.VaultCapability
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertIs

@ExtendWith(MockKExtension::class)
class StripeAchDescriptorTest {
    @RelaxedMockK
    private lateinit var localConfig: PrimerConfig

    @RelaxedMockK
    private lateinit var config: PaymentMethodConfigDataResponse

    @MockK
    private lateinit var eventDispatcher: EventDispatcher

    @MockK
    private lateinit var paymentResultRepository: PaymentResultRepository

    @MockK
    private lateinit var checkoutErrorEventResolver: BaseErrorEventResolver

    @MockK
    private lateinit var completeStripeAchPaymentSessionDelegate: CompleteStripeAchPaymentSessionDelegate

    @MockK
    private lateinit var stripeAchMandateTimestampLoggingDelegate: StripeAchMandateTimestampLoggingDelegate

    @InjectMockKs
    private lateinit var stripeAchDescriptor: StripeAchDescriptor

    @Test
    fun `selectedBehaviour should return NewFragmentBehaviour when isStandalonePaymentMethod is true`() {
        every { localConfig.isStandalonePaymentMethod } returns true
        assertIs<NewFragmentBehaviour>(stripeAchDescriptor.selectedBehaviour)
    }

    @Test
    fun `selectedBehaviour should return NewFragmentBehaviour when isStandalonePaymentMethod is false`() {
        every { localConfig.isStandalonePaymentMethod } returns false
        assertIs<NewFragmentBehaviour>(stripeAchDescriptor.selectedBehaviour)
    }

    @Test
    fun `type should return FORM`() {
        assertEquals(PaymentMethodUiType.FORM, stripeAchDescriptor.type)
    }

    @Test
    fun `additionalInfoResolver should return AchAdditionalInfoResolver`() {
        assertIs<AchAdditionalInfoResolver>(stripeAchDescriptor.additionalInfoResolver)
    }

    @Test
    fun `vaultCapability should return SINGLE_USE_ONLY`() {
        assertEquals(VaultCapability.SINGLE_USE_ONLY, stripeAchDescriptor.vaultCapability)
    }

    @Test
    fun `headlessDefinition should return list with NATIVE_UI`() {
        assertEquals(
            listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI),
            stripeAchDescriptor.headlessDefinition.paymentMethodManagerCategories
        )
    }
}
