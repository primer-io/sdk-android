package io.primer.composable.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.SubmitButton(
    modifier: Modifier = Modifier,
    text: String
) {
    Button(
        onClick = { submit() },
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}
