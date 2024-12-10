package io.primer.android.googlepay

import io.mockk.every
import io.mockk.mockk
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.utils.Failure
import io.primer.android.core.utils.Success
import io.primer.android.data.settings.GooglePayButtonStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GooglePayFactoryTest {
    private lateinit var primerSettings: PrimerSettings
    private lateinit var configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>

    @BeforeEach
    fun setUp() {
        primerSettings = mockk {
            every { paymentMethodOptions.googlePayOptions.merchantName } returns "Test Merchant"
            every { paymentMethodOptions.googlePayOptions.buttonStyle } returns GooglePayButtonStyle.WHITE
            every { paymentMethodOptions.googlePayOptions.captureBillingAddress } returns true
            every { paymentMethodOptions.googlePayOptions.existingPaymentMethodRequired } returns false
        }
        configurationDataSource = mockk()
    }

    @Test
    fun `build returns GooglePay when configuration is valid`() {
        mockConfigurationData("USD")
        val factory = GooglePayFactory(primerSettings, configurationDataSource)

        // Build GooglePay
        val result = factory.build()

        // Assert result
        assertEquals(true, result is Success)
        assertEquals(true, (result as Success).value is GooglePay)
    }

    @Test
    fun `build returns Failure when country code is invalid`() {
        mockConfigurationData("anything else")
        val factory = GooglePayFactory(primerSettings, configurationDataSource)
        // Build GooglePay
        val result = factory.build()

        // Assert result
        assertEquals(true, result is Failure)
    }

    private fun mockConfigurationData(currencyCodeStr: String) {
        every { configurationDataSource.get() } returns mockk {
            every {
                clientSession
            } returns mockk {
                every {
                    order
                } returns mockk {
                    every {
                        currentAmount
                    } returns 1000
                    every {
                        currencyCode
                    } returns currencyCodeStr
                    every {
                        countryCode
                    } returns CountryCode.US
                }
                every {
                    paymentMethod
                } returns mockk {
                    every {
                        orderedAllowedCardNetworks
                    } returns emptyList()
                }
            }
        }
    }
}
