package io.primer.composable.internal.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.android.configuration.domain.model.Surcharge
import io.primer.ui.core.payment.domain.formatter.DefaultSurchargeFormatter

@Composable
internal fun SurchargeLabel(
    surcharge: Surcharge?,
    currency: java.util.Currency?,
    modifier: Modifier = Modifier,
) {
    if (surcharge != null && currency != null) {
        val spacing = LocalPrimerSpacingTokens.current
        val surchargeFormatter = remember { DefaultSurchargeFormatter() }
        
        Text(
            text = surchargeFormatter.formatSurchargeAmount(surcharge, currency),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(top = spacing.xsmall),
        )
    }
}