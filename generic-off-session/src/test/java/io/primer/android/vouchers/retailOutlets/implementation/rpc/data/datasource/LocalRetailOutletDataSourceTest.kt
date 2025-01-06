package io.primer.android.vouchers.retailOutlets.implementation.rpc.data.datasource

import io.mockk.every
import io.mockk.mockk
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.models.RetailOutletDataResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LocalRetailOutletDataSourceTest {
    private lateinit var dataSource: LocalRetailOutletDataSource

    @BeforeEach
    fun setUp() {
        dataSource = LocalRetailOutletDataSource()
    }

    @Test
    fun `test initial state is empty`() {
        val retailers = dataSource.get()
        assertEquals(0, retailers.size)
    }

    @Test
    fun `test update stores data correctly`() {
        val input =
            listOf(
                mockk<RetailOutletDataResponse> {
                    every { id } returns "1"
                    every { name } returns "Retailer 1"
                },
                mockk<RetailOutletDataResponse> {
                    every { id } returns "2"
                    every { name } returns "Retailer 2"
                },
            )

        dataSource.update(input)

        val retailers = dataSource.get()
        assertEquals(2, retailers.size)
        assertEquals("1", retailers[0].id)
        assertEquals("Retailer 1", retailers[0].name)
        assertEquals("2", retailers[1].id)
        assertEquals("Retailer 2", retailers[1].name)
    }

    @Test
    fun `test update clears previous data`() {
        val initialInput =
            listOf(
                mockk<RetailOutletDataResponse> {
                    every { id } returns "1"
                    every { name } returns "Retailer 1"
                },
            )
        val newInput =
            listOf(
                mockk<RetailOutletDataResponse> {
                    every { id } returns "2"
                    every { name } returns "Retailer 2"
                },
            )

        dataSource.update(initialInput)
        assertEquals(1, dataSource.get().size)
        assertEquals("1", dataSource.get()[0].id)

        dataSource.update(newInput)
        assertEquals(1, dataSource.get().size)
        assertEquals("2", dataSource.get()[0].id)
    }
}
