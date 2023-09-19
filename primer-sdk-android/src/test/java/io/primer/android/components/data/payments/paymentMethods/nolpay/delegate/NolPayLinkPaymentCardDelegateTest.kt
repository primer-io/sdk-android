package io.primer.android.components.data.payments.paymentMethods.nolpay.delegate

import io.mockk.impl.annotations.RelaxedMockK
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayLinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayUnlinkPaymentCardInteractor
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayLinkPaymentCardDelegate
import io.primer.android.domain.tokenization.TokenizationInteractor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NolPayLinkPaymentCardDelegateTest {
    @RelaxedMockK
    internal lateinit var nolPayGetLinkPaymentCardTokenInteractor:
        NolPayGetLinkPaymentCardTokenInteractor

    @RelaxedMockK
    internal lateinit var nolPayGetLinkPaymentCardOTPInteractor:
        NolPayGetLinkPaymentCardOTPInteractor

    @RelaxedMockK
    internal lateinit var nolPayLinkPaymentCardInteractor: NolPayLinkPaymentCardInteractor

    @RelaxedMockK
    internal lateinit var nolPayUnlinkPaymentCardInteractor: NolPayUnlinkPaymentCardInteractor

    @RelaxedMockK
    internal lateinit var tokenizationInteractor: TokenizationInteractor

    @RelaxedMockK
    internal lateinit var errorFlowResolver: BaseErrorFlowResolver

    private lateinit var delegate: NolPayLinkPaymentCardDelegate

    @BeforeEach
    fun setup() {
        delegate = NolPayLinkPaymentCardDelegate(
            nolPayGetLinkPaymentCardTokenInteractor,
            nolPayGetLinkPaymentCardOTPInteractor,
            nolPayLinkPaymentCardInteractor,
            nolPayUnlinkPaymentCardInteractor,
            tokenizationInteractor,
            errorFlowResolver
        )
    }

    @Test
    fun `should store linked payment card and card token when collected data is NolPayTagData`() {
    }

    @Test
    fun `should store phone number data when collected data is NolPayPhoneData`() {
    }

    @Test
    fun `should emit correct step when getPaymentCardToken() is successful`() {
    }

    @Test
    fun `should emit error when getPaymentCardToken() fails`() {
    }

    @Test
    fun `should emit correct step when getPaymentCardOTP() is successful`() {
    }

    @Test
    fun `should emit error when getPaymentCardOTP() fails`() {
    }

    @Test
    fun `should emit error when linkPaymentCard() fails`() {
    }

    @Test
    fun `should emit correct step when tokenize() is successful`() {
    }

    @Test
    fun `should emit error when tokenize() fails`() {
    }
}
