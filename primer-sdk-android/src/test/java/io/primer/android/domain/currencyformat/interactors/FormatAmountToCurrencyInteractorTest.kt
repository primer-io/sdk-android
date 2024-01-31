package io.primer.android.domain.currencyformat.interactors

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.primer.android.InstantExecutorExtension
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.currencyformat.models.CurrencyFormat
import io.primer.android.domain.currencyformat.models.FormatCurrencyParams
import io.primer.android.domain.currencyformat.repository.CurrencyFormatRepository
import io.primer.android.model.MonetaryAmount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.text.DecimalFormatSymbols
import java.util.Locale

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class FormatAmountToCurrencyInteractorTest {

    @RelaxedMockK
    internal lateinit var currencyFormatRepository: CurrencyFormatRepository

    @RelaxedMockK
    internal lateinit var primerSettings: PrimerSettings

    private lateinit var interactor: FormatAmountToCurrencyInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor = FormatAmountToCurrencyInteractor(currencyFormatRepository, primerSettings)
        every { primerSettings.locale }.returns(Locale.US)
        every { currencyFormatRepository.getCurrencyFormats() }.returns(
            listOf(
                CurrencyFormat("EUR", 2),
                CurrencyFormat("USD", 2),
                CurrencyFormat("GBP", 2),
                CurrencyFormat("JPY", 0),
                CurrencyFormat("KRW", 0),
                CurrencyFormat("SEK", 2),
                CurrencyFormat("CNY", 2),
                CurrencyFormat("HUF", 2),
                CurrencyFormat("AUD", 2)
            )
        )
    }

    @Test
    fun `execute() should format 0 USD correctly`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("USD", 0)!!)
        Assertions.assertEquals("$0${decimalSeparator}00", interactor(params))
    }

    @Test
    fun `execute() should format USD correctly`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("USD", AMOUNT)!!)
        Assertions.assertEquals("$1${decimalSeparator}00", interactor(params))
    }

    @Test
    fun `execute() should format GBP correctly`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("GBP", AMOUNT)!!)
        Assertions.assertEquals("£1${decimalSeparator}00", interactor(params))
    }

    @Test
    fun `execute() should format EUR correctly`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("EUR", AMOUNT)!!)
        Assertions.assertEquals("€1${decimalSeparator}00", interactor(params))
    }

    @Test
    fun `execute() should format JPY correctly`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("JPY", AMOUNT)!!)
        Assertions.assertEquals("¥100", interactor(params))
    }

    @Test
    fun `execute() should format KRW correctly`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("KRW", AMOUNT)!!)
        Assertions.assertEquals("₩100", interactor(params))
    }

    @Test
    fun `execute() should format SEK correctly`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("SEK", AMOUNT)!!)
        Assertions.assertEquals("SEK1${decimalSeparator}00", interactor(params))
    }

    @Test
    fun `execute() should format CNY correctly`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("CNY", AMOUNT)!!)
        Assertions.assertEquals("CN¥1${decimalSeparator}00", interactor(params))
    }

    @Test
    fun `execute() should format HUF correctly`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("HUF", 999999999)!!)
        Assertions.assertEquals(
            "HUF9${groupingSeparator}999${groupingSeparator}999${decimalSeparator}99",
            interactor(params)
        )
    }

    @Test
    fun `execute() should format huge USD correctly`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("USD", 999999999)!!)
        Assertions.assertEquals(
            "$9${groupingSeparator}999${groupingSeparator}999${decimalSeparator}99",
            interactor(params)
        )
    }

    @Test
    fun `execute() should format huge JPY correctly`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("JPY", 999999999)!!)
        Assertions.assertEquals(
            "¥999${groupingSeparator}999${groupingSeparator}999",
            interactor(params)
        )
    }

    @Test
    fun `execute() should format small USD correctly`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("USD", 1)!!)
        Assertions.assertEquals("$0${decimalSeparator}01", interactor(params))
    }

    @Test
    fun `amountToCurrencyString should format small JPY correctly`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("JPY", 1)!!)
        Assertions.assertEquals("¥1", interactor(params))
    }

    @Test
    fun `amountToCurrencyString should format USD correctly with UK Locale`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("USD", 100)!!)
        every { primerSettings.locale }.returns((Locale.UK))
        Assertions.assertEquals("US$1.00", interactor(params))
    }

    private companion object {
        const val AMOUNT = 100
        val groupingSeparator = DecimalFormatSymbols.getInstance().groupingSeparator
        val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
    }
}
