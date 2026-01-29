package io.primer.android.internal.presentation.checkout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
internal fun DefaultLoading() {
    val loadingDescription = stringResource(R.string.accessibility_common_loading)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .testTag("primer_loading_screen")
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = loadingDescription
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.primer_checkout_loading_indicator),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = LocalPrimerTheme.current.spacingTokens.small),
        )
    }
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun DefaultLoadingPreview() = PreviewContainer {
    DefaultLoading()
}
