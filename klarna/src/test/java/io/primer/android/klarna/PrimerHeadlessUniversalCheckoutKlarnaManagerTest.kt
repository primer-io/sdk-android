package io.primer.android.klarna

import androidx.lifecycle.ViewModelStoreOwner
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.klarna.api.component.KlarnaComponent
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PrimerHeadlessUniversalCheckoutKlarnaManagerTest {
    private lateinit var viewModelStoreOwner: ViewModelStoreOwner
    private lateinit var primerSessionIntent: PrimerSessionIntent
    private lateinit var klarnaManager: PrimerHeadlessUniversalCheckoutKlarnaManager

    @BeforeEach
    fun setUp() {
        viewModelStoreOwner = mockk(relaxed = true)
        primerSessionIntent = mockk(relaxed = true)
        klarnaManager = PrimerHeadlessUniversalCheckoutKlarnaManager(viewModelStoreOwner)
        mockkObject(KlarnaComponent)
    }

    @Test
    fun `provideKlarnaComponent should return KlarnaComponent instance`() {
        // Arrange
        val expectedComponent = mockk<KlarnaComponent>(relaxed = true)
        every { KlarnaComponent.provideInstance(viewModelStoreOwner, primerSessionIntent) } returns expectedComponent

        // Act
        val component = klarnaManager.provideKlarnaComponent(primerSessionIntent)

        // Assert
        assertNotNull(component)
        verify { KlarnaComponent.provideInstance(viewModelStoreOwner, primerSessionIntent) }
    }
}
