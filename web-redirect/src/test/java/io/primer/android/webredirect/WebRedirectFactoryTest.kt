package io.primer.android.webredirect

import io.mockk.clearAllMocks
import io.primer.android.core.utils.Success
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class WebRedirectFactoryTest {

    private lateinit var webRedirectFactory: WebRedirectFactory
    private val paymentMethodType = "testPaymentMethodType"

    @BeforeEach
    fun setUp() {
        webRedirectFactory = WebRedirectFactory(paymentMethodType)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `build should return Success with WebRedirectPaymentMethod`() {
        // When
        val result = webRedirectFactory.build()

        // Then
        assertTrue(result is Success)
        assertTrue(result.value is WebRedirectPaymentMethod)
        assertEquals(paymentMethodType, (result.value as WebRedirectPaymentMethod).type)
    }
}
