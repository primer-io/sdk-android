package io.primer.android.components.manager.raw

import io.mockk.mockk
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodBinData
import io.primer.android.components.domain.error.PrimerInputValidationError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PrimerHeadlessUniversalCheckoutRawDataManagerListenerTest {
    @Test
    fun `onBinDataAvailable should have a no-op default implementation`() {
        val listener = object : PrimerHeadlessUniversalCheckoutRawDataManagerListener {
            override fun onValidationChanged(
                isValid: Boolean,
                errors: List<PrimerInputValidationError>,
            ) = Unit
        }
        val result = listener.onBinDataAvailable(mockk<PrimerPaymentMethodBinData>())
        assertEquals(Unit, result)
    }
}
