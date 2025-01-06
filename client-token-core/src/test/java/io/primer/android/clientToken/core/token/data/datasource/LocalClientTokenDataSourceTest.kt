package io.primer.android.clientToken.core.token.data.datasource

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.primer.android.clientToken.core.token.data.model.ClientToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class LocalClientTokenDataSourceTest {
    private lateinit var localClientTokenDataSource: LocalClientTokenDataSource
    private lateinit var initialClientToken: ClientToken

    @BeforeEach
    fun setUp() {
        mockkObject(ClientToken.Companion)
        initialClientToken =
            mockk<ClientToken> {
                every { configurationUrl } returns "https://primer.io"
                every { accessToken } returns "initialToken"
            }
        localClientTokenDataSource = LocalClientTokenDataSource()
    }

    @Test
    fun `get returns the current client token`() {
        every { ClientToken.fromString(any()) } returns initialClientToken

        // Act
        localClientTokenDataSource.update("token")
        val result = localClientTokenDataSource.get()

        // Assert
        assertEquals(initialClientToken, result)
    }

    @Test
    fun `update with valid token updates the client token`() {
        // Arrange
        val newClientTokenString = "newToken"

        every { ClientToken.fromString(any()) } returns initialClientToken

        // Act
        localClientTokenDataSource.update(newClientTokenString)

        // Assert
        val result = localClientTokenDataSource.get()
        assertEquals(initialClientToken, result)
    }

    @Test
    fun `update with blank configuration URL retains the old configuration URL`() {
        // Arrange
        val newClientTokenString = "newTokenWithBlankUrl"

        val emptyClientToken =
            mockk<ClientToken> {
                every { configurationUrl } returns ""
                every { accessToken } returns "initialToken"
                every { copy(configurationUrl = any()) } returns initialClientToken
            }

        every { ClientToken.fromString(any()) } returnsMany (listOf(initialClientToken, emptyClientToken))

        // Act
        localClientTokenDataSource.update(newClientTokenString)

        // Assert
        val result = localClientTokenDataSource.get()
        assertEquals(initialClientToken.configurationUrl, result.configurationUrl)
    }
}
