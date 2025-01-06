package io.primer.android.ipay88.implementation.composer.ui.navigation

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import io.primer.android.ipay88.implementation.composer.ui.navigation.extension.toIPay88LauncherParams
import io.primer.android.ipay88.implementation.composer.ui.navigation.launcher.IPay88ActivityLauncherParams
import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import io.primer.ipay88.api.ui.IPay88LauncherParams
import io.primer.ipay88.api.ui.NativeIPay88Activity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IPay88NavigatorTest {
    private lateinit var context: Context
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var navigator: IPay88Navigator

    @BeforeEach
    fun setUp() {
        context = mockk()
        launcher = mockk(relaxed = true)
        navigator = IPay88Navigator(context, launcher)
    }

    @Test
    fun `navigate should launch the correct intent`() {
        val launcherParams = mockk<IPay88ActivityLauncherParams>()

        val mockIPay88LauncherParams = mockk<IPay88LauncherParams>()
        mockkStatic(IPay88ActivityLauncherParams::toIPay88LauncherParams)
        every { any<IPay88ActivityLauncherParams>().toIPay88LauncherParams() } returns mockIPay88LauncherParams

        val intent = mockk<Intent>()

        // Mock the NativeIPay88Activity.getLaunchIntent() call
        mockkObject(NativeIPay88Activity.Companion)
        every {
            NativeIPay88Activity.Companion.getLaunchIntent(context, mockIPay88LauncherParams)
        } returns intent

        // Use mockkConstructor to mock IPay88ActivityLauncherParams
        mockkConstructor(IPay88ActivityLauncherParams::class)
        every { anyConstructed<IPay88ActivityLauncherParams>().errorCode } returns 0

        navigator.navigate(launcherParams)

        verify { launcher.launch(intent) }
    }

    @Test
    fun `canHandle should return true for IPay88ActivityLauncherParams`() {
        val params = mockk<IPay88ActivityLauncherParams>()

        val result = navigator.canHandle(params)

        assert(result)
    }

    @Test
    fun `canHandle should return false for non-IPay88ActivityLauncherParams`() {
        val params = mockk<NavigationParams>()

        val result = navigator.canHandle(params)

        assert(!result)
    }
}
