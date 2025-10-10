package io.primer.android.surcharge.utils

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.primer.android.R
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.domain.model.Surcharge
import io.primer.android.core.domain.None
import io.primer.android.data.settings.internal.MonetaryAmount
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.ui.core.domain.FormatAmountToCurrencyInteractor
import io.primer.android.ui.core.payment.domain.interactor.SurchargeInteractor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Currency

internal class SurchargeFormatterTest {

    private lateinit var surchargeFormatter: SurchargeFormatter
    private val amountToCurrencyInteractor: FormatAmountToCurrencyInteractor = mockk()
    private val surchargeInteractor: SurchargeInteractor = mockk()
    private val currency: Currency = mockk()
    private val context: Context = mockk()

    @BeforeEach
    fun setUp() {
        surchargeFormatter = SurchargeFormatter(
            amountToCurrencyInteractor = amountToCurrencyInteractor,
            surchargeInteractor = surchargeInteractor,
            currency = currency,
        )
    }

    @Test
    fun `getSurchargeForSavedPaymentMethod returns 0 if token is null`() {
        every { surchargeInteractor(None) } returns emptyMap()
        val result = surchargeFormatter.getSurchargeForSavedPaymentMethod(token = null)
        assertEquals(0, result)
    }

    @Test
    fun `getSurchargeForSavedPaymentMethod retrieves surcharge correctly`() {
        val token = mockk<PrimerVaultedPaymentMethod>()
        val paymentMethodType = "PAYMENT_CARD"
        val cardNetwork = CardNetwork.Type.VISA.name
        every { token.paymentMethodType } returns paymentMethodType
        every { token.paymentInstrumentData.binData?.network } returns cardNetwork

        every { surchargeInteractor(None)[paymentMethodType] } returns
            Surcharge.CardNetworksSurcharge(mapOf(cardNetwork to 200))

        val result = surchargeFormatter.getSurchargeForSavedPaymentMethod(token = token)

        assertEquals(200, result)
    }

    @Test
    fun `getSurchargeForPaymentMethodType returns 0 when no surcharge exists`() {
        val paymentMethodType = "PAYPAL"
        every { surchargeInteractor(None)[paymentMethodType] } returns null

        val result = surchargeFormatter.getSurchargeForPaymentMethodType(type = paymentMethodType)

        assertEquals(0, result)
    }

    @Test
    fun `getSurchargeForPaymentMethodType returns surcharge amount when it's a PaymentMethodSurcharge`() {
        val paymentMethodType = "PAYPAL"
        every { surchargeInteractor(None)[paymentMethodType] } returns Surcharge.PaymentMethodSurcharge(500)

        val result = surchargeFormatter.getSurchargeForPaymentMethodType(type = paymentMethodType)

        assertEquals(500, result)
    }

    @Test
    fun `getSurchargeForPaymentMethodType returns surcharge for network`() {
        val paymentMethodType = "PAYMENT_CARD"
        val cardNetwork = CardNetwork.Type.VISA.name
        every { surchargeInteractor(None)[paymentMethodType] } returns
            Surcharge.CardNetworksSurcharge(mapOf(cardNetwork to 300))

        val result =
            surchargeFormatter.getSurchargeForPaymentMethodType(type = paymentMethodType, network = cardNetwork)

        assertEquals(300, result)
    }

    @Test
    fun `formatSurchargeAsString returns no additional fee for zero amount`() {
        every { context.getString(R.string.no_additional_fee) } returns "No additional fee"

        val result = surchargeFormatter.formatSurchargeAsString(amount = 0, context = context)

        assertEquals("No additional fee", result)
    }

    @Test
    fun `formatSurchargeAsString returns formatted surcharge`() {
        val monetaryAmount = mockk<MonetaryAmount>()
        every { currency.currencyCode } returns "USD"
        every { monetaryAmount.currency } returns "USD"
        every { amountToCurrencyInteractor.execute(any()) } returns "$5.00"

        val result = surchargeFormatter.formatSurchargeAsString(amount = 500, context = context)

        assertEquals("+$5.00", result)
    }

    @Test
    fun `getSurchargeLabelTextForPaymentMethodType returns additional fees message when amount is null`() {
        every { context.getString(R.string.additional_fees_may_apply) } returns "Additional fees may apply"

        val result = surchargeFormatter.getSurchargeLabelTextForPaymentMethodType(amount = null, context)

        assertEquals("Additional fees may apply", result)
    }
}
