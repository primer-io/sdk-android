package io.primer.logging.internal

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.core.logging.ConsolePrimerLogger
import io.primer.android.core.logging.PrimerLog
import io.primer.android.core.logging.PrimerLogLevel
import io.primer.android.core.logging.PrimerLogger
import io.primer.android.core.logging.PrimerLogging
import io.primer.android.core.logging.internal.HttpLoggerInterceptor
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.io.IOException

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class HttpLoggerInterceptorTest {
    // ktlint-disable max-line-length
    private val rawBody = JSONObject(
        """{"customerId":null,"orderId":"android-test-356e6b98-07a3-4b2a-9857-1a5f7c553818","amount":1000,"currencyCode":"EUR","order":{"countryCode":"NL","lineItems":[{"itemId":"item-123","description":"this item","amount":1000,"quantity":1,"discountAmount":0}]},"customer":{"emailAddress":"EMAIL_ADDRESS","mobileNumber":"+44 7398 595742","firstName":"FIRST_NAME","lastName":"LAST_NAME","billingAddress":{"firstName":"FIRST_NAME","lastName":"LAST_NAME","postalCode":"12345","addressLine1":"1 test","countryCode":"NL","city":"test","state":"test"},"shippingAddress":{"postalCode":"12345","addressLine1":"1 test","countryCode":"NL","city":"test","state":"test"},"nationalDocumentId":"9011211234567"},"paymentMethod":{"vaultOnSuccess":false,"descriptor":"test-descriptor","vaultOn3DS":false,"options":{"PAYMENT_CARD":{"networks":{"JCB":{"surcharge":{"amount":0}}}},"PAYPAL":{"surcharge":{"amount":50}},"ADYEN_IDEAL":{"surcharge":{"amount":120}},"ADYEN_GIROPAY":{"surcharge":{"amount":130}},"ADYEN_SOFORT":{"surcharge":{"amount":150}},"ADYEN_TRUSTLY":{"surcharge":{"amount":140}},"KLARNA":{"surcharge":{"amount":140}},"GOOGLE_PAY":{"surcharge":{"amount":60}}}},"clientToken":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImtpZCI6ImNsaWVudC10b2tlbi1zaWduaW5nLWtleSJ9.eyJleHAiOjE3MDIzNzI2ODIsImFjY2Vzc1Rva2VuIjoiZDExODJhYTYtYjZhZi00ZTNhLWJmMmMtNTI2MjRlNzM0MDJlIiwiYW5hbHl0aWNzVXJsIjoiaHR0cHM6Ly9hbmFseXRpY3MuYXBpLnN0YWdpbmcuY29yZS5wcmltZXIuaW8vbWl4cGFuZWwiLCJhbmFseXRpY3NVcmxWMiI6Imh0dHBzOi8vYW5hbHl0aWNzLnN0YWdpbmcuZGF0YS5wcmltZXIuaW8vY2hlY2tvdXQvdHJhY2siLCJpbnRlbnQiOiJDSEVDS09VVCIsImNvbmZpZ3VyYXRpb25VcmwiOiJodHRwczovL2FwaS5zdGFnaW5nLnByaW1lci5pby9jbGllbnQtc2RrL2NvbmZpZ3VyYXRpb24iLCJjb3JlVXJsIjoiaHR0cHM6Ly9hcGkuc3RhZ2luZy5wcmltZXIuaW8iLCJwY2lVcmwiOiJodHRwczovL3Nkay5hcGkuc3RhZ2luZy5wcmltZXIuaW8iLCJlbnYiOiJTVEFHSU5HIiwicGF5bWVudEZsb3ciOiJERUZBVUxUIn0.PfiiWhYSsvo-mWFr7UMMaTZzCctWQuQpbdW5CARsvos","clientTokenExpirationDate":"2023-12-12T09:18:02.346787"}"""
    ).toString()

    private val blacklistedHeaders: List<String> = listOf("Primer-Client-Token")
    private val whitelistedBodyKeys: List<WhitelistedKey> =
        whitelistedKeys {
            primitiveKey("orderId")
            nonPrimitiveKey("order") {
                primitiveKey("countryCode")
                nonPrimitiveKey("lineItems") {
                    primitiveKey("quantity")
                }
            }
        }

    private val consolePrimerLogger by lazy { ConsolePrimerLogger() }

    private val customPrimerLogger by lazy {
        object : PrimerLogger {
            override var logLevel: PrimerLogLevel = PrimerLogLevel.DEBUG

            override fun log(primerLog: PrimerLog) {
                // no-op
            }
        }
    }

    @RelaxedMockK
    private lateinit var logReporter: LogReporter

    private lateinit var interceptor: HttpLoggerInterceptor

    @Test
    fun `should log headers and obfuscate sensitive headers when intercept() is called and obfuscation is enabled`() {
        initInterceptor(primerLogger = customPrimerLogger)
        val request = mockk<Request> {
            every { url } returns "https://example.com/core".toHttpUrl()
            every { method } returns "GET"
            every { headers } returns mapOf(
                "Content-Type" to "application/json; charset=utf-8",
                "Content-Length" to "2",
                "Primer-SDK-Version" to "2.19.2",
                "Primer-SDK-Client" to "ANDROID_NATIVE",
                "Primer-Client-Token" to "0928df7c-ed97-4c2b-9ca7-3774c9252b56"
            ).toHeaders()
            every { body } returns
                "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        }
        val response = mockk<Response> {
            every { headers } returns mapOf(
                "x-powered-by" to "Express",
                "content-type" to "application/json; charset=utf-8",
                "etag" to """"W/"847-yT0ll/7/DgvUQ6WdTDcRHMGcrHI"""",
                "function-execution-id" to "k6xuqcreanwy",
                "x-cloud-trace-context" to "f0153994d00d137bd411f2f8bab24e59;o=1",
                "date" to "Wed, 20 Dec 2023 13:05:11 GMT",
                "server" to "Google Frontend",
                "content-length" to "2119",
                "alt-svc" to """h3=":443"; ma=2592000,h3-29=":443"; ma=2592000""",
                "Primer-Client-Token" to "0928df7c-ed97-4c2b-9ca7-3774c9252b56"
            ).toHeaders()
            every { body } returns
                "{}".toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull())
            every { code } returns 200
            every { this@mockk.request } returns request
            every { isSuccessful } returns true
        }
        val chain = mockk<Interceptor.Chain>(relaxed = true) {
            every { request() } returns request
            every { proceed(request) } returns response
        }

        interceptor.intercept(chain)

        verify {
            logReporter.debug(
                """
                    --> GET https://example.com/core (2-byte body)
                    Content-Type: application/json; charset=utf-8
                    Content-Length: 2
                    Primer-SDK-Version: 2.19.2
                    Primer-SDK-Client: ANDROID_NATIVE
                    Primer-Client-Token: ****
                """.trimIndent()
            )
            logReporter.debug(
                """
                    <-- 200 GET https://example.com/core (0ms, 2-byte body)
                    Content-Type: application/json; charset=utf-8
                    Content-Length: 2
                    x-powered-by: Express
                    etag: "W/"847-yT0ll/7/DgvUQ6WdTDcRHMGcrHI"
                    function-execution-id: k6xuqcreanwy
                    x-cloud-trace-context: f0153994d00d137bd411f2f8bab24e59;o=1
                    date: Wed, 20 Dec 2023 13:05:11 GMT
                    server: Google Frontend
                    alt-svc: h3=":443"; ma=2592000,h3-29=":443"; ma=2592000
                    Primer-Client-Token: ****
                """.trimIndent()
            )
        }
    }

    @Test
    fun `should log body and obfuscate sensitive fields when intercept() is called and obfuscation is enabled`() {
        initInterceptor(primerLogger = customPrimerLogger)
        val request = mockk<Request> {
            every { url } returns "https://example.com/core".toHttpUrl()
            every { method } returns "GET"
            every { headers } returns emptyMap<String, String>().toHeaders()
            every { body } returns
                rawBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        }
        val response = mockk<Response> {
            every { headers } returns emptyMap<String, String>().toHeaders()
            every { body } returns
                rawBody.toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull())
            every { code } returns 200
            every { this@mockk.request } returns request
            every { isSuccessful } returns true
        }
        val chain = mockk<Interceptor.Chain>(relaxed = true) {
            every { request() } returns request
            every { proceed(request) } returns response
        }

        interceptor.intercept(chain)

        /* ktlint-disable max-line-length */
        val expectedBody = JSONObject(
            """{"customerId":null,"orderId":"android-test-356e6b98-07a3-4b2a-9857-1a5f7c553818","amount":"****","currencyCode":"****","order":{"countryCode":"NL","lineItems":[{"itemId":"****","description":"****","amount":"****","quantity":1,"discountAmount":"****"}]},"customer":{"emailAddress":"****","mobileNumber":"****","firstName":"****","lastName":"****","billingAddress":{"firstName":"****","lastName":"****","postalCode":"****","addressLine1":"****","countryCode":"****","city":"****","state":"****"},"shippingAddress":{"postalCode":"****","addressLine1":"****","countryCode":"****","city":"****","state":"****"},"nationalDocumentId":"****"},"paymentMethod":{"vaultOnSuccess":"****","descriptor":"****","vaultOn3DS":"****","options":{"PAYMENT_CARD":{"networks":{"JCB":{"surcharge":{"amount":"****"}}}},"PAYPAL":{"surcharge":{"amount":"****"}},"ADYEN_IDEAL":{"surcharge":{"amount":"****"}},"ADYEN_GIROPAY":{"surcharge":{"amount":"****"}},"ADYEN_SOFORT":{"surcharge":{"amount":"****"}},"ADYEN_TRUSTLY":{"surcharge":{"amount":"****"}},"KLARNA":{"surcharge":{"amount":"****"}},"GOOGLE_PAY":{"surcharge":{"amount":"****"}}}},"clientToken":"****","clientTokenExpirationDate":"****"}"""
        ).toString()

        verify {
            logReporter.debug(
                """
                    $expectedBody
                    --> END GET (1933-byte body)
                """.trimIndent()
            )
            logReporter.debug(
                """
                    $expectedBody
                    <-- END HTTP (1933-byte body)
                """.trimIndent()
            )
        }
        /* ktlint-enable  max-line-length */
    }

    @Test
    fun `should log headers and not obfuscate sensitive headers when intercept() is called and obfuscation is disabled`() {
        initInterceptor(primerLogger = consolePrimerLogger)
        val request = mockk<Request> {
            every { url } returns "https://example.com/core".toHttpUrl()
            every { method } returns "GET"
            every { headers } returns mapOf(
                "Content-Type" to "application/json; charset=utf-8",
                "Content-Length" to "2",
                "Primer-SDK-Version" to "2.19.2",
                "Primer-SDK-Client" to "ANDROID_NATIVE",
                "Primer-Client-Token" to "0928df7c-ed97-4c2b-9ca7-3774c9252b56"
            ).toHeaders()
            every { body } returns
                "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        }
        val response = mockk<Response> {
            every { headers } returns mapOf(
                "x-powered-by" to "Express",
                "content-type" to "application/json; charset=utf-8",
                "etag" to """"W/"847-yT0ll/7/DgvUQ6WdTDcRHMGcrHI"""",
                "function-execution-id" to "k6xuqcreanwy",
                "x-cloud-trace-context" to "f0153994d00d137bd411f2f8bab24e59;o=1",
                "date" to "Wed, 20 Dec 2023 13:05:11 GMT",
                "server" to "Google Frontend",
                "content-length" to "2119",
                "alt-svc" to """h3=":443"; ma=2592000,h3-29=":443"; ma=2592000""",
                "Primer-Client-Token" to "0928df7c-ed97-4c2b-9ca7-3774c9252b56"
            ).toHeaders()
            every { body } returns
                "{}".toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull())
            every { code } returns 200
            every { this@mockk.request } returns request
            every { isSuccessful } returns true
        }
        val chain = mockk<Interceptor.Chain>(relaxed = true) {
            every { request() } returns request
            every { proceed(request) } returns response
        }

        interceptor.intercept(chain)

        verify {
            logReporter.debug(
                """
                    --> GET https://example.com/core (2-byte body)
                    Content-Type: application/json; charset=utf-8
                    Content-Length: 2
                    Primer-SDK-Version: 2.19.2
                    Primer-SDK-Client: ANDROID_NATIVE
                    Primer-Client-Token: 0928df7c-ed97-4c2b-9ca7-3774c9252b56
                """.trimIndent()
            )
            logReporter.debug(
                """
                    <-- 200 GET https://example.com/core (0ms, 2-byte body)
                    Content-Type: application/json; charset=utf-8
                    Content-Length: 2
                    x-powered-by: Express
                    etag: "W/"847-yT0ll/7/DgvUQ6WdTDcRHMGcrHI"
                    function-execution-id: k6xuqcreanwy
                    x-cloud-trace-context: f0153994d00d137bd411f2f8bab24e59;o=1
                    date: Wed, 20 Dec 2023 13:05:11 GMT
                    server: Google Frontend
                    alt-svc: h3=":443"; ma=2592000,h3-29=":443"; ma=2592000
                    Primer-Client-Token: 0928df7c-ed97-4c2b-9ca7-3774c9252b56
                """.trimIndent()
            )
        }
    }

    @Test
    fun `should log body and not obfuscate sensitive fields when intercept() is called and obfuscation is disabled`() {
        initInterceptor(primerLogger = consolePrimerLogger)
        val request = mockk<Request> {
            every { url } returns "https://example.com/core".toHttpUrl()
            every { method } returns "GET"
            every { headers } returns emptyMap<String, String>().toHeaders()
            every { body } returns
                rawBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        }
        val response = mockk<Response> {
            every { headers } returns emptyMap<String, String>().toHeaders()
            every { body } returns
                rawBody.toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull())
            every { code } returns 200
            every { this@mockk.request } returns request
            every { isSuccessful } returns true
        }
        val chain = mockk<Interceptor.Chain>(relaxed = true) {
            every { request() } returns request
            every { proceed(request) } returns response
        }

        interceptor.intercept(chain)

        verify {
            logReporter.debug(
                """
                    $rawBody
                    --> END GET (1933-byte body)
                """.trimIndent()
            )
            logReporter.debug(
                """
                    $rawBody
                    <-- END HTTP (1933-byte body)
                """.trimIndent()
            )
        }
    }

    @Test
    fun `should log headers and not obfuscate sensitive headers when intercept() is called and isDebugBuild is true`() {
        initInterceptor(primerLogger = customPrimerLogger, isDebugBuild = true)
        val request = mockk<Request> {
            every { url } returns "https://example.com/core".toHttpUrl()
            every { method } returns "GET"
            every { headers } returns mapOf(
                "Content-Type" to "application/json; charset=utf-8",
                "Content-Length" to "2",
                "Primer-SDK-Version" to "2.19.2",
                "Primer-SDK-Client" to "ANDROID_NATIVE",
                "Primer-Client-Token" to "0928df7c-ed97-4c2b-9ca7-3774c9252b56"
            ).toHeaders()
            every { body } returns
                "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        }
        val response = mockk<Response> {
            every { headers } returns mapOf(
                "x-powered-by" to "Express",
                "content-type" to "application/json; charset=utf-8",
                "etag" to """"W/"847-yT0ll/7/DgvUQ6WdTDcRHMGcrHI"""",
                "function-execution-id" to "k6xuqcreanwy",
                "x-cloud-trace-context" to "f0153994d00d137bd411f2f8bab24e59;o=1",
                "date" to "Wed, 20 Dec 2023 13:05:11 GMT",
                "server" to "Google Frontend",
                "content-length" to "2119",
                "alt-svc" to """h3=":443"; ma=2592000,h3-29=":443"; ma=2592000""",
                "Primer-Client-Token" to "0928df7c-ed97-4c2b-9ca7-3774c9252b56"
            ).toHeaders()
            every { body } returns
                "{}".toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull())
            every { code } returns 200
            every { this@mockk.request } returns request
            every { isSuccessful } returns true
        }
        val chain = mockk<Interceptor.Chain>(relaxed = true) {
            every { request() } returns request
            every { proceed(request) } returns response
        }

        interceptor.intercept(chain)

        verify {
            logReporter.debug(
                """
                    --> GET https://example.com/core (2-byte body)
                    Content-Type: application/json; charset=utf-8
                    Content-Length: 2
                    Primer-SDK-Version: 2.19.2
                    Primer-SDK-Client: ANDROID_NATIVE
                    Primer-Client-Token: 0928df7c-ed97-4c2b-9ca7-3774c9252b56
                """.trimIndent()
            )
            logReporter.debug(
                """
                    <-- 200 GET https://example.com/core (0ms, 2-byte body)
                    Content-Type: application/json; charset=utf-8
                    Content-Length: 2
                    x-powered-by: Express
                    etag: "W/"847-yT0ll/7/DgvUQ6WdTDcRHMGcrHI"
                    function-execution-id: k6xuqcreanwy
                    x-cloud-trace-context: f0153994d00d137bd411f2f8bab24e59;o=1
                    date: Wed, 20 Dec 2023 13:05:11 GMT
                    server: Google Frontend
                    alt-svc: h3=":443"; ma=2592000,h3-29=":443"; ma=2592000
                    Primer-Client-Token: 0928df7c-ed97-4c2b-9ca7-3774c9252b56
                """.trimIndent()
            )
        }
    }

    @Test
    fun `should log body and not obfuscate sensitive fields when intercept() is called and isDebugBuild is true`() {
        initInterceptor(primerLogger = customPrimerLogger, isDebugBuild = true)
        val request = mockk<Request> {
            every { url } returns "https://example.com/core".toHttpUrl()
            every { method } returns "GET"
            every { headers } returns emptyMap<String, String>().toHeaders()
            every { body } returns
                rawBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        }
        val response = mockk<Response> {
            every { headers } returns emptyMap<String, String>().toHeaders()
            every { body } returns
                rawBody.toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull())
            every { code } returns 200
            every { this@mockk.request } returns request
            every { isSuccessful } returns true
        }
        val chain = mockk<Interceptor.Chain>(relaxed = true) {
            every { request() } returns request
            every { proceed(request) } returns response
        }

        interceptor.intercept(chain)

        verify {
            logReporter.debug(
                """
                    $rawBody
                    --> END GET (1933-byte body)
                """.trimIndent()
            )
            logReporter.debug(
                """
                    $rawBody
                    <-- END HTTP (1933-byte body)
                """.trimIndent()
            )
        }
    }

    @Test
    fun `should log headers and obfuscate sensitive headers when intercept() is called and with PCI url`() {
        initInterceptor(
            primerLogger = customPrimerLogger
        )
        val request = mockk<Request> {
            every { url } returns "https://pci.example.com/session".toHttpUrl()
            every { method } returns "GET"
            every { headers } returns mapOf(
                "Content-Type" to "application/json; charset=utf-8",
                "Content-Length" to "2",
                "Primer-SDK-Version" to "2.19.2",
                "Primer-SDK-Client" to "ANDROID_NATIVE",
                "Primer-Client-Token" to "0928df7c-ed97-4c2b-9ca7-3774c9252b56"
            ).toHeaders()
            every { body } returns
                "{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        }
        val response = mockk<Response> {
            every { headers } returns mapOf(
                "x-powered-by" to "Express",
                "content-type" to "application/json; charset=utf-8",
                "etag" to """"W/"847-yT0ll/7/DgvUQ6WdTDcRHMGcrHI"""",
                "function-execution-id" to "k6xuqcreanwy",
                "x-cloud-trace-context" to "f0153994d00d137bd411f2f8bab24e59;o=1",
                "date" to "Wed, 20 Dec 2023 13:05:11 GMT",
                "server" to "Google Frontend",
                "content-length" to "2119",
                "alt-svc" to """h3=":443"; ma=2592000,h3-29=":443"; ma=2592000""",
                "Primer-Client-Token" to "0928df7c-ed97-4c2b-9ca7-3774c9252b56"
            ).toHeaders()
            every { body } returns
                "{}".toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull())
            every { code } returns 200
            every { this@mockk.request } returns request
            every { isSuccessful } returns true
        }
        val chain = mockk<Interceptor.Chain>(relaxed = true) {
            every { request() } returns request
            every { proceed(request) } returns response
        }

        interceptor.intercept(chain)

        verify {
            logReporter.debug(
                """
                    --> GET https://pci.example.com/session (2-byte body)
                    Content-Type: application/json; charset=utf-8
                    Content-Length: 2
                    Primer-SDK-Version: 2.19.2
                    Primer-SDK-Client: ANDROID_NATIVE
                    Primer-Client-Token: ****
                """.trimIndent()
            )
            logReporter.debug(
                """
                    <-- 200 GET https://pci.example.com/session (0ms, 2-byte body)
                    Content-Type: application/json; charset=utf-8
                    Content-Length: 2
                    x-powered-by: Express
                    etag: "W/"847-yT0ll/7/DgvUQ6WdTDcRHMGcrHI"
                    function-execution-id: k6xuqcreanwy
                    x-cloud-trace-context: f0153994d00d137bd411f2f8bab24e59;o=1
                    date: Wed, 20 Dec 2023 13:05:11 GMT
                    server: Google Frontend
                    alt-svc: h3=":443"; ma=2592000,h3-29=":443"; ma=2592000
                    Primer-Client-Token: ****
                """.trimIndent()
            )
        }
    }

    @Test
    fun `should log body and obfuscate everything when intercept() is called and with PCI url`() {
        initInterceptor(
            primerLogger = customPrimerLogger,
            pciUrlProvider = { "https://pci.example.com" }
        )
        val request = mockk<Request> {
            every { url } returns "https://pci.example.com/session".toHttpUrl()
            every { method } returns "GET"
            every { headers } returns emptyMap<String, String>().toHeaders()
            every { body } returns
                rawBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        }
        val response = mockk<Response> {
            every { headers } returns emptyMap<String, String>().toHeaders()
            every { body } returns
                rawBody.toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull())
            every { code } returns 200
            every { this@mockk.request } returns request
            every { isSuccessful } returns true
        }
        val chain = mockk<Interceptor.Chain>(relaxed = true) {
            every { request() } returns request
            every { proceed(request) } returns response
        }

        interceptor.intercept(chain)

        verify {
            logReporter.debug(
                """
                    [sensitive data]
                    --> END GET
                """.trimIndent()
            )
            logReporter.debug(
                """
                    [sensitive data]
                    <-- END HTTP
                """.trimIndent()
            )
        }
    }

    @Test
    fun `should log error when intercept() is called but request throws exception`() {
        initInterceptor(primerLogger = consolePrimerLogger)
        val errorMessage = "Socket error"
        val request = mockk<Request> {
            every { url } returns "https://pci.example.com/session".toHttpUrl()
            every { method } returns "GET"
            every { headers } returns emptyMap<String, String>().toHeaders()
            every { body } returns
                rawBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        }
        val chain = mockk<Interceptor.Chain>(relaxed = true) {
            every { request() } returns request
            every { proceed(request) } throws IOException(errorMessage)
        }

        assertThrows<IOException> {
            interceptor.intercept(chain)
        }

        verify {
            logReporter.debug(
                """
                    <-- GET https://pci.example.com/session
                    HTTP failed: $errorMessage
                    <-- END HTTP
                """.trimIndent()
            )
        }
    }

    private fun initInterceptor(
        primerLogger: PrimerLogger = consolePrimerLogger,
        pciUrlProvider: () -> String? = { null },
        isDebugBuild: Boolean = false
    ) {
        PrimerLogging.logger = primerLogger
        interceptor = HttpLoggerInterceptor(
            logReporter = logReporter,
            level = HttpLoggerInterceptor.Level.BODY,
            blacklistedHttpHeaderProviderRegistry = mockk {
                every { getAll() } returns listOf(
                    mockk {
                        every { values } returns blacklistedHeaders
                    }
                )
            },
            whitelistedHttpBodyKeyProviderRegistry = mockk {
                every { getAll() } returns listOf(
                    mockk {
                        every { values } returns whitelistedBodyKeys
                    }
                )
            },
            pciUrlProvider = pciUrlProvider,
            getCurrentTimeMillis = { 0L },
            isDebugBuild = isDebugBuild
        )
    }
}
