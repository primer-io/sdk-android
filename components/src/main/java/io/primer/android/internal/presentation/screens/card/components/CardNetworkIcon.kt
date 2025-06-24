package io.primer.android.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.primer.android.components.R
import io.primer.android.configuration.data.model.CardNetwork

@Composable
internal fun CardNetworkIcon(
    modifier: Modifier = Modifier,
    networkType: CardNetwork.Type?,
    size: Int = 24
) {
    // For now, use a simple placeholder icon
    // TODO: Integrate with proper assets manager when DI is resolved
    Box(modifier = modifier.size(size.dp)) {
        Icon(
            painter = painterResource(id = R.drawable.ic_generic_card),
            contentDescription = networkType?.name ?: "Card network",
            modifier = Modifier.size(size.dp),
            tint = if (networkType == null) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}