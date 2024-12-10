package io.primer.android.clientToken.core.token.data.repository

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import io.primer.android.clientToken.core.token.data.datasource.LocalClientTokenDataSource
import io.primer.android.clientToken.core.token.data.model.ClientToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ClientTokenDataRepositoryTest {

    private lateinit var clientTokenDataSource: LocalClientTokenDataSource
    private lateinit var clientTokenDataRepository: ClientTokenDataRepository
    private lateinit var clientToken: ClientToken

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        clientTokenDataSource = mockk()
        clientTokenDataRepository = ClientTokenDataRepository(clientTokenDataSource)
        clientToken = mockk<ClientToken> {
            every { intent } returns "intent"
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `setClientToken calls data source update with valid client token`() {
        // Arrange
        val newClientTokenString = "newValidClientToken"
        every { clientTokenDataSource.update(any()) } just Runs

        // Act
        clientTokenDataRepository.setClientToken(newClientTokenString)

        // Assert
        verify { clientTokenDataSource.update(newClientTokenString) }
    }

    @Test
    fun `setClientToken throws IllegalArgumentException for invalid client token`() {
        // Arrange
        val invalidClientTokenString = ""

        every { clientTokenDataSource.update(any()) } throws IllegalArgumentException()

        // Act & Assert
        assertThrows(IllegalArgumentException::class.java) {
            clientTokenDataRepository.setClientToken(invalidClientTokenString)
        }
    }

    @Test
    fun `getClientTokenIntent returns correct intent from data source`() {
        // Arrange
        every { clientTokenDataSource.get() } returns clientToken

        // Act
        val result = clientTokenDataRepository.getClientTokenIntent()

        // Assert
        assertEquals(clientToken.intent, result)
    }
}
