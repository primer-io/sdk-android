package io.primer.composable.internal.presentation.checkout.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CheckoutAppBar(
    title: String,
    onBackClick: (() -> Unit)?,
    onCancelClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge,
            )
        },
        navigationIcon = {
            onBackClick?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        actions = {
            onCancelClick?.let {
                TextButton(
                    onClick = it,
                    modifier = Modifier.padding(end = LocalPrimerSpacingTokens.current.small),
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        modifier = modifier,
    )
}
