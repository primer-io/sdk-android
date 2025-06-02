package io.primer.composable.internal.presentation.checkout

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.models.PrimerError
import io.primer.composable.internal.di.ComposableSdk
import io.primer.composable.internal.di.ComposableSdk.cleanup
import io.primer.composable.internal.presentation.screens.card.CardFormScreen
import io.primer.composable.internal.presentation.screens.error.ErrorScreen
import io.primer.composable.internal.presentation.screens.loading.LoadingScreen
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionScreen
import io.primer.composable.internal.presentation.screens.success.SuccessScreen
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.PaymentMethodSelectionScope
import io.primer.composable.scope.PrimerCheckoutScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Checkout(
    clientToken: String,
    primerSettings: PrimerSettings,
    loadingScreen: (@Composable () -> Unit)?,
    paymentSelectionScreen: (@Composable PaymentMethodSelectionScope.() -> Unit)?,
    cardFormScopeScreen: (@Composable CardFormScope.() -> Unit)?,
    successScreen: (@Composable () -> Unit)?,
    errorScreen: (@Composable (cause: PrimerError) -> Unit)?,
    content: (@Composable PrimerCheckoutScope.() -> Unit)?,
) {

    val context = LocalContext.current

    DisposableEffect(clientToken) {
        ComposableSdk.initialize(context, clientToken, primerSettings)
        onDispose { cleanup() }
    }

    when (ComposableSdk.state.collectAsStateWithLifecycle().value) {
        is ComposableSdk.State.Error -> Unit
        ComposableSdk.State.Initializing -> Unit
        ComposableSdk.State.NotInitialized -> Unit
        ComposableSdk.State.Ready -> {
            val viewModel = viewModel<CheckoutViewModel>()
            content?.let { it(viewModel) } ?: ModalBottomSheet(
                onDismissRequest = { cleanup() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                viewModel.NavigationHost(
                    loadingScreen = loadingScreen ?: { LoadingScreen() },
                    paymentSelectionScreen = paymentSelectionScreen ?: { PaymentMethodSelectionScreen() },
                    cardFormScopeScreen = cardFormScopeScreen ?: { CardFormScreen() },
                    successScreen = successScreen ?: { SuccessScreen(message = "Success!") },
                    errorScreen = errorScreen ?: { ErrorScreen(message = it.description) }
                )
            }
        }
    }
}
