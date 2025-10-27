package io.primer.android.internal.presentation.screens.klarna

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.components.R
import io.primer.android.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.android.scope.PrimerKlarnaScope

@Composable
internal fun PrimerKlarnaScope.DefaultKlarnaScreen() {
    val state by state.collectAsStateWithLifecycle()

    Column {
        CheckoutAppBar(
            title = stringResource(R.string.primer_components_klarna_screen_title),
            onBackClick = { onBack() },
            onCancelClick = { onCancel() },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            with(components) {
                when (state.step) {
                    PrimerKlarnaScope.Step.Loading -> LoadingIndicator()
                    PrimerKlarnaScope.Step.CategorySelection -> CategorySelector()
                    PrimerKlarnaScope.Step.ViewReady -> PaymentViewContainer()
                    PrimerKlarnaScope.Step.AwaitingFinalization -> FinalizeButton()
                }
            }
        }
    }
}
