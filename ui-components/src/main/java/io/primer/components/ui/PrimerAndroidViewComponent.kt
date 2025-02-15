package io.primer.components.ui

import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * A composable that wraps a custom [View] inside an Compose [AndroidView], allowing it to be rendered within a
 * composable function.
 * This enables seamless integration of traditional Android views into a Compose-based layout.
 *
 * @param modifier The [Modifier] to be applied to the wrapping [AndroidView].
 * @param view The custom [View] instance to wrap.
 */
@Composable
fun PrimerAndroidViewComponent(modifier: Modifier = Modifier, view: View) {
    AndroidView(
        factory = { view },
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    )
}
