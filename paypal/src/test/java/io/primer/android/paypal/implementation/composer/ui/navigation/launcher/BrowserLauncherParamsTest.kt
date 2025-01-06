package io.primer.android.paypal.implementation.composer.ui.navigation.launcher

import io.primer.android.PrimerSessionIntent
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BrowserLauncherParamsTest {
    @Test
    fun `test BrowserLauncherParams properties`() {
        // Arrange
        val url = "https://example.com"
        val host = "example.com"
        val paymentMethodType = "paypal"
        val sessionIntent = PrimerSessionIntent.CHECKOUT

        // Act
        val browserLauncherParams =
            BrowserLauncherParams(
                url = url,
                host = host,
                paymentMethodType = paymentMethodType,
                sessionIntent = sessionIntent,
            )

        // Assert
        assertEquals(url, browserLauncherParams.url)
        assertEquals(host, browserLauncherParams.host)
        assertEquals(paymentMethodType, browserLauncherParams.paymentMethodType)
        assertEquals(sessionIntent, browserLauncherParams.sessionIntent)
    }

    @Test
    fun `test BrowserLauncherParams inheritance`() {
        // Arrange
        val paymentMethodType = "paypal"
        val sessionIntent = PrimerSessionIntent.CHECKOUT

        // Act
        val browserLauncherParams =
            BrowserLauncherParams(
                url = "https://example.com",
                host = "example.com",
                paymentMethodType = paymentMethodType,
                sessionIntent = sessionIntent,
            )

        // Assert
        assertEquals(paymentMethodType, browserLauncherParams.paymentMethodType)
        assertEquals(sessionIntent, browserLauncherParams.sessionIntent)
    }
}
