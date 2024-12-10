package io.primer.android.vouchers.retailOutlets.implementation.validation.domain

import io.mockk.coEvery
import io.mockk.mockk
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models.RetailOutlet
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.repository.RetailOutletRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class XenditRetailerOutletValidatorTest {

    private val retailOutletRepository: RetailOutletRepository = mockk()
    private lateinit var validator: XenditRetailerOutletValidator

    @BeforeEach
    fun setUp() {
        validator = XenditRetailerOutletValidator(retailOutletRepository)
    }

    @Test
    fun `validate should return error for null input`() = runTest {
        val result = validator.validate(null)
        assertEquals(
            PrimerInputValidationError(
                "invalid-retailer",
                "[invalid-retailer] Retailer outlet cannot be blank.",
                PrimerInputElementType.RETAIL_OUTLET
            ),
            result
        )
    }

    @Test
    fun `validate should return error for blank input`() = runTest {
        val result = validator.validate("")
        assertEquals(
            PrimerInputValidationError(
                "invalid-retailer",
                "[invalid-retailer] Retailer outlet cannot be blank.",
                PrimerInputElementType.RETAIL_OUTLET
            ),
            result
        )
    }

    @Test
    fun `validate should return error for invalid retailer ID`() = runTest {
        val invalidRetailerId = "invalid_id"
        coEvery { retailOutletRepository.getCachedRetailOutlets() } returns listOf(
            RetailOutlet("valid_id", "Valid Retailer", false, "https://valid.url")
        )

        val result = validator.validate(invalidRetailerId)
        assertEquals(
            PrimerInputValidationError(
                "invalid-retailer",
                "[invalid-retailer] Retailer outlet ID can be only from list of retailers.",
                PrimerInputElementType.RETAIL_OUTLET
            ),
            result
        )
    }

    @Test
    fun `validate should return null for valid retailer ID`() = runTest {
        val validRetailerId = "valid_id"
        coEvery { retailOutletRepository.getCachedRetailOutlets() } returns listOf(
            RetailOutlet(validRetailerId, "Valid Retailer", false, "https://valid.url")
        )

        val result = validator.validate(validRetailerId)
        assertNull(result)
    }
}
