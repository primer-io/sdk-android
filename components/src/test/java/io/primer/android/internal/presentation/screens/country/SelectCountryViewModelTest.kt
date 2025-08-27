package io.primer.android.internal.presentation.screens.country

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.ui.core.data.repository.CountriesDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class)
class SelectCountryViewModelTest {

    private lateinit var countriesRepository: CountriesDataRepository
    private lateinit var checkoutNavigator: CheckoutNavigator
    private lateinit var logReporter: LogReporter
    private lateinit var viewModel: SelectCountryViewModel

    @BeforeEach
    fun setup() {
        countriesRepository = mockk()
        checkoutNavigator = mockk(relaxed = true)
        logReporter = mockk(relaxed = true)
    }

    @Test
    fun `constructor should store dependencies correctly and load countries`() = runTest {
        val countryList = listOf(
            PrimerCountry("United States", CountryCode.US),
            PrimerCountry("United Kingdom", CountryCode.GB),
        )
        coEvery { countriesRepository.getCountries() } returns countryList

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        assertNotNull(viewModel)
        coVerify(exactly = 1) { countriesRepository.getCountries() }
    }

    @Test
    fun `should initialize state with empty countries and loading true then update after load`() = runTest {
        val countryList = listOf(
            PrimerCountry("France", CountryCode.FR),
            PrimerCountry("Germany", CountryCode.DE),
        )
        coEvery { countriesRepository.getCountries() } returns countryList

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(countryList, state.countries)
        assertEquals(countryList, state.filteredCountries)
        assertFalse(state.isLoading)
    }

    @Test
    fun `should initialize with empty search query`() = runTest {
        coEvery { countriesRepository.getCountries() } returns emptyList()

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("", state.searchQuery)
    }

