package io.primer.composable.internal.presentation.checkout.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerTypographyTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CheckoutAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = LocalPrimerTypographyTokens.current.titleXlarge.toTextStyle(),
                color = LocalPrimerColorTokens.current.primerColorTextPrimary
            )
        },
        navigationIcon = {
            onBackClick?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = LocalPrimerColorTokens.current.primerColorTextPrimary,
                    )
                }
            }
        },
        actions = {
            onCancelClick?.let {
                TextButton(onClick = it) {
                    Text(
                        text = "Cancel",
                        style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
                        color = LocalPrimerColorTokens.current.primerColorTextPrimary
                    )
                }
            }
        },
        modifier = modifier,
    )
}
