package io.primer.android.components.currencyformat.domain.interactors

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.primer.android.components.currencyformat.domain.models.CurrencyFormat
import io.primer.android.components.currencyformat.domain.models.FormatCurrencyParams
import io.primer.android.components.currencyformat.domain.repository.CurrencyFormatRepository
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.MonetaryAmount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Locale

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
internal class FormatAmountToDecimalInteractorTest {
    @RelaxedMockK
    internal lateinit var currencyFormatRepository: CurrencyFormatRepository

    @RelaxedMockK
    internal lateinit var primerSettings: PrimerSettings

    private lateinit var interactor: FormatAmountToDecimalInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor = FormatAmountToDecimalInteractor(currencyFormatRepository, primerSettings)
        every { primerSettings.locale }.returns(Locale.US)
    }

    @Test
    fun `execute() should format the amount into the correct string format`() {
        val params = FormatCurrencyParams(MonetaryAmount.create("EUR", 123)!!)
        every { currencyFormatRepository.getCurrencyFormats() }.returns(
            listOf(
                CurrencyFormat(
                    "EUR",
                    2,
                ),
            ),
        )

        runTest {
            val result = interactor(params)
            Assertions.assertEquals("1.23", result)
        }
    }
}
