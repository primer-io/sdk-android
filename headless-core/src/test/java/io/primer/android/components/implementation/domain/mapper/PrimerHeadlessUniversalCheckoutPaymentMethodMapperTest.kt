package io.primer.android.components.implementation.domain.mapper

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.implementation.domain.PaymentMethodDescriptorsRepository
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.VaultCapability
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PrimerHeadlessUniversalCheckoutPaymentMethodMapperTest {
    private val paymentMethodDescriptorsRepository = mockk<PaymentMethodDescriptorsRepository>()
    private val mapper = PrimerHeadlessUniversalCheckoutPaymentMethodMapper(paymentMethodDescriptorsRepository)

    @Test
    fun `getPrimerHeadlessUniversalCheckoutPaymentMethod() returns correct payment method when payment method is supported and vault capability is SINGLE_USE_AND_VAULT`() {
        val paymentMethodType = PaymentMethodType.STRIPE_ACH.name
        val headlessDefinition =
            mockk<HeadlessDefinition> {
                every { paymentMethodManagerCategories } returns listOf(PrimerPaymentMethodManagerCategory.STRIPE_ACH)
                every { rawDataDefinition } returns null
            }
        val descriptor =
            mockk<PaymentMethodDescriptor> {
                every { config.type } returns paymentMethodType
                every { config.name } returns "Stripe ACH"
                every { vaultCapability } returns VaultCapability.SINGLE_USE_AND_VAULT
                every { this@mockk.headlessDefinition } returns headlessDefinition
            }
        every { paymentMethodDescriptorsRepository.getPaymentMethodDescriptors() } returns listOf(descriptor)

        val result = mapper.getPrimerHeadlessUniversalCheckoutPaymentMethod(paymentMethodType)

        assertEquals(paymentMethodType, result.paymentMethodType)
        assertEquals("Stripe ACH", result.paymentMethodName)
        assertEquals(
            listOf(PrimerSessionIntent.CHECKOUT, PrimerSessionIntent.VAULT),
            result.supportedPrimerSessionIntents,
        )
        assertEquals(listOf(PrimerPaymentMethodManagerCategory.STRIPE_ACH), result.paymentMethodManagerCategories)
        verify {
            paymentMethodDescriptorsRepository.getPaymentMethodDescriptors()
            descriptor.headlessDefinition
            descriptor.config.type
            descriptor.config.name
            descriptor.vaultCapability
            headlessDefinition.paymentMethodManagerCategories
        }
    }

    @Test
    fun `getPrimerHeadlessUniversalCheckoutPaymentMethod() returns correct payment method when payment method is supported and vault capability is VAULT_ONLY`() {
        val paymentMethodType = PaymentMethodType.STRIPE_ACH.name
        val headlessDefinition =
            mockk<HeadlessDefinition> {
                every { paymentMethodManagerCategories } returns listOf(PrimerPaymentMethodManagerCategory.STRIPE_ACH)
                every { rawDataDefinition } returns null
            }
        val descriptor =
            mockk<PaymentMethodDescriptor> {
                every { config.type } returns paymentMethodType
                every { config.name } returns "Stripe ACH"
                every { vaultCapability } returns VaultCapability.VAULT_ONLY
                every { this@mockk.headlessDefinition } returns headlessDefinition
            }
        every { paymentMethodDescriptorsRepository.getPaymentMethodDescriptors() } returns listOf(descriptor)

        val result = mapper.getPrimerHeadlessUniversalCheckoutPaymentMethod(paymentMethodType)

        assertEquals(paymentMethodType, result.paymentMethodType)
        assertEquals("Stripe ACH", result.paymentMethodName)
        assertEquals(
            listOf(PrimerSessionIntent.VAULT),
            result.supportedPrimerSessionIntents,
        )
        assertEquals(listOf(PrimerPaymentMethodManagerCategory.STRIPE_ACH), result.paymentMethodManagerCategories)
        verify {
            paymentMethodDescriptorsRepository.getPaymentMethodDescriptors()
            descriptor.headlessDefinition
            descriptor.config.type
            descriptor.config.name
            descriptor.vaultCapability
            headlessDefinition.paymentMethodManagerCategories
        }
    }

    @Test
    fun `getPrimerHeadlessUniversalCheckoutPaymentMethod() returns correct payment method when payment method is supported and vault capability is SINGLE_USE_ONLY`() {
        val paymentMethodType = PaymentMethodType.STRIPE_ACH.name
        val headlessDefinition =
            mockk<HeadlessDefinition> {
                every { paymentMethodManagerCategories } returns listOf(PrimerPaymentMethodManagerCategory.STRIPE_ACH)
                every { rawDataDefinition } returns null
            }
        val descriptor =
            mockk<PaymentMethodDescriptor> {
                every { config.type } returns paymentMethodType
                every { config.name } returns "Stripe ACH"
                every { vaultCapability } returns VaultCapability.SINGLE_USE_ONLY
                every { this@mockk.headlessDefinition } returns headlessDefinition
            }
        every { paymentMethodDescriptorsRepository.getPaymentMethodDescriptors() } returns listOf(descriptor)

        val result = mapper.getPrimerHeadlessUniversalCheckoutPaymentMethod(paymentMethodType)

        assertEquals(paymentMethodType, result.paymentMethodType)
        assertEquals("Stripe ACH", result.paymentMethodName)
        assertEquals(
            listOf(PrimerSessionIntent.CHECKOUT),
            result.supportedPrimerSessionIntents,
        )
        assertEquals(listOf(PrimerPaymentMethodManagerCategory.STRIPE_ACH), result.paymentMethodManagerCategories)
        verify {
            paymentMethodDescriptorsRepository.getPaymentMethodDescriptors()
            descriptor.headlessDefinition
            descriptor.config.type
            descriptor.config.name
            descriptor.vaultCapability
            headlessDefinition.paymentMethodManagerCategories
        }
    }

    @Test
    fun `getPrimerHeadlessUniversalCheckoutPaymentMethod() returns correct payment method when payment method is supported and defines rawDataDefinition`() {
        val paymentMethodType = PaymentMethodType.PAYMENT_CARD.name
        val headlessDefinition =
            mockk<HeadlessDefinition> {
                every { paymentMethodManagerCategories } returns listOf(PrimerPaymentMethodManagerCategory.RAW_DATA)
                every { rawDataDefinition } returns
                    mockk<HeadlessDefinition.RawDataDefinition> {
                        every { requiredInputDataClass } returns PrimerCardData::class
                    }
            }
        val descriptor =
            mockk<PaymentMethodDescriptor> {
                every { config.type } returns paymentMethodType
                every { config.name } returns "Payment Card"
                every { vaultCapability } returns VaultCapability.SINGLE_USE_ONLY
                every { this@mockk.headlessDefinition } returns headlessDefinition
            }
        every { paymentMethodDescriptorsRepository.getPaymentMethodDescriptors() } returns listOf(descriptor)

        val result = mapper.getPrimerHeadlessUniversalCheckoutPaymentMethod(paymentMethodType)

        assertEquals(paymentMethodType, result.paymentMethodType)
        assertEquals("Payment Card", result.paymentMethodName)
        assertEquals(
            listOf(PrimerSessionIntent.CHECKOUT),
            result.supportedPrimerSessionIntents,
        )
        assertEquals(listOf(PrimerPaymentMethodManagerCategory.RAW_DATA), result.paymentMethodManagerCategories)
        assertEquals(PrimerCardData::class, result.requiredInputDataClass)

        verify {
            paymentMethodDescriptorsRepository.getPaymentMethodDescriptors()
            descriptor.headlessDefinition
            descriptor.config.type
            descriptor.config.name
            descriptor.vaultCapability
            headlessDefinition.paymentMethodManagerCategories
            headlessDefinition.rawDataDefinition
        }
    }

    @Test
    fun `getPrimerHeadlessUniversalCheckoutPaymentMethod() throws IllegalStateException when payment method is supported but headless definition is null`() {
        val paymentMethodType = PaymentMethodType.STRIPE_ACH.name
        val descriptor =
            mockk<PaymentMethodDescriptor> {
                every { config.type } returns paymentMethodType
                every { config.name } returns "Stripe ACH"
                every { vaultCapability } returns VaultCapability.SINGLE_USE_ONLY
                every { this@mockk.headlessDefinition } returns null
            }
        every { paymentMethodDescriptorsRepository.getPaymentMethodDescriptors() } returns listOf(descriptor)

        assertThrows<IllegalStateException> {
            mapper.getPrimerHeadlessUniversalCheckoutPaymentMethod(paymentMethodType)
        }

        verify {
            paymentMethodDescriptorsRepository.getPaymentMethodDescriptors()
            descriptor.headlessDefinition
            descriptor.config.type
        }
        verify(exactly = 0) {
            descriptor.vaultCapability
        }
    }

    @Test
    fun `getPrimerHeadlessUniversalCheckoutPaymentMethod() throws UnsupportedPaymentMethodException when payment method is unsupported`() {
        every { paymentMethodDescriptorsRepository.getPaymentMethodDescriptors() } returns emptyList()

        val paymentMethodType = PaymentMethodType.STRIPE_ACH.name

        assertThrows<UnsupportedPaymentMethodException> {
            mapper.getPrimerHeadlessUniversalCheckoutPaymentMethod(paymentMethodType)
        }
    }
}