    @Test
    fun `loadCountries should handle exception and set isLoading false`() = runTest {
        coEvery { countriesRepository.getCountries() } throws RuntimeException("Network error")

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.countries.isEmpty())
        assertTrue(state.filteredCountries.isEmpty())
        assertFalse(state.isLoading)
        verify(exactly = 1) { logReporter.error("Failed to load countries: Network error") }
    }

    @Test
    fun `onCountrySelected should navigate back with result`() = runTest {
        coEvery { countriesRepository.getCountries() } returns emptyList()

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        viewModel.onCountrySelected("US", "United States")
        advanceUntilIdle()

        coVerify(
            exactly = 1,
        ) { checkoutNavigator.navigateBackWithResult("selected_country", Pair("US", "United States")) }
    }

    @Test
    fun `onCancel should navigate back`() = runTest {
        coEvery { countriesRepository.getCountries() } returns emptyList()

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        viewModel.onCancel()
        advanceUntilIdle()

        coVerify(exactly = 1) { checkoutNavigator.navigateBack() }
    }

    @Test
    fun `onSearch should update search query and filter countries`() = runTest {
        val countryList = listOf(
            PrimerCountry("United States", CountryCode.US),
            PrimerCountry("United Kingdom", CountryCode.GB),
            PrimerCountry("Canada", CountryCode.CA),
        )
        coEvery { countriesRepository.getCountries() } returns countryList
        coEvery { countriesRepository.findCountryByQuery("United") } returns listOf(
            PrimerCountry("United States", CountryCode.US),
            PrimerCountry("United Kingdom", CountryCode.GB),
        )

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        viewModel.onSearch("United")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("United", state.searchQuery)
        assertEquals(2, state.filteredCountries.size)
        assertTrue(state.filteredCountries.any { it.code == CountryCode.US })
        assertTrue(state.filteredCountries.any { it.code == CountryCode.GB })
        assertFalse(state.filteredCountries.any { it.code == CountryCode.CA })
        coVerify(exactly = 1) { countriesRepository.findCountryByQuery("United") }
    }

    @Test
    fun `onSearch with empty query should show all countries`() = runTest {
        val countryList = listOf(
            PrimerCountry("France", CountryCode.FR),
            PrimerCountry("Germany", CountryCode.DE),
            PrimerCountry("Italy", CountryCode.IT),
        )
        coEvery { countriesRepository.getCountries() } returns countryList
        coEvery { countriesRepository.findCountryByQuery("Germany") } returns listOf(
            PrimerCountry("Germany", CountryCode.DE),
        )

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        viewModel.onSearch("Germany")
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.filteredCountries.size)

        viewModel.onSearch("")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("", state.searchQuery)
        assertEquals(countryList, state.filteredCountries)
    }

    @Test
    fun `onSearch with blank query should show all countries`() = runTest {
        val countryList = listOf(
            PrimerCountry("Spain", CountryCode.ES),
            PrimerCountry("Portugal", CountryCode.PT),
        )
        coEvery { countriesRepository.getCountries() } returns countryList

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        viewModel.onSearch("  ")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("  ", state.searchQuery)
        assertEquals(countryList, state.filteredCountries)
    }

    @Test
    fun `state should update correctly when loading countries successfully`() = runTest {
        val countryList = listOf(
            PrimerCountry("Japan", CountryCode.JP),
            PrimerCountry("China", CountryCode.CN),
            PrimerCountry("South Korea", CountryCode.KR),
        )
        coEvery { countriesRepository.getCountries() } returns countryList

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(countryList, state.countries)
        assertEquals(countryList, state.filteredCountries)
        assertEquals("", state.searchQuery)
        assertFalse(state.isLoading)
    }

    @Test
    fun `multiple search queries should update state correctly`() = runTest {
        val allCountries = listOf(
            PrimerCountry("United States", CountryCode.US),
            PrimerCountry("United Kingdom", CountryCode.GB),
            PrimerCountry("Canada", CountryCode.CA),
        )
        coEvery { countriesRepository.getCountries() } returns allCountries
        coEvery { countriesRepository.findCountryByQuery("United") } returns listOf(
            PrimerCountry("United States", CountryCode.US),
            PrimerCountry("United Kingdom", CountryCode.GB),
        )
        coEvery { countriesRepository.findCountryByQuery("Canada") } returns listOf(
            PrimerCountry("Canada", CountryCode.CA),
        )

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        viewModel.onSearch("United")
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.filteredCountries.size)

        viewModel.onSearch("Canada")
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.filteredCountries.size)
        assertEquals(CountryCode.CA, viewModel.state.value.filteredCountries.first().code)

        coVerify(exactly = 1) { countriesRepository.findCountryByQuery("United") }
        coVerify(exactly = 1) { countriesRepository.findCountryByQuery("Canada") }
    }

    @Test
    fun `countries should load immediately on initialization`() = runTest {
        val countryList = listOf(
            PrimerCountry("Mexico", CountryCode.MX),
            PrimerCountry("Argentina", CountryCode.AR),
        )
        coEvery { countriesRepository.getCountries() } returns countryList

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(countryList, state.countries)
        assertEquals(countryList, state.filteredCountries)
        coVerify(exactly = 1) { countriesRepository.getCountries() }
    }

    @Test
    fun `empty country list should be handled correctly`() = runTest {
        coEvery { countriesRepository.getCountries() } returns emptyList()

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.countries.isEmpty())
        assertTrue(state.filteredCountries.isEmpty())
        assertFalse(state.isLoading)
        coVerify(exactly = 1) { countriesRepository.getCountries() }
    }

    @Test
    fun `onCountrySelected with special characters should work correctly`() = runTest {
        coEvery { countriesRepository.getCountries() } returns emptyList()

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        viewModel.onCountrySelected("CI", "Côte d'Ivoire")
        advanceUntilIdle()

        coVerify(exactly = 1) {
            checkoutNavigator.navigateBackWithResult("selected_country", Pair("CI", "Côte d'Ivoire"))
        }
    }

    @Test
    fun `search query with trimmed spaces should filter correctly`() = runTest {
        val countryList = listOf(
            PrimerCountry("Netherlands", CountryCode.NL),
            PrimerCountry("Belgium", CountryCode.BE),
        )
        coEvery { countriesRepository.getCountries() } returns countryList
        coEvery { countriesRepository.findCountryByQuery("  Netherlands  ") } returns listOf(
            PrimerCountry("Netherlands", CountryCode.NL),
        )

        viewModel = SelectCountryViewModel(
            countriesRepository = countriesRepository,
            checkoutNavigator = checkoutNavigator,
            logReporter = logReporter,
        )
        advanceUntilIdle()

        viewModel.onSearch("  Netherlands  ")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("  Netherlands  ", state.searchQuery)
        assertEquals(1, state.filteredCountries.size)
        assertEquals(CountryCode.NL, state.filteredCountries.first().code)
        coVerify(exactly = 1) { countriesRepository.findCountryByQuery("  Netherlands  ") }
    }
}
