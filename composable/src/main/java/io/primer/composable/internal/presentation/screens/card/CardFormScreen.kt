package io.primer.composable.internal.presentation.screens.card

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.scope.CardFormScope

@Composable
fun CardFormScope.CardFormScreen(
    modifier: Modifier = Modifier
) {
    Text("Card Form Screen")
    submitButton {
        Text("Submit")
    }
}
