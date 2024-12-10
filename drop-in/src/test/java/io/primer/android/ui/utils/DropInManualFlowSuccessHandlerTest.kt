package io.primer.android.ui.utils

import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.paymentMethods.core.domain.repository.PrimerHeadlessRepository
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
internal class DropInManualFlowSuccessHandlerTest {

    @MockK(relaxed = true)
    lateinit var primerHeadlessRepository: PrimerHeadlessRepository

    @Test
    fun `handle should call handleManualFlowSuccess`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val checkoutAdditionalInfo = mockk<PrimerCheckoutAdditionalInfo>()
        val dropInManualFlowSuccessHandler = DropInManualFlowSuccessHandler(primerHeadlessRepository)

        dropInManualFlowSuccessHandler.handle(checkoutAdditionalInfo)

        coVerify { primerHeadlessRepository.handleManualFlowSuccess(checkoutAdditionalInfo) }
    }
}
