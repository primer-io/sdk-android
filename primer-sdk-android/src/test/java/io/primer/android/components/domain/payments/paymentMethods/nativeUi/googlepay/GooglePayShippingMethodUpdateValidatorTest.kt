package io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.models.GooglePayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.repository.GooglePayConfigurationRepository
import io.primer.android.data.configuration.models.ShippingMethod
import io.primer.android.domain.action.models.ActionUpdateShippingOptionIdParams
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.exception.ShippingAddressUnserviceableException
import io.primer.android.domain.session.models.CheckoutModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private const val VALID_ID = "valid_id"

class GooglePayShippingMethodUpdateValidatorTest {
    private val validUpdate = ActionUpdateShippingOptionIdParams(VALID_ID)

    private val configurationRepository = mockk<GooglePayConfigurationRepository>()
    private val errorResolver = mockk<BaseErrorEventResolver>(relaxed = true)
    private lateinit var validator: GooglePayShippingMethodUpdateValidator

    @BeforeEach
    fun setUp() {
        validator = GooglePayShippingMethodUpdateValidator(
            configurationRepository,
            errorResolver,
            Dispatchers.Unconfined
        )
    }

    @Test
    fun `should throw ShippingAddressUnserviceableException when shipping options is null`() = runBlocking {
        val mockConfiguration = mockk<GooglePayConfiguration> {
            every { shippingOptions } returns null
        }
        every { configurationRepository.getConfiguration() } returns flowOf(mockConfiguration)

        assertThrows(ShippingAddressUnserviceableException::class.java) {
            runBlocking { validator(validUpdate).single() }
        }
        verify(exactly = 1) { errorResolver.resolve(ofType(ShippingAddressUnserviceableException::class), any()) }
    }

    @Test
    fun `should throw ShippingAddressUnserviceableException when shipping methods empty`() = runBlocking {
        val emptyShippingOptions = CheckoutModule.Shipping(listOf(), VALID_ID)
        val mockConfiguration = mockk<GooglePayConfiguration> {
            every { shippingOptions } returns emptyShippingOptions
        }
        every { configurationRepository.getConfiguration() } returns flowOf(mockConfiguration)

        assertThrows(ShippingAddressUnserviceableException::class.java) {
            runBlocking { validator(validUpdate).single() }
        }
        verify(exactly = 1) { errorResolver.resolve(ofType(ShippingAddressUnserviceableException::class), any()) }
    }

    @Test
    fun `should throw ShippingAddressUnserviceableException when shipping method is not listed`() = runBlocking {
        val otherShippingOptions = CheckoutModule.Shipping(
            listOf(
                ShippingMethod("name", "description", 100, "other_id")

            ),
            VALID_ID
        )
        val mockConfiguration = mockk<GooglePayConfiguration> {
            every { shippingOptions } returns otherShippingOptions
        }
        every { configurationRepository.getConfiguration() } returns flowOf(mockConfiguration)

        assertThrows(ShippingAddressUnserviceableException::class.java) {
            runBlocking { validator(validUpdate).single() }
        }
        verify(exactly = 1) { errorResolver.resolve(ofType(ShippingAddressUnserviceableException::class), any()) }
    }

    @Test
    fun `should emit Unit when configuration is valid `() = runBlocking {
        val validShippingOptions = CheckoutModule.Shipping(
            listOf(
                ShippingMethod("name", "description", 100, "valid_id")
            ),
            VALID_ID
        )
        val mockConfiguration = mockk<GooglePayConfiguration>(relaxed = true) {
            every { shippingOptions } returns validShippingOptions
        }
        every { configurationRepository.getConfiguration() } returns flowOf(mockConfiguration)

        val result = validator.invoke(validUpdate).single()

        assertEquals(Unit, result)
        verify(exactly = 0) { errorResolver.resolve(ofType(ShippingAddressUnserviceableException::class), any()) }
    }
}
