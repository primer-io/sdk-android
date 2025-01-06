package io.primer.logging.internal.dsl

import io.mockk.junit5.MockKExtension
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class WhitelistedKeyBuilderTest {
    @Test
    fun `whitelistedKeys returns the same keys that are used in the building block`() {
        val result =
            whitelistedKeys {
                nonPrimitiveKey("sessionData") {
                    primitiveKey("description")
                    nonPrimitiveKey("orderLines") {
                        primitiveKey("type")
                        primitiveKey("name")
                    }
                }
            }

        assertEquals(
            listOf(
                WhitelistedKey.NonPrimitiveWhitelistedKey(
                    value = "sessionData",
                    children =
                        listOf(
                            WhitelistedKey.PrimitiveWhitelistedKey(value = "description"),
                            WhitelistedKey.NonPrimitiveWhitelistedKey(
                                value = "orderLines",
                                children =
                                    listOf(
                                        WhitelistedKey.PrimitiveWhitelistedKey(value = "type"),
                                        WhitelistedKey.PrimitiveWhitelistedKey(value = "name"),
                                    ),
                            ),
                        ),
                ),
            ),
            result,
        )
    }
}
