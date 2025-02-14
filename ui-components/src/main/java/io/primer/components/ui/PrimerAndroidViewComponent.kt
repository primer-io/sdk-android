package io.primer.components.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.primer.android.klarna.api.ui.PrimerKlarnaPaymentView

@Composable
fun PrimerAndroidViewComponent(view: PrimerKlarnaPaymentView) {
    AndroidView(
        factory = { _ ->
            // Create and configure your regular View
            view
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    )
}
