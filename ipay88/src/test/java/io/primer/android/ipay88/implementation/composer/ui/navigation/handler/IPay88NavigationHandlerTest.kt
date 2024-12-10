package io.primer.android.ipay88.implementation.composer.ui.navigation.handler

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.mockk.mockk
import io.primer.android.ipay88.implementation.composer.ui.navigation.IPay88MockNavigator
import io.primer.android.ipay88.implementation.composer.ui.navigation.IPay88Navigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartNewTaskNavigator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class IPay88NavigationHandlerTest {

    private val context: Context = mockk()
    private val activity: Activity = mockk()
    private val launcher: ActivityResultLauncher<Intent> = mockk()

    private val navigationHandler = IPay88NavigationHandler()

    @Test
    fun `getSupportedNavigators with context should return StartNewTaskNavigator`() {
        val navigators = navigationHandler.getSupportedNavigators(context)

        assertEquals(1, navigators.size)
        assertTrue(navigators.first() is StartNewTaskNavigator)
    }

    @Test
    fun `getSupportedNavigators with activity and launcher should return IPay88Navigator`() {
        val navigators = navigationHandler.getSupportedNavigators(activity, launcher)

        assertEquals(2, navigators.size)
        assertTrue(navigators[0] is IPay88Navigator)
        assertTrue(navigators[1] is IPay88MockNavigator)
    }
}
