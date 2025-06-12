package io.primer.composable.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.SubmitButton(
    modifier: Modifier = Modifier,
    text: String,
) {
    val currentState by state.collectAsState()

    Button(
        onClick = { submit() },
        modifier = modifier.fillMaxWidth(),
        enabled = currentState.isSubmitEnabled,
    ) {
        if (currentState.isLoading) {
            Text("Loading...")
        } else {
            Text(text)
        }
    }
}
