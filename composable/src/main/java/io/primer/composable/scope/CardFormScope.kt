package io.primer.composable.scope

import androidx.compose.runtime.Composable

interface CardFormScope {

    fun submitButton(content: @Composable () -> Unit)

}
