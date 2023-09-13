package io.primer.android.components.manager.nolpay

import androidx.lifecycle.SavedStateHandle
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.primer.android.components.data.payments.paymentMethods.nolpay.delegate.NolPayLinkPaymentCardDelegate
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidatorRegistry
import io.primer.android.components.manager.nolPay.NolPayData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PrimerHeadlessUniveralCheckoutNolPayManagerTest {

    @RelaxedMockK
    internal lateinit var nolPayConfigInteractor: NolPayConfigurationInteractor

    @RelaxedMockK
    internal lateinit var nolPayAppSecretInteractor: NolPayAppSecretInteractor

    @RelaxedMockK
    internal lateinit var nolPayDataValidatorRegistry: NolPayDataValidatorRegistry

    @RelaxedMockK
    internal lateinit var nolPayLinkPaymentCardDelegate: NolPayLinkPaymentCardDelegate

    @RelaxedMockK
    internal lateinit var savedStateHandle: SavedStateHandle

    private lateinit var manager: PrimerHeadlessUniversalCheckoutNolPayManager

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        manager = PrimerHeadlessUniversalCheckoutNolPayManager(
            nolPayConfigInteractor,
            nolPayAppSecretInteractor,
            nolPayDataValidatorRegistry,
            nolPayLinkPaymentCardDelegate,
            savedStateHandle
        )
    }

    @Test
    fun `should validate collected data`() = runTest {
        val collectedData = NolPayData.NolPayPhoneData("", "")
        val expectedValidationError = PrimerValidationError("test", "test")

        coEvery { nolPayDataValidatorRegistry.getValidator(any()).validate(any()) }.returns(
            listOf(
                expectedValidationError
            )
        )

        manager.updateCollectedData(collectedData)
        manager.validationFlow.collectLatest {
            assertEquals(listOf(expectedValidationError), it)
        }
    }

    @Test
    fun `should return tag when getAvailableTag() is called`() {

    }

    @Test
    fun `should emit collect card data step once sdk is initialised`() {

    }

    @Test
    fun `should handle collected card data when submit() is called`() {

    }

}