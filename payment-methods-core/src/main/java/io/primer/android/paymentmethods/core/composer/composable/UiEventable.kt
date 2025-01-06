package io.primer.android.paymentmethods.core.composer.composable

import io.primer.android.paymentmethods.core.ui.navigation.NavigationParams
import kotlinx.coroutines.flow.SharedFlow

sealed interface ComposerUiEvent {
    data class Navigate(val params: NavigationParams) : ComposerUiEvent

    object Finish : ComposerUiEvent
}

interface UiEventable {
    val uiEvent: SharedFlow<ComposerUiEvent>
}
