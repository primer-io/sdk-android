package io.primer.android.components

import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.components.domain.core.models.bancontact.PrimerRawBancontactCardData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.payment.async.bancontact.AdyenBancontactCardPaymentMethodDescriptor
import org.junit.Test
import kotlin.test.assertEquals

internal class RawBancontactCardTest {

    @Test
    fun `setTokenizableValues() should add into values param correct key values of card data`() {
        val rawData = PrimerRawBancontactCardData(
            CARD_NUMBER,
            CARD_EXPIRY_MONTH,
            CARD_EXPIRY_YEAR,
            CARDHOLDER_NAME
        )

        val descriptor = mockk<AdyenBancontactCardPaymentMethodDescriptor>(relaxed = true)

        val result = rawData.setTokenizableValues(descriptor, "")

        coVerify {
            descriptor.setTokenizableField(PrimerInputElementType.CARD_NUMBER, CARD_NUMBER)
            descriptor.setTokenizableField(PrimerInputElementType.EXPIRY_MONTH, CARD_EXPIRY_MONTH)
            descriptor.setTokenizableField(PrimerInputElementType.EXPIRY_YEAR, CARD_EXPIRY_YEAR)
            descriptor.setTokenizableField(PrimerInputElementType.CARDHOLDER_NAME, CARDHOLDER_NAME)
            descriptor.appendTokenizableValue(SESSION_INFO, BROWSER_INFO, USER_AGENT, any())
        }

        assertEquals(descriptor, result)
    }

    internal companion object {
        private const val CARD_NUMBER = "4871049999999910"
        private const val CARD_EXPIRY_MONTH = "03"
        private const val CARD_EXPIRY_YEAR = "2030"
        private const val CARDHOLDER_NAME = "Tester"

        private const val SESSION_INFO = "sessionInfo"
        private const val BROWSER_INFO = "browserInfo"
        private const val USER_AGENT = "userAgent"
    }
}
