package io.primer.android.googlepay.implementation.composer.ui.assets

import io.mockk.mockk
import io.primer.android.data.settings.GooglePayButtonStyle
import io.primer.android.data.settings.PrimerGooglePayOptions
import io.primer.android.data.settings.PrimerPaymentMethodOptions
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.googlepay.R
import io.primer.android.googlepay.implementation.configuration.domain.GooglePayConfigurationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

internal class GooglePayBrandTest {
    @Test
    fun `iconResId should return correct resource ID`() {
        // Given
        val brand =
            GooglePayBrand(
                PrimerSettings(
                    paymentMethodOptions =
                        PrimerPaymentMethodOptions(
                            googlePayOptions =
                                PrimerGooglePayOptions(
                                    buttonStyle = GooglePayButtonStyle.BLACK,
                                ),
                        ),
                ),
                mockk<GooglePayConfigurationRepository>(),
            )

        // When
        val iconResId = brand.iconResId

        // Then
        assertEquals(R.drawable.ic_logo_googlepay, iconResId)
    }

    @Test
    fun `logoResId should return correct resource ID when GooglePayButtonStyle is BLACK`() {
        // Given
        val brand =
            GooglePayBrand(
                PrimerSettings(
                    paymentMethodOptions =
                        PrimerPaymentMethodOptions(
                            googlePayOptions =
                                PrimerGooglePayOptions(
                                    buttonStyle = GooglePayButtonStyle.BLACK,
                                ),
                        ),
                ),
                mockk<GooglePayConfigurationRepository>(),
            )

        // When
        val logoResId = brand.logoResId

        // Then
        assertEquals(R.drawable.ic_logo_google_pay_black_square, logoResId)
    }

    @Test
    fun `logoResId should return correct resource ID when GooglePayButtonStyle is WHITE`() {
        // Given
        val brand =
            GooglePayBrand(
                PrimerSettings(
                    paymentMethodOptions =
                        PrimerPaymentMethodOptions(
                            googlePayOptions =
                                PrimerGooglePayOptions(
                                    buttonStyle = GooglePayButtonStyle.WHITE,
                                ),
                        ),
                ),
                mockk<GooglePayConfigurationRepository>(),
            )

        // When
        val logoResId = brand.logoResId

        // Then
        assertEquals(R.drawable.ic_logo_google_pay_square, logoResId)
    }

    @Test
    fun `iconLightResId should return correct resource ID`() {
        // Given
        val brand =
            GooglePayBrand(
                PrimerSettings(
                    paymentMethodOptions =
                        PrimerPaymentMethodOptions(
                            googlePayOptions =
                                PrimerGooglePayOptions(
                                    buttonStyle = GooglePayButtonStyle.BLACK,
                                ),
                        ),
                ),
                mockk<GooglePayConfigurationRepository>(),
            )

        // When
        val iconLightResId = brand.iconLightResId

        // Then
        assertEquals(R.drawable.ic_logo_googlepay_light, iconLightResId)
    }

    @Test
    fun `viewProvider is defined`() {
        // Given
        val brand =
            GooglePayBrand(
                PrimerSettings(
                    paymentMethodOptions =
                        PrimerPaymentMethodOptions(
                            googlePayOptions =
                                PrimerGooglePayOptions(
                                    buttonStyle = GooglePayButtonStyle.BLACK,
                                ),
                        ),
                ),
                mockk<GooglePayConfigurationRepository>(),
            )

        // When
        val viewProvider = brand.viewProvider()

        // Then
        assertNotNull(viewProvider)
    }
}
