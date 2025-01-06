package io.primer.android.ipay88

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.utils.Failure
import io.primer.android.core.utils.Success
import io.primer.android.ipay88.implementation.helpers.IPay88SdkClassValidator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IPay88PaymentMethodFactoryTest {
    private lateinit var factory: IPay88PaymentMethodFactory
    private val type = "someType"
    private val configurationDataSource: CacheConfigurationDataSource = mockk()

    @BeforeEach
    fun setUp() {
        mockkConstructor(IPay88SdkClassValidator::class)
        factory = IPay88PaymentMethodFactory(type, configurationDataSource)
    }

    @AfterEach
    fun tearDown() {
        unmockkConstructor(IPay88SdkClassValidator::class)
    }

    @Test
    fun `build returns Failure when IPaySdk is not included`() {
        every { anyConstructed<IPay88SdkClassValidator>().isIPaySdkIncluded() } returns false
        val clientSessionDataResponse =
            mockk<ClientSessionDataResponse> {
                every { order } returns
                    mockk {
                        every { countryCode } returns
                            mockk {
                                every { name } returns "US"
                            }
                    }
            }
        val configurationData =
            mockk<ConfigurationData> {
                every { clientSession } returns clientSessionDataResponse
            }
        every { configurationDataSource.get() } returns configurationData

        val result = factory.build()

        assertTrue(result is Failure)
        val exception = result.value
        assertTrue(exception is IllegalStateException)
        assertTrue(
            exception.message!!.contains(
                IPay88SdkClassValidator.I_PAY_CLASS_NOT_LOADED_ERROR.format(type, "us", "us", type),
            ),
        )
    }

    @Test
    fun `build returns Success when IPaySdk is included`() {
        every { anyConstructed<IPay88SdkClassValidator>().isIPaySdkIncluded() } returns true

        val result = factory.build()

        assertTrue(result is Success)
        val paymentMethod = result.value
        assertTrue(paymentMethod is IPay88PaymentMethod)
        assertEquals(paymentMethod.type, type)
    }
}
