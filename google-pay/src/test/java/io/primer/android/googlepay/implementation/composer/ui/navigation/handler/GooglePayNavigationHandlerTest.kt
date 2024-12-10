package io.primer.android.googlepay.implementation.composer.ui.navigation.handler

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.googlepay.GooglePayFacadeFactory
import io.primer.android.googlepay.implementation.composer.ui.navigation.GooglePay3DSNavigator
import io.primer.android.googlepay.implementation.composer.ui.navigation.GooglePayNavigator
import io.primer.android.googlepay.implementation.composer.ui.navigation.GooglePayProcessor3DSNavigator
import io.primer.android.googlepay.implementation.composer.ui.navigation.MockGooglePay3DSNavigator
import io.primer.paymentMethodCoreUi.core.ui.navigation.StartNewTaskNavigator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class GooglePayNavigationHandlerTest {

    @RelaxedMockK
    private lateinit var context: Context

    @RelaxedMockK
    private lateinit var activity: Activity

    @RelaxedMockK
    private lateinit var launcher: ActivityResultLauncher<Intent>

    private lateinit var handler: GooglePayNavigationHandler

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        handler = GooglePayNavigationHandler()
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
    fun `getSupportedNavigators should return GooglePayNavigator, GooglePay3DSNavigator, GooglePayProcessor3DSNavigator and MockGooglePay3DSNavigator when called with Activity and Launcher`() {
        // Given
        DISdkContext.headlessSdkContainer = mockk<SdkContainer>(relaxed = true).also { sdkContainer ->
            val cont = spyk<DependencyContainer>().also { container ->
                container.registerFactory<LogReporter> { mockk(relaxed = true) }
                container.registerFactory<GooglePayFacadeFactory> { mockk(relaxed = true) }
            }

            every { sdkContainer.containers }.returns(mutableMapOf(cont::class.simpleName.orEmpty() to cont))
        }

        // When
        val navigators = handler.getSupportedNavigators(activity, launcher)

        // Then
        assertEquals(4, navigators.size)
        assertEquals(GooglePayNavigator::class, navigators[0]::class)
        assertEquals(GooglePay3DSNavigator::class, navigators[1]::class)
        assertEquals(GooglePayProcessor3DSNavigator::class, navigators[2]::class)
        assertEquals(MockGooglePay3DSNavigator::class, navigators[3]::class)
    }
}
