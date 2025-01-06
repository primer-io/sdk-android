package io.primer.android.card.implementation.composer.ui.navigation.handler

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.primer.android.card.implementation.composer.ui.navigation.Card3DSNavigator
import io.primer.android.card.implementation.composer.ui.navigation.CardProcessor3DSNavigator
import io.primer.android.card.implementation.composer.ui.navigation.MockCard3DSNavigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartNewTaskNavigator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class CardNavigationHandlerTest {
    @RelaxedMockK
    private lateinit var context: Context

    @RelaxedMockK
    private lateinit var activity: Activity

    @RelaxedMockK
    private lateinit var launcher: ActivityResultLauncher<Intent>

    private lateinit var handler: CardNavigationHandler

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        handler = CardNavigationHandler()
    }

    @Test
    fun `getSupportedNavigators should return StartNewTaskNavigator when called with Context`() {
        // When
        val navigators = handler.getSupportedNavigators(context)

        // Then
        assertEquals(1, navigators.size)
        assertEquals(StartNewTaskNavigator::class, navigators[0]::class)
    }

    @Test
    fun `getSupportedNavigators should return Card3DSNavigator, CardProcessor3DSNavigator and MockCard3DSNavigator when called with Activity and Launcher`() {
        // When
        val navigators = handler.getSupportedNavigators(activity, launcher)

        // Then
        assertEquals(3, navigators.size)
        assertEquals(Card3DSNavigator::class, navigators[0]::class)
        assertEquals(CardProcessor3DSNavigator::class, navigators[1]::class)
        assertEquals(MockCard3DSNavigator::class, navigators[2]::class)
    }
}
