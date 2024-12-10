package io.primer.android.vouchers.retailOutlets.implementation.rpc.domain

import io.mockk.coEvery
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.configuration.domain.model.PaymentMethodConfig
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.vouchers.InstantExecutorExtension
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models.RetailOutlet
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models.RetailOutletParams
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.repository.RetailOutletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class RetailOutletInteractorTest {

    private val configurationRepository: ConfigurationRepository = mockk()
    private val retailOutletRepository: RetailOutletRepository = mockk()
    private lateinit var interactor: RetailOutletInteractor

    @BeforeEach
    fun setUp() {
        interactor = RetailOutletInteractor(configurationRepository, retailOutletRepository)
    }

    @Test
    fun `performAction should return sorted and enabled retail outlets`() = runTest {
        val paymentMethodType = "retail_outlet"
        val params = RetailOutletParams(paymentMethodType)
        val retailOutlets = listOf(
            RetailOutlet(id = "2", name = "Z Outlet", disabled = false, iconUrl = "url2"),
            RetailOutlet(id = "1", name = "A Outlet", disabled = false, iconUrl = "url1"),
            RetailOutlet(id = "3", name = "Disabled Outlet", disabled = true, iconUrl = "url3")
        )

        val paymentMethodConfig = mockk<PaymentMethodConfig> {
            every { id } returns "id"
            every { type } returns paymentMethodType
        }

        every { configurationRepository.getConfiguration() } returns mockk {
            every { paymentMethods } returns listOf(paymentMethodConfig)
        }

        coEvery { retailOutletRepository.getRetailOutlets(any()) } returns Result.success(retailOutlets)

        val result = interactor(params)
        assertTrue(result.isSuccess)
        val resultsList = result.getOrThrow()
        assertTrue(resultsList.find { it.id == "3" } == null)
        assertEquals(2, resultsList.size)
        assertEquals("A Outlet", resultsList[0].name)
        assertEquals("Z Outlet", resultsList[1].name)
    }
}
