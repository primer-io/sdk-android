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
import io.primer.composable.PrimerCheckout
import io.primer.composable.internal.di.ComposableSdk

@Composable
internal fun Checkout(
    clientToken: String,
    primerSettings: PrimerSettings,
    successContent: (@Composable () -> Unit)?,
    failureContent: (@Composable (cause: PrimerError) -> Unit)?,
    content: (@Composable PrimerCheckout.() -> Unit)?,
) {

    val context = LocalContext.current

    DisposableEffect(clientToken) {
        ComposableSdk.initialize(context, clientToken, primerSettings)
        onDispose { ComposableSdk.cleanup() }
    }

    when (ComposableSdk.state.collectAsStateWithLifecycle().value) {
        is ComposableSdk.State.Error -> Unit
        ComposableSdk.State.Initializing -> Unit
        ComposableSdk.State.NotInitialized -> Unit
        ComposableSdk.State.Ready -> {
            val viewModel = viewModel<CheckoutViewModel>()
            content?.let { it(viewModel) } ?: viewModel.DefaultContent()
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrimerCheckout.DefaultContent() {
    ModalBottomSheet(
        onDismissRequest = { cleanup() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        NavigationHost()
    }
}
