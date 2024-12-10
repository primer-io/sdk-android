package io.primer.android.vault.implementation.vaultedMethods.data.mapping.vault.default

import io.primer.android.vault.implementation.vaultedMethods.data.mapping.default.EmptyAdditionalDataMapper
import io.primer.android.vault.implementation.vaultedMethods.data.model.empty.EmptyExchangeDataRequest
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
