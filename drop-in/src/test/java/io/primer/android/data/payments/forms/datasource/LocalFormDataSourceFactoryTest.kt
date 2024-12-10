package io.primer.android.data.payments.forms.datasource

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.domain.helper.CountriesRepository
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertIs

class LocalFormDataSourceFactoryTest {

    @MockK
    private lateinit var primerTheme: PrimerTheme

    @MockK
    private lateinit var countriesRepository: CountriesRepository

    private lateinit var factory: LocalFormDataSourceFactory

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        factory = LocalFormDataSourceFactory(primerTheme, countriesRepository)
    }

    @Test
    fun `should return BlikLocalFormDataSource for ADYEN_BLIK`() {
        val dataSource = factory.getLocalFormDataSource(PaymentMethodType.ADYEN_BLIK)
        assertIs<BlikLocalFormDataSource>(dataSource)
    }

    @Test
    fun `should return PhoneNumberLocalFormDataSource for XENDIT_OVO`() {
        val dataSource = factory.getLocalFormDataSource(PaymentMethodType.XENDIT_OVO)
        assertIs<PhoneNumberLocalFormDataSource>(dataSource)
    }

    @Test
    fun `should return PhoneNumberLocalFormDataSource for ADYEN_MBWAY`() {
        val dataSource = factory.getLocalFormDataSource(PaymentMethodType.ADYEN_MBWAY)
        assertIs<PhoneNumberLocalFormDataSource>(dataSource)
    }

    @Test
    fun `should return XfersLocalFormDataSource for XFERS_PAYNOW`() {
        val dataSource = factory.getLocalFormDataSource(PaymentMethodType.XFERS_PAYNOW)
        assertIs<XfersLocalFormDataSource>(dataSource)
    }

    @Test
    fun `should return FastBankTransferLocalFormDataSource for RAPYD_FAST`() {
        val dataSource = factory.getLocalFormDataSource(PaymentMethodType.RAPYD_FAST)
        assertIs<FastBankTransferLocalFormDataSource>(dataSource)
    }

    @Test
    fun `should return PromptPayLocalFormDataSource for OMISE_PROMPTPAY`() {
        val dataSource = factory.getLocalFormDataSource(PaymentMethodType.OMISE_PROMPTPAY)
        assertIs<PromptPayLocalFormDataSource>(dataSource)
    }

    @Test
    fun `should return PromptPayLocalFormDataSource for RAPYD_PROMPTPAY`() {
        val dataSource = factory.getLocalFormDataSource(PaymentMethodType.RAPYD_PROMPTPAY)
        assertIs<PromptPayLocalFormDataSource>(dataSource)
    }

    @Test
    fun `should return MultibancoLocalFormDataSource for ADYEN_MULTIBANCO`() {
        val dataSource = factory.getLocalFormDataSource(PaymentMethodType.ADYEN_MULTIBANCO)
        assertIs<MultibancoLocalFormDataSource>(dataSource)
    }

    @Test
    fun `should return BancontactCardLocalFormDataSource for ADYEN_BANCONTACT_CARD`() {
        val dataSource = factory.getLocalFormDataSource(PaymentMethodType.ADYEN_BANCONTACT_CARD)
        assertIs<BancontactCardLocalFormDataSource>(dataSource)
    }

    @Test
    fun `should throw error for unsupported paymentMethodType (KLARNA)`() {
        assertThrows<IllegalStateException>("Invalid paymentMethodType KLARNA") {
            factory.getLocalFormDataSource(PaymentMethodType.KLARNA)
        }
    }
}
