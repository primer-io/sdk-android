package io.primer.android.domain.action.validator

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.action.models.ActionUpdateBillingAddressParams
import io.primer.android.domain.action.models.ActionUpdateCustomerDetailsParams
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.domain.session.repository.ConfigurationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class ActionUpdateFilterTest {
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
    fun `filter() should return 'false' when called with ActionUpdateCustomerDetailsParams`() = runTest {
        val result = filter.filter(mockk<ActionUpdateCustomerDetailsParams>()).toList()

        assertEquals(listOf(false), result)
    }

    @Test
    fun `filter() should return 'false' when called with ActionUpdateBillingAddressParams`() = runTest {
        val result = filter.filter(mockk<ActionUpdateBillingAddressParams>()).toList()

        assertEquals(listOf(false), result)
    }

    @Test
    fun `filter() should return 'true' when called with ActionUpdateSelectPaymentMethodParams and intent is vaulted`() = runTest {
        every {
            configurationRepository.fetchConfiguration(any())
        } returns flowOf(mockk())
        every { config.intent.paymentMethodIntent.isVault } returns true

        val result = filter.filter(mockk<ActionUpdateSelectPaymentMethodParams>()).toList()

        assertEquals(listOf(true), result)
        coVerify {
            configurationRepository.fetchConfiguration(true)
            config.intent.paymentMethodIntent.isVault
        }
    }

    @Test
    fun `filter() should return 'true' when called with ActionUpdateSelectPaymentMethodParams and intent is not vaulted and there are no surcharges`() = runTest {
        every {
            configurationRepository.fetchConfiguration(any())
        } returns flowOf(
            mockk {
                every {
                    clientSession.clientSessionDataResponse.paymentMethod?.surcharges
                } returns emptyMap()
            }
        )
        every { config.intent.paymentMethodIntent.isVault } returns false

        val result = filter.filter(mockk<ActionUpdateSelectPaymentMethodParams>()).toList()

        assertEquals(listOf(true), result)
        coVerify {
            configurationRepository.fetchConfiguration(true)
            config.intent.paymentMethodIntent.isVault
        }
    }

    @Test
    fun `filter() should return 'false' when called with ActionUpdateSelectPaymentMethodParams and intent is not vaulted and there are surcharges`() = runTest {
        every {
            configurationRepository.fetchConfiguration(any())
        } returns flowOf(
            mockk {
                every {
                    clientSession.clientSessionDataResponse.paymentMethod?.surcharges
                } returns mapOf("order" to 1)
            }
        )
        every { config.intent.paymentMethodIntent.isVault } returns false

        val result = filter.filter(mockk<ActionUpdateSelectPaymentMethodParams>()).toList()

        assertEquals(listOf(false), result)
        coVerify {
            configurationRepository.fetchConfiguration(true)
            config.intent.paymentMethodIntent.isVault
        }
    }

    @Test
    fun `filter() should return 'true' when called with ActionUpdateUnselectPaymentMethodParams and intent is vaulted`() = runTest {
        every {
            configurationRepository.fetchConfiguration(any())
        } returns flowOf(mockk())
        every { config.intent.paymentMethodIntent.isVault } returns true

        val result = filter.filter(mockk<ActionUpdateUnselectPaymentMethodParams>()).toList()

        assertEquals(listOf(true), result)
        coVerify {
            configurationRepository.fetchConfiguration(true)
            config.intent.paymentMethodIntent.isVault
        }
    }

    @Test
    fun `filter() should return 'true' when called with ActionUpdateUnselectPaymentMethodParams and intent is not vaulted and there are no surcharges`() = runTest {
        every {
            configurationRepository.fetchConfiguration(any())
        } returns flowOf(
            mockk {
                every {
                    clientSession.clientSessionDataResponse.paymentMethod?.surcharges
                } returns emptyMap()
            }
        )
        every { config.intent.paymentMethodIntent.isVault } returns false

        val result = filter.filter(mockk<ActionUpdateUnselectPaymentMethodParams>()).toList()

        assertEquals(listOf(true), result)
        coVerify {
            configurationRepository.fetchConfiguration(true)
            config.intent.paymentMethodIntent.isVault
        }
    }

    @Test
    fun `filter() should return 'false' when called with ActionUpdateUnselectPaymentMethodParams and intent is not vaulted and there are surcharges`() = runTest {
        every {
            configurationRepository.fetchConfiguration(any())
        } returns flowOf(
            mockk {
                every {
                    clientSession.clientSessionDataResponse.paymentMethod?.surcharges
                } returns mapOf("order" to 1)
            }
        )
        every { config.intent.paymentMethodIntent.isVault } returns false

        val result = filter.filter(mockk<ActionUpdateUnselectPaymentMethodParams>()).toList()

        assertEquals(listOf(false), result)
        coVerify {
            configurationRepository.fetchConfiguration(true)
            config.intent.paymentMethodIntent.isVault
        }
    }
}
