package io.primer.android.internal.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.ui.core.data.repository.CountriesDataRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCountriesUseCaseTest {

    private lateinit var useCase: GetCountriesUseCase
    private lateinit var mockCountriesRepository: CountriesDataRepository

    @BeforeEach
    fun setUp() {
        mockCountriesRepository = mockk()
        useCase = GetCountriesUseCase(mockCountriesRepository)
    }

    @Test
    fun `invoke should return countries from repository`() = runTest {
        val expectedCountries = listOf(
            PrimerCountry("United States", CountryCode.US),
            PrimerCountry("United Kingdom", CountryCode.GB),
        )
        coEvery { mockCountriesRepository.getCountries() } returns expectedCountries

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedCountries, result.getOrNull())
        coVerify(exactly = 1) { mockCountriesRepository.getCountries() }
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        val expectedException = RuntimeException("Failed to load countries")
        coEvery { mockCountriesRepository.getCountries() } throws expectedException

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    @Test
    fun `findByQuery should return filtered countries`() = runTest {
        val query = "United"
        val expectedCountries = listOf(
            PrimerCountry("United States", CountryCode.US),
            PrimerCountry("United Kingdom", CountryCode.GB),
        )
        coEvery { mockCountriesRepository.findCountryByQuery(query) } returns expectedCountries

        val result = useCase.findByQuery(query)

        assertEquals(expectedCountries, result)
        coVerify(exactly = 1) { mockCountriesRepository.findCountryByQuery(query) }
    }

    @Test
    fun `findByQuery should return empty list when no matches`() = runTest {
        val query = "xyz"
        coEvery { mockCountriesRepository.findCountryByQuery(query) } returns emptyList()

        val result = useCase.findByQuery(query)

        assertTrue(result.isEmpty())
    }
}
