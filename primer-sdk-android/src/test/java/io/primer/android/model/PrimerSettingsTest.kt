package io.primer.android.model

import io.primer.android.model.dto.Order
import io.primer.android.model.dto.PrimerSettings
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PrimerSettingsTest {

    @Test
    fun `currentAmount throws if no amount value present`() {
        val settings = PrimerSettings()
        val exception = assertThrows<Exception> { settings.currentAmount }
        assert(exception.message == PrimerSettings.AMOUNT_EXCEPTION)
    }

    @Test
    fun `currentAmount is totalOrderAmount if order merchantAmount not present`() {
        val settings = PrimerSettings(
            order = Order(totalOrderAmount = 200)
        )
        assert(settings.currentAmount == 200)
    }

    @Test
    fun `currentAmount is amount if order amount values not present`() {
        val settings = PrimerSettings(
            order = Order()
        )
        assert(settings.currentAmount == 150)
    }
}
