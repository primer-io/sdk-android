package io.primer.android.webRedirectShared.implementation.composer.ui.navigation.handler

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.mockk.mockk
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.WebRedirectNavigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartNewTaskNavigator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WebRedirectNavigationHandlerTest {
    private lateinit var navigationHandler: WebRedirectNavigationHandler
    private lateinit var context: Context
    private lateinit var activity: Activity
    private lateinit var launcher: ActivityResultLauncher<Intent>

    @BeforeEach
    fun setUp() {
        navigationHandler = WebRedirectNavigationHandler()
        context = mockk(relaxed = true)
        activity = mockk(relaxed = true)
        launcher = mockk(relaxed = true)
    }

    @Test
    fun `getSupportedNavigators with context should return StartNewTaskNavigator`() {
        val navigators = navigationHandler.getSupportedNavigators(context)
        assertEquals(1, navigators.size)
        assertEquals(StartNewTaskNavigator::class.java, navigators[0]::class.java)
    }

    @Test
    fun `getSupportedNavigators with activity and launcher should return WebRedirectNavigator`() {
        val navigators = navigationHandler.getSupportedNavigators(activity, launcher)
        assertEquals(1, navigators.size)
        assertEquals(WebRedirectNavigator::class.java, navigators[0]::class.java)
    }
}
