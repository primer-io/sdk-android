package io.primer.android.webRedirectShared.implementation.deeplink.domain

import io.mockk.every
import io.mockk.mockk
import io.primer.android.core.domain.None
import io.primer.android.webRedirectShared.implementation.deeplink.domain.repository.RedirectDeeplinkRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DefaultRedirectDeeplinkInteractorTest {
    @Test
    fun `execute should return the deeplink URL from the repository`() {
        // Arrange
        val expectedUrl = "https://example.com/deeplink"
        val deeplinkRepository = mockk<RedirectDeeplinkRepository>()
        every { deeplinkRepository.getDeeplinkUrl() } returns expectedUrl
        val interactor = DefaultRedirectDeeplinkInteractor(deeplinkRepository)

        // Act
        val actualUrl = interactor.execute(None)

        // Assert
        assertEquals(expectedUrl, actualUrl)
    }
}
