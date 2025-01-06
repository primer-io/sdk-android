package io.primer.android.banks.implementation.rpc.data.datasource

import io.mockk.mockk
import io.primer.android.banks.implementation.rpc.data.models.IssuingBankDataResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LocalIssuingBankDataSourceTest {
    private lateinit var dataSource: LocalIssuingBankDataSource

    @BeforeEach
    fun setUp() {
        dataSource = LocalIssuingBankDataSource()
    }

    @Test
    fun `test get returns empty list initially`() {
        // Act
        val result = dataSource.get()

        // Assert
        assertEquals(emptyList<IssuingBankDataResponse>(), result)
    }

    @Test
    fun `test update updates the list correctly`() {
        // Arrange
        val inputList =
            listOf(
                mockk<IssuingBankDataResponse>(relaxed = true),
                mockk<IssuingBankDataResponse>(relaxed = true),
            )

        // Act
        dataSource.update(inputList)
        val result = dataSource.get()

        // Assert
        assertEquals(inputList, result)
    }

    @Test
    fun `test update clears existing list and adds new items`() {
        // Arrange
        val initialList =
            listOf(
                mockk<IssuingBankDataResponse>(relaxed = true),
            )
        dataSource.update(initialList)

        val updatedList =
            listOf(
                mockk<IssuingBankDataResponse>(relaxed = true),
                mockk<IssuingBankDataResponse>(relaxed = true),
            )

        // Act
        dataSource.update(updatedList)
        val result = dataSource.get()

        // Assert
        assertEquals(updatedList, result)
    }
}
