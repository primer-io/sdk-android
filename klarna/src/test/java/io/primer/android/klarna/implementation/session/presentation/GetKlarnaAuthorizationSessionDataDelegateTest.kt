package io.primer.android.klarna.implementation.session.presentation

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GetKlarnaAuthorizationSessionDataDelegateTest {
    @MockK
    private lateinit var repo: ConfigurationRepository

    @InjectMockKs
    private lateinit var delegate: GetKlarnaAuthorizationSessionDataDelegate

    @Test
    fun `getAuthorizationSessionDataOrNull() should return null when extraMerchantData is not available for Klarna`() {
        every {
            repo.getConfiguration().paymentMethods
        } returns listOf(
            mockk {
                every { type } returns "KLARNA"
                every { options } returns mockk {
                    every { extraMerchantData } returns null
                }
            }
        )

        val result = delegate.getAuthorizationSessionDataOrNull()

        assertEquals(null, result)
        verify(exactly = 1) {
            repo.getConfiguration().paymentMethods
        }
    }

    @Test
    fun `getAuthorizationSessionDataOrNull() should return null when KLARNA options are missing`() {
        every {
            repo.getConfiguration().paymentMethods
        } returns listOf(
            mockk {
                every { type } returns "ADYEN_IDEAL"
            }
        )

        val result = delegate.getAuthorizationSessionDataOrNull()

        assertEquals(null, result)
        verify(exactly = 1) {
            repo.getConfiguration().paymentMethods
        }
    }

    @Test
    fun `getAuthorizationSessionDataOrNull() should return value when extraMerchantData is available for Klarna`() {
        val extraMerchantData = JSONObject("""{"a":"b"}""")
        every {
            repo.getConfiguration().paymentMethods
        } returns listOf(
            mockk {
                every { type } returns "KLARNA"
                every { options } returns mockk options@{
                    every { this@options.extraMerchantData } returns JSONObject("""{"a":"b"}""")
                }
            }
        )

        val result = delegate.getAuthorizationSessionDataOrNull()

        assertEquals(
            JSONObject().apply {
                put(
                    "attachment",
                    JSONObject().apply {
                        put("content_type", "application/vnd.klarna.internal.emd-v2+json")
                        put("body", extraMerchantData.toString())
                    }
                )
            }.toString(),
            result
        )
        verify(exactly = 1) {
            repo.getConfiguration().paymentMethods
        }
    }
}
