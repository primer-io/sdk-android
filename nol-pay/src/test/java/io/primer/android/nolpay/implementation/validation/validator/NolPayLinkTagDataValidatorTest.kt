package io.primer.android.nolpay.implementation.validation.validator

import android.nfc.Tag
import io.mockk.mockk
import io.primer.android.nolpay.api.manager.linkCard.composable.NolPayLinkCollectableData
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class NolPayLinkTagDataValidatorTest {
    private val validator = NolPayLinkTagDataValidator()

    @Test
    fun `validate should return empty list when validation is successful`() =
        runTest {
            // Given
            val tagData = NolPayLinkCollectableData.NolPayTagData(mockk<Tag>())

            // When
            val result = validator.validate(tagData)

            // Then
            assert(result.isSuccess)
            val validationErrors = result.getOrThrow()
            assertEquals(emptyList(), validationErrors)
        }
}
