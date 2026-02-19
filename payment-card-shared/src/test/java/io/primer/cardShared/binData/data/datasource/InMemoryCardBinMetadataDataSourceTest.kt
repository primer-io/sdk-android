package io.primer.cardShared.binData.data.datasource

import io.mockk.mockk
import io.primer.cardShared.binData.data.model.BinDataResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemoryCardBinMetadataDataSourceTest {
    private lateinit var dataSource: InMemoryCardBinMetadataDataSource

    @BeforeEach
    fun setUp() {
        dataSource = InMemoryCardBinMetadataDataSource()
    }

    @Test
    fun `get returns empty map when no data is present`() {
        val result = dataSource.get()
        assertEquals(emptyMap<String, BinDataResponse>(), result)
    }

    @Test
    fun `update adds data to the cache`() {
        val key = "key1"
        val data = mockk<BinDataResponse>()

        dataSource.update(key to data)

        val result = dataSource.get()
        assertEquals(mapOf(key to data), result)
    }

    @Test
    fun `update overwrites existing data for the same key`() {
        val key = "key1"
        val initialData = mockk<BinDataResponse>()
        val newData = mockk<BinDataResponse>()

        dataSource.update(key to initialData)
        dataSource.update(key to newData)

        val result = dataSource.get()
        assertEquals(mapOf(key to newData), result)
    }
}
