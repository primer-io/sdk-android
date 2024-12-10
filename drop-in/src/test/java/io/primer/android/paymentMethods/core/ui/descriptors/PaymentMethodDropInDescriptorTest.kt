@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.core.ui.descriptors

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.LoadingState
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.fragments.SessionCompleteFragment
import io.primer.android.ui.fragments.SessionCompleteViewType
import io.primer.android.ui.fragments.SuccessType
import io.primer.android.viewmodel.ViewStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertIs
import kotlin.test.assertNull

class PaymentMethodDropInDescriptorTest {

    @Test
    fun `createSuccessBehavior() returns correct NewFragmentBehaviour`() {
        mockkObject(SessionCompleteFragment)
        val fragment = mockk<SessionCompleteFragment>()
        every { SessionCompleteFragment.newInstance(any(), any()) } returns fragment
        val viewStatus = mockk<ViewStatus.ShowSuccess> {
            every { delay } returns 3000
            every { successType } returns SuccessType.PAYMENT_SUCCESS
        }

        val descriptor = object : PaymentMethodDropInDescriptor {
            override val paymentMethodType: String = "paymentMethodType"
            override val uiOptions: UiOptions = mockk()
            override val selectedBehaviour: PaymentMethodBehaviour = mockk()
            override val uiType: PaymentMethodUiType = mockk()
            override val loadingState: LoadingState? = null
        }

        val result = descriptor.createSuccessBehavior(viewStatus)

        val resultFragment = result.factory.invoke() as SessionCompleteFragment
        assertEquals(false, result.returnToPreviousOnBack)
        verify {
            SessionCompleteFragment.newInstance(3000, SessionCompleteViewType.Success(SuccessType.PAYMENT_SUCCESS))
        }
        unmockkObject(SessionCompleteFragment)
    }

    @Test
    fun `behaviours returns empty list when isInitScreenEnabled is false and isStandalonePaymentMethod is true`() {
        val uiOptions = mockk<UiOptions> {
            every { isInitScreenEnabled } returns false
            every { isStandalonePaymentMethod } returns true
        }

        val descriptor = object : PaymentMethodDropInDescriptor {
            override val paymentMethodType: String = "paymentMethodType"
            override val uiOptions: UiOptions = uiOptions
            override val selectedBehaviour: PaymentMethodBehaviour = mockk()
            override val uiType: PaymentMethodUiType = mockk()
            override val loadingState: LoadingState? = null
        }

        assertEquals(emptyList<PaymentMethodBehaviour>(), descriptor.behaviours)
    }

    @Test
    fun `behaviours returns list with NewFragmentBehaviour when isInitScreenEnabled is true or isStandalonePaymentMethod is false`() {
        mockkObject(PaymentMethodLoadingFragment)
        every { PaymentMethodLoadingFragment.Companion.newInstance() } returns mockk()
        val uiOptions = mockk<UiOptions> {
            every { isInitScreenEnabled } returns true
            every { isStandalonePaymentMethod } returns false
        }

        val descriptor = object : PaymentMethodDropInDescriptor {
            override val paymentMethodType: String = "paymentMethodType"
            override val uiOptions: UiOptions = uiOptions
            override val selectedBehaviour: PaymentMethodBehaviour = mockk()
            override val uiType: PaymentMethodUiType = mockk()
            override val loadingState: LoadingState? = null
        }

        val behaviour = descriptor.behaviours.single()
        assertIs<NewFragmentBehaviour>(behaviour)
        behaviour.factory.invoke()
        verify { PaymentMethodLoadingFragment.newInstance() }
        unmockkObject(PaymentMethodLoadingFragment)
    }

    @Test
    fun `cancelBehaviour returns null`() {
        val uiOptions = mockk<UiOptions>()
        val descriptor = object : PaymentMethodDropInDescriptor {
            override val paymentMethodType: String = "paymentMethodType"
            override val uiOptions: UiOptions = uiOptions
            override val selectedBehaviour: PaymentMethodBehaviour = mockk()
            override val uiType: PaymentMethodUiType = mockk()
            override val loadingState: LoadingState? = null
        }

        val cancelBehaviour = descriptor.cancelBehaviour

        assertNull(cancelBehaviour)
    }
}
