package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.delegate

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.domain.deeplink.klarna.KlarnaDeeplinkInteractor
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GetKlarnaDeeplinkDelegateTest {
    @MockK
    private lateinit var klarnaDeeplinkInteractor: KlarnaDeeplinkInteractor

    private lateinit var delegate: GetKlarnaDeeplinkDelegate

    @BeforeEach
    fun setUp() {
        delegate = GetKlarnaDeeplinkDelegate(interactor = klarnaDeeplinkInteractor)
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(klarnaDeeplinkInteractor)
    }

    @Test
    fun `getDeeplink() should return deeplink returned by deeplink interactor when called`() = runTest {
        every { klarnaDeeplinkInteractor.execute(any()) } returns "deeplink"

        val deeplink = delegate.getDeeplink().getOrNull()

        assertEquals("deeplink", deeplink)
        coVerify(exactly = 1) {
            klarnaDeeplinkInteractor.execute(any())
        }
    }
}
