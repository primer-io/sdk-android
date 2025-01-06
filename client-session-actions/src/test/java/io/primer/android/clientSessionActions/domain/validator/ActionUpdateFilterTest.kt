package io.primer.android.clientSessionActions.domain.validator

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.clientSessionActions.domain.models.ActionUpdateBillingAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateCustomerDetailsParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateEmailAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateMobileNumberParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingOptionIdParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.data.settings.internal.PrimerConfig
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ActionUpdateFilterTest {
    @MockK
    private lateinit var configurationRepository: ConfigurationRepository

    @MockK
    private lateinit var config: PrimerConfig

    @InjectMockKs
    private lateinit var filter: ActionUpdateFilter

    @AfterEach
    fun tearDown() {
        confirmVerified(configurationRepository, config)
    }

    @Test
    fun `filter() should return 'false' when called with ActionUpdateCustomerDetailsParams`() =
        runTest {
            val result = filter.filter(mockk<ActionUpdateCustomerDetailsParams>())

            assertFalse(result)
        }

    @Test
    fun `filter() should return 'false' when called with ActionUpdateBillingAddressParams`() =
        runTest {
            val result = filter.filter(mockk<ActionUpdateBillingAddressParams>())

            assertFalse(result)
        }

    @Test
    fun `filter() should return 'true' when called with ActionUpdateSelectPaymentMethodParams and intent is vaulted`() =
        runTest {
            every {
                configurationRepository.getConfiguration()
            } returns mockk()
            every { config.intent.paymentMethodIntent.isVault } returns true

            val result = filter.filter(mockk<ActionUpdateSelectPaymentMethodParams>())

            assertTrue(result)
            coVerify {
                configurationRepository.getConfiguration()
                config.intent.paymentMethodIntent.isVault
            }
        }

    @Test
    fun `filter() should return 'true' when called with ActionUpdateSelectPaymentMethodParams and intent is not vaulted and there are no surcharges`() =
        runTest {
            every {
                configurationRepository.getConfiguration()
            } returns
                mockk<Configuration> {
                    every {
                        clientSession.clientSessionDataResponse.paymentMethod?.surcharges
                    } returns emptyMap()
                }

            every { config.intent.paymentMethodIntent.isVault } returns false

            val result = filter.filter(mockk<ActionUpdateSelectPaymentMethodParams>())

            assertTrue(result)
            coVerify {
                configurationRepository.getConfiguration()
                config.intent.paymentMethodIntent.isVault
            }
        }

    @Test
    fun `filter() should return 'false' when called with ActionUpdateSelectPaymentMethodParams and intent is not vaulted and there are surcharges`() =
        runTest {
            every {
                configurationRepository.getConfiguration()
            } returns
                mockk<Configuration> {
                    every {
                        clientSession.clientSessionDataResponse.paymentMethod?.surcharges
                    } returns mapOf("order" to 1)
                }
            every { config.intent.paymentMethodIntent.isVault } returns false

            val result = filter.filter(mockk<ActionUpdateSelectPaymentMethodParams>())

            assertFalse(result)
            coVerify {
                configurationRepository.getConfiguration()
                config.intent.paymentMethodIntent.isVault
            }
        }

    @Test
    fun `filter() should return 'true' when called with ActionUpdateUnselectPaymentMethodParams and intent is vaulted`() =
        runTest {
            coEvery {
                configurationRepository.getConfiguration()
            } returns mockk()
            every { config.intent.paymentMethodIntent.isVault } returns true

            val result = filter.filter(mockk<ActionUpdateUnselectPaymentMethodParams>())

            assertTrue(result)
            coVerify {
                configurationRepository.getConfiguration()
                config.intent.paymentMethodIntent.isVault
            }
        }

    @Test
    fun `filter() should return 'true' when called with ActionUpdateUnselectPaymentMethodParams and intent is not vaulted and there are no surcharges`() =
        runTest {
            every {
                configurationRepository.getConfiguration()
            } returns
                mockk {
                    every {
                        clientSession.clientSessionDataResponse.paymentMethod?.surcharges
                    } returns emptyMap()
                }

            every { config.intent.paymentMethodIntent.isVault } returns false

            val result = filter.filter(mockk<ActionUpdateUnselectPaymentMethodParams>())

            assertTrue(result)
            coVerify {
                configurationRepository.getConfiguration()
                config.intent.paymentMethodIntent.isVault
            }
        }

    @Test
    fun `filter() should return 'false' when called with ActionUpdateUnselectPaymentMethodParams and intent is not vaulted and there are surcharges`() =
        runTest {
            every {
                configurationRepository.getConfiguration()
            } returns
                mockk {
                    every {
                        clientSession.clientSessionDataResponse.paymentMethod?.surcharges
                    } returns mapOf("order" to 1)
                }

            every { config.intent.paymentMethodIntent.isVault } returns false

            val result = filter.filter(mockk<ActionUpdateUnselectPaymentMethodParams>())

            assertFalse(result)
            coVerify {
                configurationRepository.getConfiguration()
                config.intent.paymentMethodIntent.isVault
            }
        }

    @Test
    fun `filter() should return 'false' when called with ActionUpdateMobileNumberParams`() =
        runTest {
            val result = filter.filter(mockk<ActionUpdateMobileNumberParams>())

            assertFalse(result)
        }

    @Test
    fun `filter() should return 'false' when called with ActionUpdateShippingAddressParams`() =
        runTest {
            val result = filter.filter(mockk<ActionUpdateShippingAddressParams>())

            assertFalse(result)
        }

    @Test
    fun `filter() should return 'false' when called with ActionUpdateShippingOptionIdParams`() =
        runTest {
            val result = filter.filter(mockk<ActionUpdateShippingOptionIdParams>())

            assertFalse(result)
        }

    @Test
    fun `filter() should return 'false' when called with ActionUpdateEmailAddressParams`() =
        runTest {
            val result = filter.filter(mockk<ActionUpdateEmailAddressParams>())

            assertFalse(result)
        }
}
