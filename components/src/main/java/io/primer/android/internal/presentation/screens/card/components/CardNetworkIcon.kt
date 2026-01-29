package io.primer.android.internal.presentation.screens.card.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import io.primer.android.components.R
import io.primer.android.components.assets.ui.getCardImageAsset
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.displayMetadata.domain.model.ImageColor

@Composable
internal fun CardNetworkIcon(
    network: CardNetwork.Type?,
    modifier: Modifier = Modifier,
) {
    val iconResId = network?.getCardImageAsset(ImageColor.COLORED) ?: R.drawable.ic_primer_credit_card

    Icon(
        painter = painterResource(id = iconResId),
        contentDescription = network?.name,
        modifier = modifier,
        tint = Color.Unspecified,
    )
}
