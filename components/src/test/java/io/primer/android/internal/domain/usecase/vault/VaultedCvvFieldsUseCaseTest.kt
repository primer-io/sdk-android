package io.primer.android.internal.domain.usecase.vault

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VaultedCvvFieldsUseCaseTest {

    private lateinit var useCase: VaultedCvvFieldsUseCase

    @BeforeEach
    fun setUp() {
        useCase = VaultedCvvFieldsUseCase()
    }

    @Test
    fun `expected cvv length updates according to bin data`() = runTest {
        assertEquals(3, useCase.expectedCvvLength.first())

        useCase.updateFirst6Digits("378282")
        assertEquals(4, useCase.expectedCvvLength.first())

        useCase.updateFirst6Digits("424242")
        assertEquals(3, useCase.expectedCvvLength.first())
    }

    @Test
    fun `isCvvValid returns true only when format and length match`() = runTest {
        useCase.updateFirst6Digits("378282")
        useCase.updateCvv("1234")
        assertTrue(useCase.isCvvValid.first())

        useCase.updateCvv("123")
        assertFalse(useCase.isCvvValid.first())

        useCase.updateFirst6Digits("424242")
        useCase.updateCvv("12a")
        assertFalse(useCase.isCvvValid.first())
    }

    @Test
    fun `clear resets currently typed cvv`() {
        useCase.updateCvv("123")
        assertEquals("123", useCase.cvvValue.value)

        useCase.clear()
        assertEquals("", useCase.cvvValue.value)
    }
}
