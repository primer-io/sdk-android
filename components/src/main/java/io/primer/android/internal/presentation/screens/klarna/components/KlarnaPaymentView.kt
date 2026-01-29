package io.primer.android.internal.presentation.screens.klarna.components

import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Composable for rendering the Klarna SDK payment view.
 */
@Composable
internal fun KlarnaPaymentView(
    paymentView: View,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            FrameLayout(context).apply {
                addView(paymentView)
            }
        },
        modifier = modifier,
    )
}
