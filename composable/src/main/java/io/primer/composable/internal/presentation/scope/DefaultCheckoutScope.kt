package io.primer.composable.internal.presentation.scope

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.composable.internal.presentation.screens.error.DefaultErrorScreen
import io.primer.composable.internal.presentation.screens.loading.DefaultLoadingScreen
import io.primer.composable.internal.presentation.screens.splash.DefaultSplashScreen
import io.primer.composable.internal.presentation.screens.success.DefaultSuccessScreen
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.scope.PrimerCardFormScope
import io.primer.composable.scope.PrimerCheckoutScope
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope

internal abstract class DefaultCheckoutScope : ViewModel(), PrimerCheckoutScope, DISdkComponent {

    @OptIn(ExperimentalMaterial3Api::class)
    override var container: @Composable (content: @Composable () -> Unit) -> Unit = { content ->
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
            onDismissRequest = ::onDismiss,
            dragHandle = {},
            containerColor = LocalPrimerColorTokens.current.primerColorBackground,
            content = { content() },
        )
    }

    override var splashScreen: @Composable () -> Unit = {
        DefaultSplashScreen()
    }

    override var loadingScreen: @Composable () -> Unit = {
        DefaultLoadingScreen()
    }

    override var successScreen: @Composable () -> Unit = {
        DefaultSuccessScreen()
    }

    override var errorScreen: @Composable (message: String) -> Unit = { message ->
        DefaultErrorScreen(message = message)
    }

    override val cardForm: PrimerCardFormScope by lazy { resolve() }

    override val paymentMethodSelection: PrimerPaymentMethodSelectionScope by lazy { resolve() }
}
