package io.primer.android.internal.presentation.checkout.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.primer.android.components.R
import io.primer.android.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.android.internal.presentation.theme.LocalPrimerSizeTokens
import io.primer.android.internal.presentation.theme.LocalPrimerSpacingTokens
import kotlinx.coroutines.delay

@Composable
internal fun DefaultSuccessScreen(
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
) {
    val spacing = LocalPrimerSpacingTokens.current
    val sizes = LocalPrimerSizeTokens.current
    val colorTokens = LocalPrimerColorTokens.current

    LaunchedEffect(onDismiss) {
        if (onDismiss != null) {
            delay(3000)
            onDismiss()
        }
    }

    Column(
        modifier = modifier
            .padding(sizes.xxxlarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_primer_success),
            contentDescription = stringResource(R.string.primer_components_content_description_payment_successful),
            tint = Color.Unspecified,
        )

        Spacer(modifier = Modifier.height(spacing.small))

        Text(
            text = stringResource(R.string.primer_components_checkout_success_title),
            style = MaterialTheme.typography.headlineSmall,
            color = colorTokens.primerColorTextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(spacing.xsmall))

        Text(
            text = stringResource(R.string.primer_components_checkout_success_description),
            style = MaterialTheme.typography.bodyMedium,
            color = colorTokens.primerColorTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
