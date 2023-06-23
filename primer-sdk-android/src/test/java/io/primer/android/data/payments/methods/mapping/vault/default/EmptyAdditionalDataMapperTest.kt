package io.primer.android.data.payments.methods.mapping.vault.default

import io.primer.android.data.payments.methods.models.empty.EmptyExchangeDataRequest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class EmptyAdditionalDataMapperTest {

    private lateinit var mapper: EmptyAdditionalDataMapper

    @BeforeEach
    fun setUp() {
        mapper = EmptyAdditionalDataMapper()
    }

    @Test
    fun `map() should return EmptyExchangeDataRequest when additional data is null`() {
        runTest {
            val exchangeDataRequest = mapper.map(null)
            assertEquals(EmptyExchangeDataRequest::class, exchangeDataRequest::class)
        }
    }
}
