package io.primer.android.vouchers.retailOutlets.implementation.validation.domain

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.primer.android.PrimerRetailerData
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models.RetailOutlet
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.repository.RetailOutletRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RetailerOutletInputValidatorTest {
    private val retailOutletRepository: RetailOutletRepository = mockk()
    private lateinit var validator: RetailerOutletInputValidator

    @BeforeEach
    fun setUp() {
        validator = RetailerOutletInputValidator(retailOutletRepository)
    }

    @Test
    fun `validate should return no errors for valid input`() =
        runTest {
            val validRetailerData = PrimerRetailerData(id = "valid_id")
            val retailOutlet =
                mockk<RetailOutlet> {
                    every { id } returns "valid_id"
                }

            coEvery { retailOutletRepository.getCachedRetailOutlets() } returns listOf(retailOutlet)

            val errors = validator.validate(validRetailerData)

            assertTrue(errors.isEmpty(), "Expected no validation errors")
        }

    @Test
    fun `validate should return errors for invalid input`() =
        runTest {
            val invalidRetailerData = PrimerRetailerData(id = "")

            val errors = validator.validate(invalidRetailerData)

            assertTrue(errors.isNotEmpty(), "Expected validation errors")
        }
}
