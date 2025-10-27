package io.primer.android.internal.presentation.screens.klarna.components

import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.scope.PrimerKlarnaScope

@Composable
internal fun PrimerKlarnaScope.DefaultKlarnaPaymentViewContainer(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsStateWithLifecycle()

    Column(modifier = modifier) {
        AndroidView(
            factory = { context ->
                FrameLayout(context)
            },
            update = { container ->
                container.removeAllViews()
                state.paymentView?.let { view ->
                    container.addView(view.get())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        with(components) {
            AuthorizeButton()
        }
    }
}
