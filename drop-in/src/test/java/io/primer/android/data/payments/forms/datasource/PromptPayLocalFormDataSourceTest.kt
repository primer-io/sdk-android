package io.primer.android.data.payments.forms.datasource

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.primer.android.R
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.data.payments.forms.models.ButtonType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PromptPayLocalFormDataSourceTest {
    private lateinit var theme: PrimerTheme
    private lateinit var dataSource: PromptPayLocalFormDataSource

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        theme = mockk<PrimerTheme>()
        dataSource = PromptPayLocalFormDataSource(theme)
    }

    @Test
    fun `get() returns correct FormDataResponse in dark mode`() = runTest {
        every { theme.isDarkMode } returns true

        val result = dataSource.get().first()

        assertEquals(R.string.scanToPay, result.title)
        assertEquals(R.drawable.ic_logo_promptpay_dark, result.logo)
        assertEquals(ButtonType.CONFIRM, result.buttonType)
        assertEquals(R.string.uploadScreenshot, result.description)
    }

    @Test
    fun `get() returns correct FormDataResponse in light mode`() = runTest {
        every { theme.isDarkMode } returns false

        val result = dataSource.get().first()

        assertEquals(R.string.scanToPay, result.title)
        assertEquals(R.drawable.ic_logo_promptpay_light, result.logo)
        assertEquals(ButtonType.CONFIRM, result.buttonType)
        assertEquals(R.string.uploadScreenshot, result.description)
    }
}
