package io.primer.android.googlepay.implementation.validation

import io.mockk.every
import io.mockk.mockk
import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingOptionIdParams
import io.primer.android.configuration.data.model.ShippingMethod
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.googlepay.implementation.configuration.data.repository.GooglePayConfigurationDataRepository
import io.primer.android.googlepay.implementation.configuration.domain.model.GooglePayConfiguration
import io.primer.android.googlepay.implementation.errors.domain.exception.ShippingAddressUnserviceableException
import io.primer.android.paymentmethods.core.configuration.domain.model.NoOpPaymentMethodConfigurationParams
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private const val VALID_ID = "valid_id"

internal class GooglePayShippingMethodUpdateValidatorTest {
    private val validUpdate = ActionUpdateShippingOptionIdParams(VALID_ID)

    private val configurationRepository = mockk<GooglePayConfigurationDataRepository>()
    private lateinit var validator: GooglePayShippingMethodUpdateValidator

    @BeforeEach
    fun setUp() {
        validator = GooglePayShippingMethodUpdateValidator(configurationRepository)
    }

    @Test
    fun `should throw ShippingAddressUnserviceableException when shipping options is null`(): Unit = runBlocking {
        val mockConfiguration = mockk<GooglePayConfiguration> {
            every { shippingOptions } returns null
        }
        every {
            configurationRepository.getPaymentMethodConfiguration(NoOpPaymentMethodConfigurationParams)
        } returns Result.success(mockConfiguration)

        assertThrows<ShippingAddressUnserviceableException> {
            validator(validUpdate)
        }
    }

    @Test
    fun `should throw ShippingAddressUnserviceableException when shipping methods empty`(): Unit = runBlocking {
        val emptyShippingOptions = CheckoutModule.Shipping(shippingMethods = listOf(), selectedMethod = VALID_ID)
        val mockConfiguration = mockk<GooglePayConfiguration> {
            every { shippingOptions } returns emptyShippingOptions
        }
        every {
            configurationRepository.getPaymentMethodConfiguration(NoOpPaymentMethodConfigurationParams)
        } returns Result.success(mockConfiguration)

        assertThrows<ShippingAddressUnserviceableException> {
            validator(validUpdate)
        }
    }

    @Test
    fun `should throw ShippingAddressUnserviceableException when shipping method is not listed`(): Unit = runBlocking {
        val otherShippingOptions = CheckoutModule.Shipping(
            shippingMethods = listOf(
                ShippingMethod("name", "description", 100, "other_id")

            ),
            selectedMethod = VALID_ID
        )
        val mockConfiguration = mockk<GooglePayConfiguration> {
            every { shippingOptions } returns otherShippingOptions
        }
        every {
            configurationRepository.getPaymentMethodConfiguration(NoOpPaymentMethodConfigurationParams)
        } returns Result.success(mockConfiguration)

        assertThrows<ShippingAddressUnserviceableException> {
            validator(validUpdate)
        }
    }

    @Test
    fun `should emit Unit when configuration is valid `() = runBlocking {
        val validShippingOptions = CheckoutModule.Shipping(
            shippingMethods = listOf(
                ShippingMethod("name", "description", 100, "valid_id")
            ),
            selectedMethod = VALID_ID
        )
        val mockConfiguration = mockk<GooglePayConfiguration>(relaxed = true) {
            every { shippingOptions } returns validShippingOptions
        }
        every {
            configurationRepository.getPaymentMethodConfiguration(NoOpPaymentMethodConfigurationParams)
        } returns Result.success(mockConfiguration)

        val result = validator.invoke(validUpdate).getOrThrow()

        assertEquals(Unit, result)
    }
}
