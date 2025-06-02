package io.primer.composable.internal.presentation.screens.card

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.CardFormScreen(
    modifier: Modifier = Modifier,
    submitButton: @Composable () -> Unit = { SubmitButton(text = "Submit") }
) {
    Text("Card Form Screen")
    submitButton()
}

@Composable
internal fun CardFormScope.SubmitButton(
    modifier: Modifier = Modifier,
    text: String
) {
    Button(onClick = {
        submit()
    }) {
        Text(text)
    }
}
