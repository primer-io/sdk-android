package io.primer.android.webRedirectShared.implementation.deeplink.data.repository

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.paymentmethods.common.utils.Constants
import io.primer.android.webRedirectShared.implementation.deeplink.domain.repository.RedirectDeeplinkRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RedirectDeeplinkDataRepositoryTest {
    private lateinit var applicationIdProvider: BaseDataProvider<String>
    private lateinit var redirectDeeplinkRepository: RedirectDeeplinkRepository

    @BeforeEach
    fun setUp() {
        applicationIdProvider = mockk()
        redirectDeeplinkRepository = RedirectDeeplinkDataRepository(applicationIdProvider)
    }

    @AfterEach
    fun tearDown() {
        // Clear static mocks and constructor mocks
        unmockkAll()
    }

    @Test
    fun `getDeeplinkUrl should return the correct deeplink URL`() =
        runTest {
            // Arrange
            val applicationId = "test_app_id"
            every { applicationIdProvider.provide() } returns applicationId

            val expectedUrl = "expected_url"

            val uriMock = mockk<Uri>()
            val uriBuilder = mockk<Uri.Builder>()

            mockkConstructor(Uri.Builder::class).also {
                every { anyConstructed<Uri.Builder>().scheme(any()) } returns uriBuilder
                every { uriBuilder.authority(any()) } returns uriBuilder
                every { uriBuilder.appendPath(any()) } returns uriBuilder
                every { uriBuilder.build() } returns uriMock
                every { uriMock.toString() }.returns(expectedUrl)
            }

            // Act
            val actualUrl = redirectDeeplinkRepository.getDeeplinkUrl()

            // Assert
            assertEquals(expectedUrl, actualUrl)

            verify { anyConstructed<Uri.Builder>().scheme(Constants.PRIMER_REDIRECT_SCHEMA) }
            verify { uriBuilder.authority("${Constants.PRIMER_REDIRECT_PREFIX}$applicationId") }
            verify { uriBuilder.appendPath("async") }
            verify { uriBuilder.build() }
        }
}
