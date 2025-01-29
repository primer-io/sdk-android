package io.primer.android.payment.config

import android.content.Context
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.primer.android.R
import io.primer.android.components.assets.displayMetadata.models.PaymentMethodImplementation
import io.primer.android.configuration.data.model.IconPosition
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class TextDisplayMetadataKtTest {
    @MockK
    lateinit var context: Context

    @Test
    fun `toTextDisplayMetadata returns correct metadata in dark mode for not STRIPE_ACH`() {
        val buttonMetadata =
            PaymentMethodImplementation.ButtonMetadata(
                iconDisplayMetadata = emptyList(),
                backgroundColor =
                PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                    colored = "#123456",
                    light = "#FFFFFF",
                    dark = "#000000",
                ),
                borderColor =
                PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                    colored = "#654321",
                    light = "#FFFFFF",
                    dark = "#111111",
                ),
                borderWidth =
                PaymentMethodImplementation.ButtonMetadata.BorderWidthMetadata(
                    colored = 1.5f,
                    light = 1.0f,
                    dark = 2.0f,
                ),
                cornerRadius = 4.0f,
                text = "Pay",
                textColor =
                PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                    colored = "#ABCDEF",
                    light = "#FFFFFF",
                    dark = "#222222",
                ),
                iconPosition = IconPosition.END,
            )

        val paymentMethodImplementation =
            PaymentMethodImplementation(
                name = "Test",
                paymentMethodType = "ADYEN_IDEAL",
                buttonMetadata = buttonMetadata,
            )

        val result = paymentMethodImplementation.toTextDisplayMetadata(true, context)

        assertEquals("Test", result.name)
        assertEquals("ADYEN_IDEAL", result.paymentMethodType)
        assertEquals("#000000", result.backgroundColor)
        assertEquals("#111111", result.borderColor)
        assertEquals(2.0f, result.borderWidth)
        assertEquals(4.0f, result.cornerRadius)
        assertEquals("Pay", result.text)
        assertEquals("#222222", result.textColor)
        assertEquals(null, result.imageColor)
        assertEquals(IconPosition.END, result.iconPosition)
    }

    @Test
    fun `toTextDisplayMetadata returns correct metadata in non-dark mode for not STRIPE_ACH`() {
        val buttonMetadata =
            PaymentMethodImplementation.ButtonMetadata(
                iconDisplayMetadata = emptyList(),
                backgroundColor =
                PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                    colored = "#789012",
                    light = "#444444",
                    dark = "#888888",
                ),
                borderColor =
                PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                    colored = "#210987",
                    light = "#555555",
                    dark = "#999999",
                ),
                borderWidth =
                PaymentMethodImplementation.ButtonMetadata.BorderWidthMetadata(
                    colored = 2.5f,
                    light = 1.0f,
                    dark = 3.0f,
                ),
                cornerRadius = 5.0f,
                text = "Pay",
                textColor =
                PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                    colored = "#FEDCBA",
                    light = "#666666",
                    dark = "#AAAAAA",
                ),
                iconPosition = IconPosition.END,
            )

        val paymentMethodImplementation =
            PaymentMethodImplementation(
                name = "Test",
                paymentMethodType = "ADYEN_IDEAL",
                buttonMetadata = buttonMetadata,
            )

        val result = paymentMethodImplementation.toTextDisplayMetadata(false, context)

        assertEquals("Test", result.name)
        assertEquals("ADYEN_IDEAL", result.paymentMethodType)
        assertEquals("#789012", result.backgroundColor)
        assertEquals("#210987", result.borderColor)
        assertEquals(2.5f, result.borderWidth)
        assertEquals(5.0f, result.cornerRadius)
        assertEquals("Pay", result.text)
        assertEquals("#FEDCBA", result.textColor)
        assertEquals(null, result.imageColor)
        assertEquals(IconPosition.END, result.iconPosition)
    }

    @Test
    fun `toTextDisplayMetadata returns correct metadata in dark mode for STRIPE_ACH`() {
        val buttonMetadata =
            PaymentMethodImplementation.ButtonMetadata(
                iconDisplayMetadata = emptyList(),
                backgroundColor =
                PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                    colored = "#345678",
                    light = "#FFFFFF",
                    dark = "#888888",
                ),
                borderColor =
                PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                    colored = "#876543",
                    light = "#FFFFFF",
                    dark = "#999999",
                ),
                borderWidth =
                PaymentMethodImplementation.ButtonMetadata.BorderWidthMetadata(
                    colored = 3.5f,
                    light = 1.0f,
                    dark = 3.0f,
                ),
                cornerRadius = 6.0f,
                text = "Pay",
                textColor =
                PaymentMethodImplementation.ButtonMetadata.ColorMetadata(
                    colored = "#BAFEDC",
                    light = "#FFFFFF",
                    dark = "#AAAAAA",
                ),
                iconPosition = IconPosition.END,
            )

        val paymentMethodImplementation =
            PaymentMethodImplementation(
                name = "Test ACH",
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                buttonMetadata = buttonMetadata,
            )

        every { context.getString(R.string.pay_with_ach) } returns "Pay with ACH"

        val result = paymentMethodImplementation.toTextDisplayMetadata(true, context)

        assertEquals("Test ACH", result.name)
        assertEquals(PaymentMethodType.STRIPE_ACH.name, result.paymentMethodType)
        assertEquals("#888888", result.backgroundColor)
        assertEquals("#999999", result.borderColor)
        assertEquals(3.0f, result.borderWidth)
        assertEquals(6.0f, result.cornerRadius)
        assertEquals("Pay with ACH", result.text)
        assertEquals("#AAAAAA", result.textColor)
        assertEquals(null, result.imageColor)
        assertEquals(IconPosition.END, result.iconPosition)

        verify { context.getString(R.string.pay_with_ach) }
    }

    @Test
    fun `toTextDisplayMetadata returns correct metadata when buttonMetadata is null`() {
        val paymentMethodImplementation =
            PaymentMethodImplementation(
                name = "Test",
                paymentMethodType = "ADYEN_IDEAL",
                buttonMetadata = null,
            )

        val result = paymentMethodImplementation.toTextDisplayMetadata(true, context)

        assertEquals("Test", result.name)
        assertEquals("ADYEN_IDEAL", result.paymentMethodType)
        assertEquals(null, result.backgroundColor)
        assertEquals(null, result.borderColor)
        assertEquals(null, result.borderWidth)
        assertEquals(null, result.cornerRadius)
        assertEquals(null, result.text)
        assertEquals(null, result.textColor)
        assertEquals(null, result.imageColor)
        assertEquals(null, result.iconPosition)
    }
}
