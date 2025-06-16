package io.primer.composable.internal.presentation.checkout.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun CheckoutScaffold(
    title: String,
    onBackClick: (() -> Unit)?,
    onCancelClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            CheckoutAppBar(
                title = title,
                onBackClick = onBackClick,
                onCancelClick = onCancelClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
        content = content
    )
}
