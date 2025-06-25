package io.primer.android.internal.presentation.screens.card.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.primer.android.components.R
import io.primer.android.components.assets.ui.getCardImageAsset
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.displayMetadata.domain.model.ImageColor

@Composable
internal fun CardNetworkIcon(
    modifier: Modifier = Modifier,
    networkType: CardNetwork.Type?,
    size: Int = 24
) {
    val iconResource = networkType?.getCardImageAsset(ImageColor.COLORED) 
        ?: R.drawable.ic_generic_card
    
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Icon(
            painter = painterResource(id = iconResource),
            contentDescription = networkType?.name ?: "Card network",
            modifier = Modifier.size(size.dp),
            tint = Color.Unspecified
        )
    }
}
