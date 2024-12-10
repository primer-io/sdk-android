package io.primer.android.klarna

import io.mockk.every
import io.mockk.mockkConstructor
import io.primer.android.core.utils.Failure
import io.primer.android.core.utils.Success
import io.primer.android.klarna.implementation.helpers.KlarnaSdkClassValidator
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KlarnaFactoryTest {

    private lateinit var factory: KlarnaFactory

    @BeforeEach
    fun setUp() {
        mockkConstructor(KlarnaSdkClassValidator::class)
    }

    @Test
    fun `build should return Success when type is PRIMER_TEST_KLARNA`() {
        val type = PaymentMethodType.PRIMER_TEST_KLARNA.name
        factory = KlarnaFactory(type)

        val result = factory.build()

        assertTrue(result is Success)
        assertTrue((result as Success).value is Klarna)
        assertEquals(type, (result.value as Klarna).type)
    }

    @Test
    fun `build should return Failure when KlarnaSdkClassValidator fails`() {
        val type = "some_other_type"
        factory = KlarnaFactory(type)

        every { anyConstructed<KlarnaSdkClassValidator>().isKlarnaSdkIncluded() } returns false

        val result = factory.build()

        assertTrue(result is Failure)
        assertTrue((result as Failure).value is IllegalStateException)
        assertEquals(KlarnaSdkClassValidator.KLARNA_CLASS_NOT_LOADED_ERROR, result.value.message)
    }

    @Test
    fun `build should return Success when KlarnaSdkClassValidator passes`() {
        val type = "some_other_type"
        factory = KlarnaFactory(type)

        every { anyConstructed<KlarnaSdkClassValidator>().isKlarnaSdkIncluded() } returns true

        val result = factory.build()

        assertTrue(result is Success)
        assertTrue((result as Success).value is Klarna)
        assertEquals(type, (result.value as Klarna).type)
    }
}
