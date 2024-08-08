package io.primer.android.data.configuration.datasource

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.primer.android.data.configuration.models.ConfigurationDataResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class GlobalConfigurationCacheDataSourceTest {

    private val testConfigurationCache = ConfigurationCache(validUntil = 123456789L, clientToken = "testToken")

    @RelaxedMockK
    internal lateinit var testConfigurationDataResponse: ConfigurationDataResponse

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        GlobalConfigurationCacheDataSource.clear()
    }

    @Test
    fun `get returns null when cache is empty`() {
        val result = GlobalConfigurationCacheDataSource.get()
        assertNull(result)
    }

    @Test
    fun `update stores the cache and get retrieves it`() {
        val input = testConfigurationCache to testConfigurationDataResponse

        GlobalConfigurationCacheDataSource.update(input)
        val result = GlobalConfigurationCacheDataSource.get()

        assertEquals(input, result, "Expected get to return the stored cache after update")
    }

    @Test
    fun `clear removes the cache`() {
        val input = testConfigurationCache to testConfigurationDataResponse

        GlobalConfigurationCacheDataSource.update(input)
        GlobalConfigurationCacheDataSource.clear()

        val result = GlobalConfigurationCacheDataSource.get()

        assertNull(result)
    }
}
